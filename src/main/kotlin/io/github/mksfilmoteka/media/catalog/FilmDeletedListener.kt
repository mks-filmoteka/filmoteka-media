package io.github.mksfilmoteka.media.catalog

import io.github.mksfilmoteka.media.file.FileService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper

@Component
class FilmDeletedListener(
    private val jsonMapper: JsonMapper,
    private val fileService: FileService,
) {

    private val log = LoggerFactory.getLogger(FilmDeletedListener::class.java)

    @KafkaListener(topics = [$$"${app.kafka.topics.film-deleted.name}"])
    fun onFilmDeleted(payload: String) {
        val event = requireNotNull(
            jsonMapper.readValue(payload, FilmDeletedEvent::class.java)
        )

        val posterName = event.posterName ?: return

        require(posterName.isNotBlank()) {
            "FilmDeletedEvent posterName must not be blank"
        }

        fileService.delete(posterName)
        log.info("Processed film deletion eventId={}, filmId={}", event.eventId, event.filmId)
    }
}