package io.github.mksfilmoteka.media.catalog

import io.github.mksfilmoteka.media.config.KafkaConsumerConfig
import io.github.mksfilmoteka.media.file.FileService
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [CatalogListenersIntegrationTest.TestConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "spring.kafka.admin.auto-create=true",
        "spring.kafka.listener.auto-startup=true",
        "spring.kafka.consumer.auto-offset-reset=earliest"
    ]
)
@EmbeddedKafka(
    partitions = 1,
    topics = [$$"${app.kafka.topics.film-deleted.name}", $$"${app.kafka.topics.film-poster-changed.name}"]
)
@DirtiesContext
class CatalogListenersIntegrationTest {

    @Value($$"${app.kafka.topics.film-deleted.name}")
    private lateinit var filmDeletedTopic: String

    @Value($$"${app.kafka.topics.film-poster-changed.name}")
    private lateinit var filmPosterChangedTopic: String

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var embeddedKafka: EmbeddedKafkaBroker

    @MockitoBean
    private lateinit var fileService: FileService

    @Test
    fun `should publish film deletion to DLT after three failed attempts`() {
        val payload = """
            {
              "eventId": "11111111-1111-4111-8111-111111111111",
              "filmId": 123,
              "posterName": "deleted-poster.jpg",
              "occurredAt": "2026-09-21T10:00:00Z"
            }
        """.trimIndent()

        assertFailedCleanupReachesDlt(filmDeletedTopic, "123", payload, "deleted-poster.jpg")
    }

    @Test
    fun `should publish poster change to DLT after three failed attempts`() {
        val payload = """
            {
              "eventId": "22222222-2222-4222-8222-222222222222",
              "filmId": 456,
              "oldPosterName": "old-poster.jpg",
              "newPosterName": "new-poster.jpg",
              "occurredAt": "2026-09-21T10:00:00Z"
            }
        """.trimIndent()

        assertFailedCleanupReachesDlt(filmPosterChangedTopic, "456", payload, "old-poster.jpg")
    }

    private fun assertFailedCleanupReachesDlt(topic: String, key: String, payload: String, posterName: String) {
        val dltTopic = "$topic.media.dlt"

        doThrow(IllegalStateException("Simulated cleanup failure")).`when`(fileService).delete(posterName)

        val consumerFactory = DefaultKafkaConsumerFactory(
            KafkaTestUtils.consumerProps(embeddedKafka, "$topic-dlt-reader", false),
            StringDeserializer(), StringDeserializer()
        )

        consumerFactory.createConsumer().use { consumer ->
            consumer.subscribe(listOf(dltTopic))

            kafkaTemplate.send(topic, key, payload).get(10, TimeUnit.SECONDS)

            val received = KafkaTestUtils.getSingleRecord(consumer, dltTopic)

            assertEquals(key, received.key())
            assertEquals(payload, received.value())

            verify(fileService, times(3)).delete(posterName)
            verifyNoMoreInteractions(fileService)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration(KafkaAutoConfiguration::class, JacksonAutoConfiguration::class)
    @Import(KafkaConsumerConfig::class, FilmDeletedListener::class, FilmPosterChangedListener::class)
    class TestConfig
}
