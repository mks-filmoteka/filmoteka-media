package io.github.mksfilmoteka.media.file

import io.github.mksfilmoteka.media.config.MediaProperties
import io.github.mksfilmoteka.media.exception.ResourceNotFoundException
import io.github.mksfilmoteka.media.util.TestUtil.imageBytes
import net.coobird.thumbnailator.tasks.UnsupportedFormatException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalFileServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var service: LocalFileService

    @BeforeEach
    fun setUp() {
        service = LocalFileService(MediaProperties(tempDir.toString()))
    }

    @Test
    fun `should store resized image and return public URL when uploading valid jpeg`() {
        val file = MockMultipartFile(
            "file",
            "poster.jpg",
            "image/jpeg",
            imageBytes("jpg", width = 800, height = 1200),
        )

        val response = service.upload(file)

        UUID.fromString(response.fileName.substringBeforeLast('.'))
        assertEquals("jpg", response.fileName.substringAfterLast('.'))
        assertEquals("/api/v1/media/files/${response.fileName}", response.url)

        val storedFile = tempDir.resolve(response.fileName)
        assertTrue(Files.exists(storedFile))

        val storedImage = ImageIO.read(storedFile.toFile())
        assertEquals(300, storedImage.width)
        assertEquals(450, storedImage.height)
    }

    @Test
    fun `should store image with png extension when uploading valid png`() {
        val file = MockMultipartFile(
            "file",
            "poster.png",
            "image/png",
            imageBytes("png", width = 200, height = 200),
        )

        val response = service.upload(file)

        assertEquals("png", response.fileName.substringAfterLast('.'))
        assertTrue(Files.exists(tempDir.resolve(response.fileName)))
    }

    @Test
    fun `should throw illegal argument when uploading empty file`() {
        val file = MockMultipartFile("file", "poster.jpg", "image/jpeg", ByteArray(0))

        val exception = assertFailsWith<IllegalArgumentException> {
            service.upload(file)
        }

        assertEquals("File must not be empty", exception.message)
    }

    @Test
    fun `should throw expected validation error when upload metadata is invalid`() {
        val imageBytes = imageBytes("jpg")

        val cases = listOf(
            UploadCase(MockMultipartFile("file", "", "image/jpeg", imageBytes), "Original filename must not be blank"),
            UploadCase(MockMultipartFile("file", "poster", "image/jpeg", imageBytes), "File extension is missing"),
            UploadCase(
                MockMultipartFile("file", "poster.gif", "image/jpeg", imageBytes),
                "Unsupported file extension: gif",
            ),
            UploadCase(MockMultipartFile("file", "poster.jpg", null, imageBytes), "Content type must not be blank"),
            UploadCase(
                MockMultipartFile("file", "poster.jpg", "image/gif", imageBytes),
                "Unsupported content type: image/gif",
            ),
            UploadCase(
                MockMultipartFile("file", "poster.jpg", "image/png", imageBytes),
                "File extension .jpg does not match content type image/png",
            ),
        )

        cases.forEach { case ->
            val exception = assertFailsWith<IllegalArgumentException> {
                service.upload(case.file)
            }
            assertEquals(case.expectedMessage, exception.message)
        }
    }

    @Test
    fun `should throw unsupported format when uploaded bytes are not an image`() {
        val file = MockMultipartFile("file", "poster.jpg", "image/jpeg", "not an image".toByteArray())

        assertFailsWith<UnsupportedFormatException> {
            service.upload(file)
        }
    }

    @Test
    fun `should throw illegal argument when image bytes do not match declared image type`() {
        val file = MockMultipartFile("file", "poster.jpg", "image/jpeg", imageBytes("png"))

        val exception = assertFailsWith<IllegalArgumentException> {
            service.upload(file)
        }

        assertEquals("File content does not match content type image/jpeg", exception.message)
    }

    @Test
    fun `should return readable resource when loading existing file`() {
        Files.writeString(tempDir.resolve("poster.jpg"), "image")

        val resource = service.load("poster.jpg")

        assertTrue(resource.exists())
        assertTrue(resource.isReadable)
        assertEquals("image", resource.inputStream.bufferedReader().use { it.readText() })
    }

    @Test
    fun `should throw illegal argument when loading path outside root location`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            service.load("../outside.jpg")
        }

        assertEquals("Invalid file path", exception.message)
    }

    @Test
    fun `should throw resource not found when loading missing file`() {
        val exception = assertFailsWith<ResourceNotFoundException> {
            service.load("missing.jpg")
        }

        assertEquals("File not found: missing.jpg", exception.message)
    }

    @Test
    fun `should remove file when deleting existing file`() {
        val file = tempDir.resolve("poster.jpg")
        Files.writeString(file, "image")

        service.delete("poster.jpg")

        assertFalse(Files.exists(file))
    }

    @Test
    fun `should not fail when deleting missing file`() {
        service.delete("missing.jpg")

        assertFalse(Files.exists(tempDir.resolve("missing.jpg")))
    }

    @Test
    fun `should throw illegal argument when deleting path outside root location`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            service.delete("../outside.jpg")
        }

        assertEquals("Invalid file path", exception.message)
    }

    private data class UploadCase(
        val file: MockMultipartFile,
        val expectedMessage: String,
    )
}
