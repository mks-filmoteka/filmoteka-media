package io.github.mksfilmoteka.media.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val correlationId = resolveCorrelationId(request)

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)
        response.setHeader(CORRELATION_ID_HEADER, correlationId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY)
        }
    }

    private fun resolveCorrelationId(request: HttpServletRequest): String {
        return request.getHeader(CORRELATION_ID_HEADER)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
    }

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-Id"
        const val CORRELATION_ID_MDC_KEY = "correlationId"
    }
}
