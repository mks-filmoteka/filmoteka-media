package io.github.mksfilmoteka.media.exception

import jakarta.servlet.http.HttpServletRequest
import net.coobird.thumbnailator.tasks.UnsupportedFormatException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        log.warn("Invalid request {} {}: {}", request.method, request.requestURI, ex.message ?: "Invalid request")

        return ResponseEntity.status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    message = ex.message ?: "Invalid request",
                    path = request.requestURI,
                    code = ErrorCode.INVALID_REQUEST
                )
            )
    }

    @ExceptionHandler(UnsupportedFormatException::class)
    fun handleUnsupportedImageFormat(
        ex: UnsupportedFormatException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        log.warn("Invalid image upload {} {}: {}", request.method, request.requestURI, ex.message)

        return ResponseEntity.status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    message = "Unsupported or invalid image file",
                    path = request.requestURI,
                    code = ErrorCode.INVALID_REQUEST
                )
            )
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        ex: ResourceNotFoundException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        log.info("Resource not found {} {}: {}", request.method, request.requestURI, ex.message ?: "Resource not found")

        return ResponseEntity.status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    message = ex.message ?: "Resource not found",
                    path = request.requestURI,
                    code = ErrorCode.RESOURCE_NOT_FOUND
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        log.error("Unexpected error {} {}", request.method, request.requestURI, ex)

        return ResponseEntity.status(status)
            .body(
                ErrorResponse(
                    status = status.value(),
                    message = "Unexpected error occurred",
                    path = request.requestURI,
                    code = ErrorCode.INTERNAL_ERROR
                )
            )
    }
}