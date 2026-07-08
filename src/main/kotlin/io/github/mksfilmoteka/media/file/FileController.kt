package io.github.mksfilmoteka.media.file

import io.github.mksfilmoteka.media.exception.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/media/files")
@Tag(name = "Media files", description = "Upload, load, and delete media files")
class FileController(private val fileService: FileService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload image file",
        description = "Uploads a JPEG or PNG image, resizes it, and returns the stored file reference.",
    )
    @ApiResponse(responseCode = "201", description = "File uploaded",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = UploadFileResponse::class),
            ),
        ],
    )
    @ApiResponse(responseCode = "400", description = "Invalid image file",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ErrorResponse::class),
            ),
        ],
    )
    fun upload(
        @Parameter(description = "JPEG or PNG image file to upload", required = true)
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<UploadFileResponse> {
        val response = fileService.upload(file)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{fileName}")
    @Operation(
        summary = "Load image file",
        description = "Returns a stored JPEG or PNG image."
    )
    @ApiResponse(responseCode = "200", description = "Image file",
        content = [
            Content(
                mediaType = MediaType.IMAGE_JPEG_VALUE,
                schema = Schema(type = "string", format = "binary"),
            ),
            Content(
                mediaType = MediaType.IMAGE_PNG_VALUE,
                schema = Schema(type = "string", format = "binary"),
            ),
        ],
    )
    @ApiResponse(responseCode = "400", description = "Unsupported file extension",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ErrorResponse::class),
            ),
        ],
    )
    @ApiResponse(responseCode = "404", description = "File not found",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ErrorResponse::class),
            ),
        ],
    )
    fun load(
        @Parameter(description = "Stored file name", example = "b8f3d76a-8b8f-4d57-a7b8-1a74d3f2b65a.jpg")
        @PathVariable fileName: String,
    ): ResponseEntity<Resource> {
        val contentType = resolveContentType(fileName)
        val resource = fileService.load(fileName)

        return ResponseEntity.ok().contentType(contentType).body(resource)
    }

    @DeleteMapping("/{fileName}")
    @Operation(
        summary = "Delete image file",
        description = "Deletes a stored media file. Deleting a missing file is treated as successful."
    )
    @ApiResponse(responseCode = "204", description = "File deleted")
    @ApiResponse(responseCode = "400", description = "Invalid file path",
        content = [
            Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = Schema(implementation = ErrorResponse::class),
            ),
        ],
    )
    fun delete(
        @Parameter(description = "Stored file name", example = "b8f3d76a-8b8f-4d57-a7b8-1a74d3f2b65a.jpg")
        @PathVariable fileName: String,
    ): ResponseEntity<Unit> {
        fileService.delete(fileName)

        return ResponseEntity.noContent().build()
    }

    private fun resolveContentType(fileName: String): MediaType {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val imageType = SupportedImageType.fromExtension(extension)
            ?: throw IllegalArgumentException("Unsupported file extension: $extension")
        return imageType.mediaType
    }
}