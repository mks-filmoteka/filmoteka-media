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

class PersonDeletedListenerTest {

    private val jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()
    private val fileService = mock(FileService::class.java)
    private val listener = PersonDeletedListener(jsonMapper, fileService)

    @Test
    fun `should delete photo from person deletion event`() {
        listener.onPersonDeleted(payload("photo.jpg"))

        verify(fileService).delete("photo.jpg")
        verifyNoMoreInteractions(fileService)
    }

    @Test
    fun `should skip cleanup when deleted person has no photo`() {
        listener.onPersonDeleted(payload(null))

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should reject blank photo name without deleting files`() {
        assertFailsWith<IllegalArgumentException> {
            listener.onPersonDeleted(payload("   "))
        }

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should reject malformed JSON without deleting files`() {
        assertFailsWith<JacksonException> {
            listener.onPersonDeleted("not json")
        }

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should reject null event without deleting files`() {
        assertFailsWith<IllegalArgumentException> {
            listener.onPersonDeleted("null")
        }

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should propagate deletion failure so Kafka can retry`() {
        val failure = IOException("Storage unavailable")
        doAnswer { throw failure }.`when`(fileService).delete("photo.jpg")

        val exception = assertFailsWith<IOException> {
            listener.onPersonDeleted(payload("photo.jpg"))
        }

        assertSame(failure, exception)
    }

    private fun payload(photoName: String?): String = """
        {
          "eventId": "00000000-0000-0000-0000-000000000001",
          "personId": 42,
          "photoName": ${jsonMapper.writeValueAsString(photoName)},
          "occurredAt": "2026-09-19T12:00:00Z"
        }
    """.trimIndent()
}
