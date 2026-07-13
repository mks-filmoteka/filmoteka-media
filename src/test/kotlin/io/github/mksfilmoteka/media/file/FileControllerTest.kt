package io.github.mksfilmoteka.media.file

import io.github.mksfilmoteka.media.exception.ErrorCode
import io.github.mksfilmoteka.media.exception.ResourceNotFoundException
import net.coobird.thumbnailator.tasks.UnsupportedFormatException
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(FileController::class)
class FileControllerTest {

    @MockitoBean
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return created response when uploading file`() {
        val file = MockMultipartFile(
            "file",
            "poster.jpg",
            "image/jpeg",
            "image".toByteArray()
        )
        `when`(fileService.upload(file))
            .thenReturn(UploadFileResponse("generated.jpg", "/api/v1/media/files/generated.jpg"))

        mockMvc.perform(multipart("/api/v1/media/files").file(file))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.fileName").value("generated.jpg"))
            .andExpect(jsonPath("$.url").value("/api/v1/media/files/generated.jpg"))

        verify(fileService).upload(file)
    }

    @Test
    fun `should return bad request when upload service rejects invalid image format`() {
        val file = MockMultipartFile(
            "file",
            "poster.gif",
            "image/gif",
            "image".toByteArray()
        )
        doAnswer {
            throw UnsupportedFormatException(UnsupportedFormatException.UNKNOWN)
        }.`when`(fileService).upload(file)

        mockMvc.perform(multipart("/api/v1/media/files").file(file))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Unsupported or invalid image file"))
            .andExpect(jsonPath("$.path").value("/api/v1/media/files"))
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.name))
    }

    @Test
    fun `should return jpeg content type when loading jpg file`() {
        val body = byteArrayOf(1, 2, 3)
        `when`(fileService.load("poster.jpg")).thenReturn(ByteArrayResource(body))

        mockMvc.perform(get("/api/v1/media/files/poster.jpg"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes(body))

        verify(fileService).load("poster.jpg")
    }

    @Test
    fun `should return png content type when loading png file`() {
        val body = byteArrayOf(4, 5, 6)
        `when`(fileService.load("poster.png")).thenReturn(ByteArrayResource(body))

        mockMvc.perform(get("/api/v1/media/files/poster.png"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(body))

        verify(fileService).load("poster.png")
    }

    @Test
    fun `should return webp content type when loading webp file`() {
        val body = byteArrayOf(7, 8, 9)
        `when`(fileService.load("poster.webp")).thenReturn(ByteArrayResource(body))

        mockMvc.perform(get("/api/v1/media/files/poster.webp"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(IMAGE_WEBP_VALUE))
            .andExpect(content().bytes(body))

        verify(fileService).load("poster.webp")
    }

    @Test
    fun `should return bad request without calling service when loading unsupported extension`() {
        mockMvc.perform(get("/api/v1/media/files/poster.gif"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Unsupported file extension: gif"))
            .andExpect(jsonPath("$.path").value("/api/v1/media/files/poster.gif"))
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.name))

        verify(fileService, never()).load(anyString())
    }

    @Test
    fun `should return not found when loaded file does not exist`() {
        `when`(fileService.load("missing.jpg"))
            .thenThrow(ResourceNotFoundException("File not found: missing.jpg"))

        mockMvc.perform(get("/api/v1/media/files/missing.jpg"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("File not found: missing.jpg"))
            .andExpect(jsonPath("$.path").value("/api/v1/media/files/missing.jpg"))
            .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name))
    }

    @Test
    fun `should return internal error without leaking details when load service fails unexpectedly`() {
        `when`(fileService.load("poster.jpg"))
            .thenThrow(RuntimeException("disk details should not leak"))

        mockMvc.perform(get("/api/v1/media/files/poster.jpg"))
            .andExpect(status().isInternalServerError)
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.message").value("Unexpected error occurred"))
            .andExpect(jsonPath("$.path").value("/api/v1/media/files/poster.jpg"))
            .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_ERROR.name))
    }

    @Test
    fun `should return no content and delegate to service when deleting file`() {
        mockMvc.perform(delete("/api/v1/media/files/poster.jpg"))
            .andExpect(status().isNoContent)

        verify(fileService).delete("poster.jpg")
    }
}
