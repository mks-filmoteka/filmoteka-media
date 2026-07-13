package io.github.mksfilmoteka.media.file

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SupportedImageTypeTest {

    @Test
    fun `should resolve image type when extension is supported with any casing`() {
        assertEquals(SupportedImageType.JPEG, SupportedImageType.fromExtension("jpg"))
        assertEquals(SupportedImageType.JPEG, SupportedImageType.fromExtension("JPEG"))
        assertEquals(SupportedImageType.PNG, SupportedImageType.fromExtension("PnG"))
        assertEquals(SupportedImageType.WEBP, SupportedImageType.fromExtension("WeBp"))
    }

    @Test
    fun `should return null when extension is unsupported`() {
        assertNull(SupportedImageType.fromExtension("gif"))
    }

    @Test
    fun `should resolve image type when content type is supported with any casing`() {
        assertEquals(SupportedImageType.JPEG, SupportedImageType.fromContentType("image/jpeg"))
        assertEquals(SupportedImageType.JPEG, SupportedImageType.fromContentType("IMAGE/JPEG"))
        assertEquals(SupportedImageType.PNG, SupportedImageType.fromContentType("Image/Png"))
        assertEquals(SupportedImageType.WEBP, SupportedImageType.fromContentType("Image/WebP"))
    }

    @Test
    fun `should return null when content type is unsupported`() {
        assertNull(SupportedImageType.fromContentType("image/gif"))
    }

    @Test
    fun `should expose expected spring media types for supported image types`() {
        assertEquals(MediaType.IMAGE_JPEG, SupportedImageType.JPEG.mediaType)
        assertEquals(MediaType.IMAGE_PNG, SupportedImageType.PNG.mediaType)
        assertEquals(MediaType.parseMediaType(IMAGE_WEBP_VALUE), SupportedImageType.WEBP.mediaType)
    }
}
