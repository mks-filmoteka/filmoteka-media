package io.github.mksfilmoteka.media

import com.jayway.jsonpath.JsonPath
import io.github.mksfilmoteka.media.util.TestUtil.clearDirectory
import io.github.mksfilmoteka.media.util.TestUtil.imageBytes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(properties = ["media.root-location=build/tmp/integration-test-uploads"])
@AutoConfigureMockMvc
class FilmotekaMediaApplicationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        clearDirectory(mediaRoot)
    }

    @AfterEach
    fun clear() {
        clearDirectory(mediaRoot)
    }

    @Test
    fun `should upload load and delete image through http endpoints`() {
        val file = MockMultipartFile(
            "file",
            "poster.jpg",
            "image/jpeg",
            imageBytes("jpg", width = 800, height = 1200),
        )

        val uploadResult = mockMvc.perform(multipart("/api/v1/media/files").file(file))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.fileName").exists())
            .andExpect(jsonPath("$.url").exists())
            .andReturn()

        val responseBody = uploadResult.response.contentAsString
        val fileName = JsonPath.read<String>(responseBody, "$.fileName")
        val url = JsonPath.read<String>(responseBody, "$.url")

        UUID.fromString(fileName.substringBeforeLast('.'))
        assertEquals("jpg", fileName.substringAfterLast('.'))
        assertEquals("/api/v1/media/files/$fileName", url)
        assertTrue(Files.exists(mediaRoot.resolve(fileName)))

        val loadedBytes = mockMvc.perform(get(url))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andReturn()
            .response
            .contentAsByteArray

        val loadedImage = ImageIO.read(ByteArrayInputStream(loadedBytes))
        assertNotNull(loadedImage)
        assertEquals(300, loadedImage.width)
        assertEquals(450, loadedImage.height)

        mockMvc.perform(delete(url))
            .andExpect(status().isNoContent)

        assertFalse(Files.exists(mediaRoot.resolve(fileName)))

        mockMvc.perform(get(url))
            .andExpect(status().isNotFound)
    }

    private companion object {
        val mediaRoot: Path = Path.of("build/tmp/integration-test-uploads").toAbsolutePath().normalize()
    }
}
