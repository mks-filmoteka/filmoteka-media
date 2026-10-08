package io.github.mksfilmoteka.media.exception

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Application error code")
enum class ErrorCode {
    INVALID_REQUEST,
    RESOURCE_NOT_FOUND,
    METHOD_NOT_ALLOWED,
    UNSUPPORTED_MEDIA_TYPE,
    FILE_TOO_LARGE,
    INTERNAL_ERROR
}
