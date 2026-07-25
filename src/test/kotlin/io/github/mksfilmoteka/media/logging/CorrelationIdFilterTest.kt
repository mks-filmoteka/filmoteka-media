package io.github.mksfilmoteka.media.logging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class CorrelationIdFilterTest {

    private val filter = CorrelationIdFilter()

    @Test
    fun `should generate correlation id when header is missing`() {
        val request = MockHttpServletRequest("GET", "/api/v1/media/files/test.jpg")
        val response = MockHttpServletResponse()
        val correlationIdInsideChain = AtomicReference<String>()

        filter.doFilter(request, response) { _, _ ->
            correlationIdInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
        }

        val responseCorrelationId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)

        assertThat(responseCorrelationId).isNotBlank()
        assertThat(UUID.fromString(responseCorrelationId)).isNotNull()
        assertThat(correlationIdInsideChain.get()).isEqualTo(responseCorrelationId)
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull()
    }

    @Test
    fun `should use existing correlation id from header`() {
        val request = MockHttpServletRequest("GET", "/api/v1/media/files/test.jpg")
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "test-123")

        val response = MockHttpServletResponse()
        val correlationIdInsideChain = AtomicReference<String>()

        filter.doFilter(request, response) { _, _ ->
            correlationIdInsideChain.set(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY))
        }

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("test-123")
        assertThat(correlationIdInsideChain.get()).isEqualTo("test-123")
        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull()
    }
}