package ru.edustor.core.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.model.Subject
import ru.edustor.core.pdf.storage.PdfStorage
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.PageRepository
import ru.edustor.core.repository.SubjectRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct

@Service
open class CleanupService(
        val pageRepository: PageRepository,
        val lessonRepository: LessonRepository,
        val subjectRepository: SubjectRepository,
        val pdfStorage: PdfStorage
) {

    val logger: Logger = LoggerFactory.getLogger(CleanupService::class.java)

    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Moscow")
    @PostConstruct
    fun cleanupRemovedEntities() {

        val cleanupBeforeDate = Instant.now().minus(10, ChronoUnit.DAYS)
        logger.info("Cleaning up entities removed before $cleanupBeforeDate")

        subjectRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deleteSubject(it) }
        lessonRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deleteLesson(it) }
        pageRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deletePage(it) }

        logger.info("Removed entities cleanup finished")
    }

    fun deleteSubject(subject: Subject) {
        logger.info("Cleaning up subject: ${subject.id}")
        subjectRepository.delete(subject)
    }

    fun deleteLesson(lesson: Lesson) {
        logger.info("Cleaning up lesson: ${lesson.id}")
        lessonRepository.delete(lesson)
    }

    fun deletePage(page: Page) {
        logger.info("Cleaning up page: ${page.id}")
        page.isUploaded.let {
            pdfStorage.delete(page.id)
        }
        pageRepository.delete(page)
    }
}