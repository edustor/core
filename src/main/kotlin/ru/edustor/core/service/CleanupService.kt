package ru.edustor.core.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.edustor.commons.storage.service.BinaryObjectStorageService
import ru.edustor.commons.storage.service.BinaryObjectStorageService.ObjectType.PAGE
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.LessonRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.annotation.PostConstruct

@Service
open class CleanupService(
        val lessonRepository: LessonRepository,
        val pdfStorage: BinaryObjectStorageService,
        val accountRepository: AccountRepository
) {
    // TODO: Make sure this executes only on one application instance (or move to another microservice?)
    val logger: Logger = LoggerFactory.getLogger(CleanupService::class.java)

    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Moscow")
    @PostConstruct
    fun cleanupRemovedEntities() {

        val cleanupBeforeDate = Instant.now().minus(10, ChronoUnit.DAYS)
        logger.info("Cleaning up entities removed before $cleanupBeforeDate")

//        Cleanup tags
        accountRepository.findByTagsRemovedOnLessThan(cleanupBeforeDate)
                .map { account ->
                    account.tags = account.tags
                            .filter {
                                val ok = it.removedOn?.isAfter(cleanupBeforeDate) ?: true
                                if (!ok) {
                                    logger.info("Cleanup tag: ${it.id}")
                                }
                                return@filter ok
                            }
                            .toMutableList()
                    accountRepository.save(account)
                }

//        Cleanup lessons
//        TODO: Cleanup empty lessons
        lessonRepository.findByRemovedOnLessThan(cleanupBeforeDate)
                .map { lesson ->
                    lesson.pages.forEach { page ->
                        if (page.isUploaded) {
                            pdfStorage.delete(PAGE, page.fileId!!)
                        }
                        logger.info("Cleanup lesson: ${lesson.id}")
                        lessonRepository.delete(lesson)
                    }
                }

//        Cleanup pages
        lessonRepository.findByPagesRemovedOnLessThan(cleanupBeforeDate)
                .map { lesson ->
                    lesson.pages = lesson.pages
                            .filter {
                                val ok = it.removedOn?.isAfter(cleanupBeforeDate) ?: true
                                if (!ok) {
                                    logger.info("Cleanup page: ${it.id}")
                                }
                                return@filter ok
                            }
                            .toMutableList()
                    lessonRepository.save(lesson)
                }

        logger.info("Removed entities cleanup finished")
    }
}