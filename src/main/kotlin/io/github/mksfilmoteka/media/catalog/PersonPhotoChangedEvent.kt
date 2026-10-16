package io.github.mksfilmoteka.media.catalog

import java.time.Instant
import java.util.*

data class PersonPhotoChangedEvent(
    val eventId: UUID,
    val personId: Long,
    val oldPhotoName: String?,
    val newPhotoName: String?,
    val occurredAt: Instant,
)
