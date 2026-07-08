package io.github.mksfilmoteka.media.exception

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Standard API error response")
data class ErrorResponse(
    @field:Schema(description = "Timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now(),

    @field:Schema(description = "HTTP status code", example = "400")
    val status: Int,

    @field:Schema(description = "Human-readable error message", example = "Unsupported file extension: gif")
    val message: String,

    @field:Schema(description = "Request path that failed", example = "/api/v1/media/files/poster.gif")
    val path: String,

    @field:Schema(description = "Application error code")
    val code: ErrorCode,
)
