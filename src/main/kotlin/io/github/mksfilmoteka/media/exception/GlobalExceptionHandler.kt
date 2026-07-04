package io.github.mksfilmoteka.media.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status)
            .body(ErrorResponse(
                    status = status.value(),
                    message = ex.message ?: "Invalid request",
                    path = request.requestURI,
                    code = ErrorCode.INVALID_REQUEST
                )
            )
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(
        ex: ResourceNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
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
        ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
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