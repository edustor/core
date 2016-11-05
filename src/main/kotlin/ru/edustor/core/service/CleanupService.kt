package ru.edustor.core.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.pdf.storage.PdfStorage
import ru.edustor.core.repository.FoldersRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.repository.PagesRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct

@Service
open class CleanupService(
        val pagesRepository: PagesRepository,
        val lessonsRepository: LessonsRepository,
        val foldersRepository: FoldersRepository,
        val pdfStorage: PdfStorage
) {

    val logger = LoggerFactory.getLogger(CleanupService::class.java)

    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Moscow")
    @PostConstruct
    fun cleanupRemovedEntities() {

        val cleanupBeforeDate = Instant.now().minus(10, ChronoUnit.DAYS)
        logger.info("Cleaning up entities removed before $cleanupBeforeDate")

        foldersRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deleteSubject(it) }
        lessonsRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deleteLesson(it) }
        pagesRepository.findByRemovedOnLessThan(cleanupBeforeDate).forEach { deletePage(it) }

        logger.info("Removed entities cleanup finished")
    }

    fun deleteSubject(folder: Folder) {
        logger.info("Cleaning up subject: ${folder.id}")
        foldersRepository.delete(folder)
    }

    fun deleteLesson(lesson: Lesson) {
        logger.info("Cleaning up lesson: ${lesson.id}")
        lessonsRepository.delete(lesson)
    }

    fun deletePage(page: Page) {
        logger.info("Cleaning up page: ${page.id}")
        page.isUploaded.let {
            pdfStorage.delete(page.id)
        }
        pagesRepository.delete(page)
    }
}