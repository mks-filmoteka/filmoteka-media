package io.github.mksfilmoteka.media.catalog

import io.github.mksfilmoteka.media.file.FileService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.BackOff
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class PersonPhotoChangedListener(
    private val jsonMapper: JsonMapper,
    private val fileService: FileService,
) {

    private val log = LoggerFactory.getLogger(PersonPhotoChangedListener::class.java)

    @RetryableTopic(
        backOff = BackOff(delayString = $$"${app.kafka.retry-delay}", multiplier = 2.0),
        retryTopicSuffix = ".media.retry",
        dltTopicSuffix = ".media.dlt",
        autoStartDltHandler = "false"
    )
    @KafkaListener(
        topics = [$$"${app.kafka.topics.person-photo-changed.name}"],
        groupId = $$"${spring.kafka.consumer.group-id}"
    )
    fun onPersonPhotoChanged(payload: String) {
        val event = requireNotNull(
            jsonMapper.readValue(payload, PersonPhotoChangedEvent::class.java)
        )

        val oldPhotoName = event.oldPhotoName ?: return

        if (oldPhotoName == event.newPhotoName) {
            return
        }

        require(oldPhotoName.isNotBlank()) {
            "PersonPhotoChangedEvent oldPhotoName must not be blank"
        }

        fileService.delete(oldPhotoName)
        log.info("Processed photo change eventId={}, personId={}", event.eventId, event.personId)
    }
}