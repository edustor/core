package ru.edustor.core.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.edustor.commons.models.rabbit.processing.documents.DocumentAssembleRequest
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.Lesson
import ru.edustor.core.repository.LessonRepository


@Service
@ManagedResource
class DocumentAssemblerService(private val rabbitTemplate: RabbitTemplate,
                               private val lessonRepository: LessonRepository) {

    val logger: Logger = LoggerFactory.getLogger(DocumentAssemblerService::class.java)

    fun assembleDocument(lesson: Lesson) {
        val documentAssembleRequest = DocumentAssembleRequest(documentId = lesson.id,
                pages = lesson.pages
                        .filter { it.fileId != null && !it.removed && it.contentType == "application/pdf" }
                        .map { DocumentAssembleRequest.Page(fileId = it.fileId!!) }
        )

        rabbitTemplate.convertAndSend(
                "internal.edustor",
                "requested.assemble.documents.processing",
                documentAssembleRequest
        )

        logger.info("Document assemble request sent: $lesson ")
    }

    @Transactional
    @ManagedOperation
    fun assembleDocument(lessonId: String) {
        val lesson = lessonRepository.findOne(lessonId) ?: throw NotFoundException("Cannot find lesson with id $lessonId")
        assembleDocument(lesson)
    }

    @Transactional
    @ManagedOperation
    fun assembleAllDocuments(reassemble: Boolean = false) {
        val lessons = when (reassemble) {
            true -> lessonRepository.findAll()
            false -> lessonRepository.findByAssembled(false)
        }

        lessons.forEach { assembleDocument(it) }
    }
}