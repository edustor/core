package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.utils.extensions.getActiveLesson
import ru.wutiarn.edustor.utils.extensions.hasAccess
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/lessons")
class LessonsController @Autowired constructor(val lessonsRepo: LessonsRepository, val documentsRepository: DocumentsRepository, val gfs: GridFsOperations) {
    @RequestMapping("/{lesson}/documents")
    fun getDocuments(@PathVariable lesson: Lesson?, @AuthenticationPrincipal user: User): List<Document> {
        lesson ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        if (!user.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You have not access to this lesson")
        return lesson.documents
    }

    @RequestMapping("/{lesson}/pdf", produces = arrayOf("application/pdf"))
    fun getPdf(@PathVariable lesson: Lesson?, @AuthenticationPrincipal user: User) {
        //        lesson ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        //        if (!user.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You have not access to this lesson")
        //
        //        val document = com.itextpdf.text.Document()
        //
        //        val outputStream = ByteArrayOutputStream()
        //        val copy = PdfCopy(document, outputStream)
        //
        //        lesson.documents.toObservable()
        //                .filter { it.isUploaded == true }
        //                .toSortedList()
        //                .flatMap { it.toObservable() }
        //                .map {
        //                    gfs.findOne(Query.query(GridFsCriteria.whereFilename().`is`(it.uuid)
        //                            .andOperator(GridFsCriteria.whereContentType().`is`("application/pdf2"))))
        //                }
        //                .filterNotNull()
        //                .subscribe {
        //                    val pdfReader = PdfReader(it.inputStream)
        //                    copy.addDocument(pdfReader)
        //                }


    }

    @RequestMapping("/current")
    fun current(@AuthenticationPrincipal user: User, @RequestParam offset: Int): Lesson {
        val userNow = OffsetDateTime.now(ZoneOffset.ofHours(offset)).toLocalDateTime()
        return user.timetable.getActiveLesson(lessonsRepo, userNow) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping("/uuid/{uuid}")
    fun byDocumentUUID(@AuthenticationPrincipal user: User, @PathVariable uuid: String): Lesson {
        val document = documentsRepository.findByUuid(uuid) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Document is not found")
        val lesson = lessonsRepo.findByDocumentsContaining(document) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Lesson is not found")

        if (!user.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You have not access to this lesson")

        return lesson
    }
}
