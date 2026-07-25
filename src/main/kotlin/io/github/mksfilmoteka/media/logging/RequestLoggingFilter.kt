package io.github.mksfilmoteka.media.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.nanoTime()

        try {
            filterChain.doFilter(request, response)
        } finally {
            val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

            if (log.isInfoEnabled) {
                val requestLog = createRequestLog(request, response, durationMs)

                log.info(
                    "Request completed: method={}, path={}, status={}, durationMs={}",
                    requestLog.method,
                    requestLog.path,
                    requestLog.status,
                    requestLog.durationMs
                )
            }
        }
    }

    internal fun createRequestLog(request: HttpServletRequest, response: HttpServletResponse, durationMs: Long)
    : RequestLog {
        return RequestLog(request.method, getRequestPath(request), response.status, durationMs)
    }

    private fun getRequestPath(request: HttpServletRequest): String {
        val queryString = request.queryString
        if (queryString.isNullOrBlank()) {
            return request.requestURI
        }
        return "${request.requestURI}?$queryString"
    }

    internal data class RequestLog(val method: String, val path: String, val status: Int, val durationMs: Long)
}