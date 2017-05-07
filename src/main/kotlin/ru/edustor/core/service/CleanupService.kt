package ru.edustor.core.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.edustor.commons.storage.service.BinaryObjectStorageService
import ru.edustor.core.model.Page
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.PageRepository
import ru.edustor.core.repository.TagRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
open class CleanupService(
        val lessonRepository: LessonRepository,
        val pdfStorage: BinaryObjectStorageService,
        val tagRepository: TagRepository,
        val pageRepository: PageRepository
) {
    // TODO: Make sure this executes only on one application instance (or move to another microservice?)
    val logger: Logger = LoggerFactory.getLogger(CleanupService::class.java)

    //    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Moscow")
//    @PostConstruct
    fun cleanupRemovedEntities() {

        val cleanupBeforeDate = Instant.now().minus(10, ChronoUnit.DAYS)
        logger.info("Cleaning up entities removed before $cleanupBeforeDate")

        val tags = tagRepository.findByRemovedOnLessThan(cleanupBeforeDate)
        tags.forEach {
            it.lessons.flatMap { it.pages }.let { removePageFiles(it) }
        }
        tagRepository.delete(tags)

        val lessons = lessonRepository.findByRemovedOnLessThan(cleanupBeforeDate)
        lessons.forEach {
            removePageFiles(it.pages)
        }
        lessonRepository.delete(lessons)

        val pages = pageRepository.findByRemovedOnLessThan(cleanupBeforeDate)
        removePageFiles(pages)
        pageRepository.delete(pages)

//        TODO: Cleanup empty lessons
        logger.info("Removed entities cleanup finished")
    }

    fun removePageFiles(pages: List<Page>) {
        pages.map {
            it.fileId?.let {
                pdfStorage.delete(BinaryObjectStorageService.ObjectType.PAGE, it)
            }
        }
    }

}