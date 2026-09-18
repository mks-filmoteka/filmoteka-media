package io.github.mksfilmoteka.media.catalog

import java.time.Instant
import java.util.*

data class FilmPosterChangedEvent(
    val eventId: UUID,
    val filmId: Long,
    val oldPosterName: String?,
    val newPosterName: String?,
    val occurredAt: Instant,
)
