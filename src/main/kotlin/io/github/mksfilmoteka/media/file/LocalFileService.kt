package io.github.mksfilmoteka.media.file

import io.github.mksfilmoteka.media.config.MediaProperties
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class LocalFileService(mediaProperties: MediaProperties) : FileService {

    private val rootLocation: Path = Path.of(mediaProperties.rootLocation)
    private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")

    init {
        Files.createDirectories(rootLocation)
    }

    override fun upload(file: MultipartFile): UploadFileResponse {
        require(!file.isEmpty) { "File must not be empty" }

        val extension = extractExtension(file.originalFilename)
        val fileName = "${UUID.randomUUID()}.$extension"
        val targetLocation = rootLocation.resolve(fileName)

        file.inputStream.use { inputStream ->
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
        }

        return UploadFileResponse(fileName)
    }

    override fun load(fileName: String): Resource {
        val filePath = rootLocation.resolve(fileName).normalize()
        val resource = UrlResource(filePath.toUri())

        require(resource.exists() && resource.isReadable) { "File not found: $fileName" }

        return resource
    }

    override fun delete(fileName: String) {
        val filePath = rootLocation.resolve(fileName).normalize()
        Files.deleteIfExists(filePath)
    }

    private fun extractExtension(originalFilename: String?): String {
        require(!originalFilename.isNullOrBlank()) { "Original filename must not be blank" }

        val extension = originalFilename.substringAfterLast('.', "").lowercase()

        require(extension.isNotBlank()) { "File extension is missing" }
        require(extension in allowedExtensions) { "Unsupported file extension: $extension" }

        return extension
    }
}