package ru.edustor.core.service

import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.edustor.core.model.Document
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import ru.edustor.core.pdf.storage.PdfStorage
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.repository.SubjectsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct

@Service
open class CleanupService(
        val mongoOperations: MongoOperations,
        val documentsRepository: DocumentsRepository,
        val lessonsRepository: LessonsRepository,
        val subjectsRepository: SubjectsRepository,
        val pdfStorage: PdfStorage
) {

    val logger = LoggerFactory.getLogger(CleanupService::class.java)

    @PostConstruct
    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Moscow")
    fun cleanupUnusedLessons() {
        logger.info("Empty lessons cleanup initiated")
        val result = mongoOperations.remove(Query.query(Criteria
                .where("documents").size(0)
                .and("date").lt(LocalDate.now().minusMonths(1))
        ), "lesson")

        logger.info("Empty lessons cleanup completed. Affected: ${result.n}")
    }

    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Moscow")
    @PostConstruct
    fun cleanupRemovedEntities() {

        val cleanupBeforeDate = Instant.now().minus(10, ChronoUnit.DAYS)
        logger.info("Cleaning up entities removed before $cleanupBeforeDate")

        subjectsRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deleteSubject(it) }
        lessonsRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deleteLesson(it) }
        documentsRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deleteDocument(it) }

        logger.info("Removed entities cleanup finished")
    }

    fun deleteSubject(folder: Folder) {
        logger.info("Cleaning up subject: ${folder.id}")
        subjectsRepository.delete(folder)
    }

    fun deleteLesson(lesson: Lesson) {
        logger.info("Cleaning up lesson: ${lesson.id}")
        lessonsRepository.delete(lesson)
    }

    fun deleteDocument(document: Document) {
        logger.info("Cleaning up document: ${document.id}")
        document.isUploaded.let {
            pdfStorage.delete(document.id)
        }
        documentsRepository.delete(document)
    }
}