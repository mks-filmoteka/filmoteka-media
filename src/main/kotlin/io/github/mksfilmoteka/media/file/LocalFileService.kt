package io.github.mksfilmoteka.media.file

import io.github.mksfilmoteka.media.config.MediaProperties
import io.github.mksfilmoteka.media.exception.ResourceNotFoundException
import net.coobird.thumbnailator.Thumbnails
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Service
class LocalFileService(private val mediaProperties: MediaProperties) : FileService {

    private val rootLocation: Path = Path.of(mediaProperties.rootLocation).toAbsolutePath().normalize()

    init {
        Files.createDirectories(rootLocation)
    }

    override fun upload(file: MultipartFile): UploadFileResponse {
        require(!file.isEmpty) { "File must not be empty" }

        validateContentType(file.contentType)

        val extension = extractExtension(file.originalFilename)
        val fileName = "${UUID.randomUUID()}.$extension"
        val targetLocation = rootLocation.resolve(fileName)

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

    private fun extractExtension(originalFilename: String?): String {
        require(!originalFilename.isNullOrBlank()) { "Original filename must not be blank" }
        val extension = originalFilename.substringAfterLast('.', "").lowercase()
        require(extension.isNotBlank()) { "File extension is missing" }
        require(SupportedImageType.fromExtension(extension) != null) { "Unsupported file extension: $extension" }
        return extension
    }

    private fun validateContentType(contentType: String?) {
        require(!contentType.isNullOrBlank()) { "Content type must not be blank" }
        require(SupportedImageType.fromContentType(contentType) != null) { "Unsupported content type: $contentType" }
    }
}