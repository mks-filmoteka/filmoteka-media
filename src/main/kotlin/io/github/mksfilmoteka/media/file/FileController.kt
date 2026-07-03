package io.github.mksfilmoteka.media.file

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/media/files")
class FileController(private val fileService: FileService) {

    @PostMapping
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadFileResponse> {
        val response = fileService.upload(file)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}