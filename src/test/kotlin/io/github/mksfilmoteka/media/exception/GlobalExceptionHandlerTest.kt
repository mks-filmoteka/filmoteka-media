package io.github.mksfilmoteka.media.exception

import io.github.mksfilmoteka.media.auth.KeycloakRealmRoleConverter
import io.github.mksfilmoteka.media.auth.SecurityConfig
import io.github.mksfilmoteka.media.file.FileController
import io.github.mksfilmoteka.media.file.FileService
import io.github.mksfilmoteka.media.util.TestUtil.adminJwt
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.multipart.MaxUploadSizeExceededException

@WebMvcTest(FileController::class)
@Import(SecurityConfig::class, KeycloakRealmRoleConverter::class)
class GlobalExceptionHandlerTest {

    @MockitoBean
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `should return not found for unknown path`() {
        mockMvc.perform(get("/api/v1/unknown"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/v1/unknown"))
            .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name))
    }

    @Test
    fun `should return method not allowed for unsupported method`() {
        mockMvc.perform(put("/api/v1/media/files/poster.jpg").with(adminJwt()))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.code").value(ErrorCode.METHOD_NOT_ALLOWED.name))
    }

    @Test
    fun `should return bad request when file part is missing`() {
        mockMvc.perform(multipart("/api/v1/media/files").with(adminJwt()))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.name))
    }

    @Test
    fun `should return content too large when upload exceeds size limit`() {
        val file = MockMultipartFile("file", "poster.jpg", "image/jpeg", "image".toByteArray())
        doAnswer {
            throw MaxUploadSizeExceededException(5L * 1024 * 1024)
        }.`when`(fileService).upload(file)

        mockMvc.perform(multipart("/api/v1/media/files").file(file).with(adminJwt()))
            .andExpect(status().`is`(413))
            .andExpect(jsonPath("$.status").value(413))
            .andExpect(jsonPath("$.path").value("/api/v1/media/files"))
            .andExpect(jsonPath("$.code").value(ErrorCode.FILE_TOO_LARGE.name))
    }
}
