package io.github.mksfilmoteka.media.catalog

import java.time.Instant
import java.util.*

data class PersonDeletedEvent(
    val eventId: UUID,
    val personId: Long,
    val photoName: String?,
    val occurredAt: Instant
)
