package io.github.mksfilmoteka.media.file

import io.github.mksfilmoteka.media.config.MediaProperties
import io.github.mksfilmoteka.media.exception.ResourceNotFoundException
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.tasks.UnsupportedFormatException
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO

@Service
class LocalFileService(private val mediaProperties: MediaProperties) : FileService {

    private val rootLocation: Path = Path.of(mediaProperties.rootLocation).toAbsolutePath().normalize()

    init {
        Files.createDirectories(rootLocation)
    }

    override fun upload(file: MultipartFile): UploadFileResponse {
        require(!file.isEmpty) { "File must not be empty" }

        val imageFile = validateImageFile(file.originalFilename, file.contentType)
        val extension = imageFile.extension
        val fileName = "${UUID.randomUUID()}.$extension"
        val targetLocation = rootLocation.resolve(fileName)

        file.inputStream.use { inputStream ->
            val typeByContent = detectImageType(inputStream)
            require(typeByContent == imageFile.type) {
                "File content does not match content type ${file.contentType}"
            }
        }

        file.inputStream.use { inputStream ->
            Thumbnails.of(inputStream)
                .size(mediaProperties.image.maxWidth, mediaProperties.image.maxHeight)
                .outputQuality(mediaProperties.image.outputQuality)
                .toFile(targetLocation.toFile())
        }

        return UploadFileResponse(fileName, "/api/v1/media/files/$fileName")
    }

    override fun load(fileName: String): Resource {
        val filePath = rootLocation.resolve(fileName).normalize()
        require(filePath.startsWith(rootLocation)) { "Invalid file path" }
        val resource = UrlResource(filePath.toUri())

        if (!resource.exists() || !resource.isReadable) {
            throw ResourceNotFoundException("File not found: $fileName")
        }

        return resource
    }

    override fun delete(fileName: String) {
        val filePath = rootLocation.resolve(fileName).normalize()
        require(filePath.startsWith(rootLocation)) { "Invalid file path" }
        Files.deleteIfExists(filePath)
    }

    private fun validateImageFile(originalFilename: String?, contentType: String?): ImageFile {
        require(!originalFilename.isNullOrBlank()) { "Original filename must not be blank" }
        require(!contentType.isNullOrBlank()) { "Content type must not be blank" }

        val extension = originalFilename.substringAfterLast('.', "").lowercase()
        require(extension.isNotBlank()) { "File extension is missing" }

        val typeByExtension = SupportedImageType.fromExtension(extension)
            ?: throw IllegalArgumentException("Unsupported file extension: $extension")
        val typeByContentType = SupportedImageType.fromContentType(contentType)
            ?: throw IllegalArgumentException("Unsupported content type: $contentType")

        require(typeByExtension == typeByContentType) {
            "File extension .$extension does not match content type $contentType"
        }

        return ImageFile(extension, typeByExtension)
    }

    private fun detectImageType(inputStream: InputStream): SupportedImageType {
        val imageInputStream = ImageIO.createImageInputStream(inputStream)
            ?: throw UnsupportedFormatException(UnsupportedFormatException.UNKNOWN)

        imageInputStream.use {
            val readers = ImageIO.getImageReaders(it)
            if (!readers.hasNext()) {
                throw UnsupportedFormatException(UnsupportedFormatException.UNKNOWN)
            }

            val reader = readers.next()
            return try {
                SupportedImageType.fromExtension(reader.formatName)
                    ?: throw UnsupportedFormatException(reader.formatName)
            } finally {
                reader.dispose()
            }
        }
    }

    private data class ImageFile(
        val extension: String,
        val type: SupportedImageType,
    )
}
