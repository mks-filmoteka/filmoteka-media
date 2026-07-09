package io.github.mksfilmoteka.media.util

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

object TestUtil {

    fun imageBytes(format: String, width: Int = 100, height: Int = 100): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = Color.RED
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()

        return ByteArrayOutputStream().use { outputStream ->
            ImageIO.write(image, format, outputStream)
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
