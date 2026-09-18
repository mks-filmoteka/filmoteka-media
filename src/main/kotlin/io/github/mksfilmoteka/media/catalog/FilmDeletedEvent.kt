package io.github.mksfilmoteka.media.catalog

import java.time.Instant
import java.util.*

data class FilmDeletedEvent(
    val eventId: UUID,
    val filmId: Long,
    val posterName: String?,
    val occurredAt: Instant
)
