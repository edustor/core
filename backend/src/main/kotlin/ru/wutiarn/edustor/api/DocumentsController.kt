package ru.wutiarn.edustor.api

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.wutiarn.edustor.exception.HttpRequestProcessingException
import ru.wutiarn.edustor.utils.processPdfUpload

/**
 * Created by wutiarn on 26.02.16.
 */
@RestController
@RequestMapping("/api/documents")
class DocumentsController {
    @RequestMapping("upload", method = arrayOf(RequestMethod.POST))
    fun upload(@RequestParam("file") file: MultipartFile): String? {
        file.contentType
        when(file.contentType) {
            "application/pdf" -> {
                processPdfUpload(file.bytes)
            }
            else -> {
                throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Unsupported content type: ${file.contentType}")
            }
        }
        return "Successfully uploaded"
    }
}