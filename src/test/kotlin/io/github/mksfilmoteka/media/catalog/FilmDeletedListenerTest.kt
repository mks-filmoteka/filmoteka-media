package io.github.mksfilmoteka.media.catalog

import io.github.mksfilmoteka.media.file.FileService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class FilmDeletedListenerTest {

    private val jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()
    private val fileService = mock(FileService::class.java)
    private val listener = FilmDeletedListener(jsonMapper, fileService)

    @Test
    fun `should delete poster from film deletion event`() {
        listener.onFilmDeleted(payload("poster.jpg"))

        verify(fileService).delete("poster.jpg")
        verifyNoMoreInteractions(fileService)
    }

    @Test
    fun `should skip cleanup when deleted film has no poster`() {
        listener.onFilmDeleted(payload(null))

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should reject blank poster name without deleting files`() {
        assertFailsWith<IllegalArgumentException> {
            listener.onFilmDeleted(payload("   "))
        }

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should reject malformed JSON without deleting files`() {
        assertFailsWith<JacksonException> {
            listener.onFilmDeleted("not json")
        }

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should reject null event without deleting files`() {
        assertFailsWith<IllegalArgumentException> {
            listener.onFilmDeleted("null")
        }

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should propagate deletion failure so Kafka can retry`() {
        val failure = IOException("Storage unavailable")
        doAnswer { throw failure }.`when`(fileService).delete("poster.jpg")

        val exception = assertFailsWith<IOException> {
            listener.onFilmDeleted(payload("poster.jpg"))
        }

        assertSame(failure, exception)
    }

    private fun payload(posterName: String?): String = """
        {
          "eventId": "00000000-0000-0000-0000-000000000001",
          "filmId": 42,
          "posterName": ${jsonMapper.writeValueAsString(posterName)},
          "occurredAt": "2026-09-19T12:00:00Z"
        }
    """.trimIndent()
}
