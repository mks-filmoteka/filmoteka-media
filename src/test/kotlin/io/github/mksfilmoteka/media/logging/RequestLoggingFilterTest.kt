package io.github.mksfilmoteka.media.logging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestLoggingFilterTest {

    private val filter = RequestLoggingFilter()

    @Test
    fun `should create request log with path without query string`() {
        val request = MockHttpServletRequest("GET", "/api/v1/media/files/test.jpg")
        val response = MockHttpServletResponse()
        response.status = 200

        val requestLog = filter.createRequestLog(request, response, 42)

        assertThat(requestLog.method).isEqualTo("GET")
        assertThat(requestLog.path).isEqualTo("/api/v1/media/files/test.jpg")
        assertThat(requestLog.status).isEqualTo(200)
        assertThat(requestLog.durationMs).isEqualTo(42)
    }

    @Test
    fun `should create request log with path and query string`() {
        val request = MockHttpServletRequest("GET", "/api/v1/media/files/test.jpg")
        request.queryString = "download=true"

        val response = MockHttpServletResponse()
        response.status = 200

        val requestLog = filter.createRequestLog(request, response, 42)

        assertThat(requestLog.method).isEqualTo("GET")
        assertThat(requestLog.path).isEqualTo("/api/v1/media/files/test.jpg?download=true")
        assertThat(requestLog.status).isEqualTo(200)
        assertThat(requestLog.durationMs).isEqualTo(42)
    }
}