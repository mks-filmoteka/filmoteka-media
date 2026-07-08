package io.github.mksfilmoteka.media.config

import jakarta.validation.Validation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MediaPropertiesTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `should use expected defaults when properties are not configured`() {
        val properties = MediaProperties()

        assertEquals("uploads", properties.rootLocation)
        assertEquals(300, properties.image.maxWidth)
        assertEquals(450, properties.image.maxHeight)
        assertEquals(0.9, properties.image.outputQuality)
    }

    @Test
    fun `should report violations when root location is blank and dimensions are not positive`() {
        val properties =
            MediaProperties("", ImageProperties(0, 0, 0.9))

        val paths = violationPaths(properties)

        assertTrue("rootLocation" in paths)
        assertTrue("image.maxWidth" in paths)
        assertTrue("image.maxHeight" in paths)
    }

    @Test
    fun `should report violation when output quality is below minimum`() {
        val properties = MediaProperties(image = ImageProperties(outputQuality = 0.0))

        val paths = violationPaths(properties)

        assertTrue("image.outputQuality" in paths)
    }

    @Test
    fun `should report violation when output quality is above maximum`() {
        val properties = MediaProperties(image = ImageProperties(outputQuality = 1.1))

        val paths = violationPaths(properties)

        assertTrue("image.outputQuality" in paths)
    }

    private fun violationPaths(properties: MediaProperties): Set<String> {
        return validator.validate(properties)
            .map { it.propertyPath.toString() }
            .toSet()
    }
}
