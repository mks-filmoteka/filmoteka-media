package io.github.mksfilmoteka.media.file

import org.springframework.http.MediaType

enum class SupportedImageType(
    val extensions: Set<String>,
    val contentType: String,
    val mediaType: MediaType
) {
    JPEG(setOf("jpg", "jpeg"), "image/jpeg", MediaType.IMAGE_JPEG),
    PNG(setOf("png"), "image/png", MediaType.IMAGE_PNG);

    companion object {
        fun fromExtension(extension: String): SupportedImageType? {
            return entries.firstOrNull { extension.lowercase() in it.extensions }
        }

        fun fromContentType(contentType: String): SupportedImageType? {
            return entries.firstOrNull { it.contentType.equals(contentType, true) }
        }
    }
}