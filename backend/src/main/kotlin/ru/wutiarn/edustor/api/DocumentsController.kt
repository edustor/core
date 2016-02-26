package ru.wutiarn.edustor.api

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import ru.wutiarn.edustor.exception.HttpRequestProcessingException
import ru.wutiarn.edustor.utils.processPdfUpload

/**
 * Created by wutiarn on 26.02.16.
 */
@Controller
@RequestMapping("/api/documents")
class DocumentsController {
    @RequestMapping("upload", method = arrayOf(RequestMethod.POST), produces = arrayOf(MediaType.IMAGE_PNG_VALUE))
    @ResponseBody
    fun upload(@RequestParam("file") file: MultipartFile): ByteArray? {
        file.contentType
        when(file.contentType) {
            "application/pdf" -> {
                val images = processPdfUpload(file.bytes)
                val res = images.values.firstOrNull()
                return res
            }
            else -> {
                throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Unsupported content type: ${file.contentType}")
            }
        }
    }
}