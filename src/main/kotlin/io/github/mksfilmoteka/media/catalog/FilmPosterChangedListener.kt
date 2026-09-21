package io.github.mksfilmoteka.media.catalog

import io.github.mksfilmoteka.media.file.FileService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.BackOff
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.RetryableTopic
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class FilmPosterChangedListener(
    private val jsonMapper: JsonMapper,
    private val fileService: FileService,
) {

    private val log = LoggerFactory.getLogger(FilmPosterChangedListener::class.java)

    @RetryableTopic(
        backOff = BackOff(delayString = $$"${app.kafka.retry-delay}", multiplier = 2.0),
        retryTopicSuffix = ".media.retry",
        dltTopicSuffix = ".media.dlt",
        autoStartDltHandler = "false"
    )
    @KafkaListener(
        topics = [$$"${app.kafka.topics.film-poster-changed.name}"],
        groupId = $$"${spring.kafka.consumer.group-id}"
    )
    fun onFilmPosterChanged(payload: String) {
        val event = requireNotNull(
            jsonMapper.readValue(payload, FilmPosterChangedEvent::class.java)
        )

        val oldPosterName = event.oldPosterName ?: return

        if (oldPosterName == event.newPosterName) {
            return
        }

        require(oldPosterName.isNotBlank()) {
            "FilmPosterChangedEvent oldPosterName must not be blank"
        }

        fileService.delete(oldPosterName)
        log.info("Processed poster change eventId={}, filmId={}", event.eventId, event.filmId)
    }
}