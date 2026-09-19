package io.github.mksfilmoteka.media.catalog

import io.github.mksfilmoteka.media.file.FileService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class FilmPosterChangedListenerTest {

    private val jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()
    private val fileService = mock(FileService::class.java)
    private val listener = FilmPosterChangedListener(jsonMapper, fileService)

    @Test
    fun `should delete only old poster when poster is replaced`() {
        listener.onFilmPosterChanged(payload("old.jpg", "new.jpg"))

        verify(fileService).delete("old.jpg")
        verifyNoMoreInteractions(fileService)
    }

    @Test
    fun `should delete old poster when poster is removed`() {
        listener.onFilmPosterChanged(payload("old.jpg", null))

        verify(fileService).delete("old.jpg")
        verifyNoMoreInteractions(fileService)
    }

    @Test
    fun `should skip cleanup when film had no poster`() {
        listener.onFilmPosterChanged(payload(null, "new.jpg"))

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should keep poster when its name is unchanged`() {
        listener.onFilmPosterChanged(payload("poster.jpg", "poster.jpg"))

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should reject blank old poster name without deleting files`() {
        assertFailsWith<IllegalArgumentException> {
            listener.onFilmPosterChanged(payload("   ", "new.jpg"))
        }

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should propagate deletion failure so Kafka can retry`() {
        val failure = IOException("Storage unavailable")
        doAnswer { throw failure }.`when`(fileService).delete("old.jpg")

        val exception = assertFailsWith<IOException> {
            listener.onFilmPosterChanged(payload("old.jpg", "new.jpg"))
        }

        assertSame(failure, exception)
    }

    private fun payload(oldPosterName: String?, newPosterName: String?): String = """
        {
          "eventId": "00000000-0000-0000-0000-000000000001",
          "filmId": 42,
          "oldPosterName": ${jsonMapper.writeValueAsString(oldPosterName)},
          "newPosterName": ${jsonMapper.writeValueAsString(newPosterName)},
          "occurredAt": "2026-09-19T12:00:00Z"
        }
    """.trimIndent()
}
