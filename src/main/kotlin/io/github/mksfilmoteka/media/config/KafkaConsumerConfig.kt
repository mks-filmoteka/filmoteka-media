package io.github.mksfilmoteka.media.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import java.time.Duration

@Configuration(proxyBeanMethods = false)
class KafkaConsumerConfig {

    @Bean
    fun filmDeletedDltTopic(
        @Value($$"${app.kafka.topics.film-deleted.name}")
        topic: String
    ): NewTopic = dltTopic(topic)

    @Bean
    fun filmPosterChangedDltTopic(
        @Value($$"${app.kafka.topics.film-poster-changed.name}")
        topic: String
    ): NewTopic = dltTopic(topic)

    private fun dltTopic(sourceTopic: String): NewTopic =
        TopicBuilder
            .name("$sourceTopic.media.dlt")
            .config(TopicConfig.RETENTION_MS_CONFIG, Duration.ofDays(30).toMillis().toString())
            .build()
}
