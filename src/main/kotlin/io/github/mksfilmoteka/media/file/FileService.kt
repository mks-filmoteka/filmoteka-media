package io.github.mksfilmoteka.media.file

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FileService {

    fun upload(file: MultipartFile): UploadFileResponse

    fun load(fileName: String): Resource

    fun delete(fileName: String)
}