package io.github.mksfilmoteka.media.catalog

import io.github.mksfilmoteka.media.file.FileService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class PersonPhotoChangedListenerTest {

    private val jsonMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()
    private val fileService = mock(FileService::class.java)
    private val listener = PersonPhotoChangedListener(jsonMapper, fileService)

    @Test
    fun `should delete only old photo when photo is replaced`() {
        listener.onPersonPhotoChanged(payload("old.jpg", "new.jpg"))

        verify(fileService).delete("old.jpg")
        verifyNoMoreInteractions(fileService)
    }

    @Test
    fun `should delete old photo when photo is removed`() {
        listener.onPersonPhotoChanged(payload("old.jpg", null))

        verify(fileService).delete("old.jpg")
        verifyNoMoreInteractions(fileService)
    }

    @Test
    fun `should skip cleanup when person had no photo`() {
        listener.onPersonPhotoChanged(payload(null, "new.jpg"))

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should keep photo when its name is unchanged`() {
        listener.onPersonPhotoChanged(payload("photo.jpg", "photo.jpg"))

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should reject blank old photo name without deleting files`() {
        assertFailsWith<IllegalArgumentException> {
            listener.onPersonPhotoChanged(payload("   ", "new.jpg"))
        }

        verifyNoInteractions(fileService)
    }

    @Test
    fun `should propagate deletion failure so Kafka can retry`() {
        val failure = IOException("Storage unavailable")
        doAnswer { throw failure }.`when`(fileService).delete("old.jpg")

        val exception = assertFailsWith<IOException> {
            listener.onPersonPhotoChanged(payload("old.jpg", "new.jpg"))
        }

        assertSame(failure, exception)
    }

    private fun payload(oldPhotoName: String?, newPhotoName: String?): String = """
        {
          "eventId": "00000000-0000-0000-0000-000000000001",
          "personId": 42,
          "oldPhotoName": ${jsonMapper.writeValueAsString(oldPhotoName)},
          "newPhotoName": ${jsonMapper.writeValueAsString(newPhotoName)},
          "occurredAt": "2026-09-19T12:00:00Z"
        }
    """.trimIndent()
}
