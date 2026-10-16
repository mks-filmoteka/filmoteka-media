package io.github.mksfilmoteka.media.catalog

import io.github.mksfilmoteka.media.file.FileService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.BackOff
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class PersonDeletedListener(
    private val jsonMapper: JsonMapper,
    private val fileService: FileService,
) {

    private val log = LoggerFactory.getLogger(PersonDeletedListener::class.java)

    @RetryableTopic(
        backOff = BackOff(delayString = $$"${app.kafka.retry-delay}", multiplier = 2.0),
        retryTopicSuffix = ".media.retry",
        dltTopicSuffix = ".media.dlt",
        autoStartDltHandler = "false"
    )
    @KafkaListener(
        topics = [$$"${app.kafka.topics.person-deleted.name}"],
        groupId = $$"${spring.kafka.consumer.group-id}"
    )
    fun onPersonDeleted(payload: String) {
        val event = requireNotNull(
            jsonMapper.readValue(payload, PersonDeletedEvent::class.java)
        )

        val photoName = event.photoName ?: return

        require(photoName.isNotBlank()) {
            "PersonDeletedEvent photoName must not be blank"
        }

        fileService.delete(photoName)
        log.info("Processed person deletion eventId={}, personId={}", event.eventId, event.personId)
    }
}