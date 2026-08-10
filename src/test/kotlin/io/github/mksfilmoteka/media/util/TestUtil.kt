package io.github.mksfilmoteka.media.util

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.request.RequestPostProcessor

object TestUtil {

    fun adminJwt(): RequestPostProcessor = jwt().authorities(SimpleGrantedAuthority("ROLE_ADMIN"))

    fun testJwt(claims: Map<String, Any>): Jwt {
        return Jwt.withTokenValue("token")
            .header("alg", "none")
            .claims { existingClaims -> existingClaims.putAll(claims) }
            .build()
    }

    fun imageBytes(format: String, width: Int = 100, height: Int = 100): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = Color.RED
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()

        return ByteArrayOutputStream().use { outputStream ->
            require(ImageIO.write(image, format, outputStream)) { "Unsupported image format: $format" }
            outputStream.toByteArray()
        }
    }

    fun clearDirectory(directory: Path) {
        Files.createDirectories(directory)
        Files.list(directory).use { paths ->
            paths.forEach { Files.deleteIfExists(it) }
        }
    }
}
