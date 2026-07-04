package io.github.mksfilmoteka.media.file

import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/media/files")
class FileController(private val fileService: FileService) {

    @PostMapping
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadFileResponse> {
        val response = fileService.upload(file)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{fileName}")
    fun load(@PathVariable fileName: String): ResponseEntity<Resource> {
        val contentType = resolveContentType(fileName)
        val resource = fileService.load(fileName)

        return ResponseEntity.ok().contentType(contentType).body(resource)
    }

    private fun resolveContentType(fileName: String): MediaType {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val imageType = SupportedImageType.fromExtension(extension)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file extension: $extension")
        return imageType.mediaType
    }
}