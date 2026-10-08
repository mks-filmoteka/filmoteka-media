package io.github.mksfilmoteka.media.exception

import jakarta.servlet.http.HttpServletRequest
import net.coobird.thumbnailator.tasks.UnsupportedFormatException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

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

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val servletRequest = (request as ServletWebRequest).request
        val status = HttpStatus.valueOf(statusCode.value())

        if (status.is5xxServerError) {
            log.error("Unexpected error {} {}", servletRequest.method, servletRequest.requestURI, ex)
        } else {
            log.warn(
                "Request rejected {} {}: status={}, message={}",
                servletRequest.method, servletRequest.requestURI, status.value(), ex.message
            )
        }

        val errorResponse = ErrorResponse(
            status = status.value(),
            message = resolveMessage(ex, status),
            path = servletRequest.requestURI,
            code = resolveErrorCode(status)
        )
        return super.handleExceptionInternal(ex, errorResponse, headers, statusCode, request)
    }

    private fun resolveMessage(ex: Exception, status: HttpStatus): String {
        if (status.is5xxServerError) {
            return "Unexpected error occurred"
        }
        val detail = (ex as? org.springframework.web.ErrorResponse)?.body?.detail
        return detail ?: status.reasonPhrase
    }

    private fun resolveErrorCode(status: HttpStatus): ErrorCode = when {
        status == HttpStatus.NOT_FOUND -> ErrorCode.RESOURCE_NOT_FOUND
        status == HttpStatus.METHOD_NOT_ALLOWED -> ErrorCode.METHOD_NOT_ALLOWED
        status == HttpStatus.UNSUPPORTED_MEDIA_TYPE -> ErrorCode.UNSUPPORTED_MEDIA_TYPE
        status.value() == 413 -> ErrorCode.FILE_TOO_LARGE
        status.is5xxServerError -> ErrorCode.INTERNAL_ERROR
        else -> ErrorCode.INVALID_REQUEST
    }
}