package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.wutiarn.edustor.exception.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.utils.processPdfUpload

/**
 * Created by wutiarn on 26.02.16.
 */
@RestController
@RequestMapping("/api/documents")
class DocumentsController @Autowired constructor(val repo: DocumentsRepository) {
    @RequestMapping("upload", method = arrayOf(RequestMethod.POST))
    fun upload(@RequestParam("file") file: MultipartFile): String? {
        file.contentType
        when (file.contentType) {
            "application/pdf" -> {
                processPdfUpload(file.bytes)
            }
            else -> {
                throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Unsupported content type: ${file.contentType}")
            }
        }
        return "Successfully uploaded"
    }

    @RequestMapping("uuid_info")
    fun uuid_info(@RequestParam uuid: String): Document? {
        return repo.findByUuid(uuid) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping("activate_uuid")
    fun activate_uuid(@RequestParam uuid: String): Map<String, Any> {
        repo.findByUuid(uuid)?.let {
            return mapOf(
                    "created" to false,
                    "document" to it
            )
        }
        val document = Document(uuid = uuid)
        repo.save(document)
        return mapOf(
                "created" to true,
                "document" to document
        )
    }
}