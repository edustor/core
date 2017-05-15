package ru.edustor.core.rabbit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.edustor.commons.models.rabbit.processing.pages.PageProcessedEvent
import ru.edustor.commons.models.rabbit.processing.pages.PageRecognizedEvent
import ru.edustor.commons.storage.service.BinaryObjectStorageService
import ru.edustor.commons.storage.service.BinaryObjectStorageService.ObjectType.PAGE
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.PageRepository
import ru.edustor.core.repository.getForAccountId
import ru.edustor.core.util.extensions.hasAccess
import ru.edustor.core.util.extensions.setIndexes
import java.time.Instant

@Component
open class RecognizedPagesProcessor(var storage: BinaryObjectStorageService,
                                    val accountRepository: AccountRepository,
                                    val lessonRepository: LessonRepository,
                                    val pageRepository: PageRepository,
                                    val rabbitTemplate: RabbitTemplate) {
    val logger: Logger = LoggerFactory.getLogger(RecognizedPagesProcessor::class.java)

    @RabbitListener(bindings = arrayOf(QueueBinding(
            value = Queue("meta.edustor/inbox/recognized", durable = "true", arguments = arrayOf(
                    Argument(name = "x-dead-letter-exchange", value = "reject.edustor")
            )),
            exchange = Exchange("internal.edustor", type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = "recognized.pages.processing"
    )))
    @Transactional
    fun handleUploadedPage(event: PageRecognizedEvent) {
        val (lesson, page) = getTargetLessonAndPage(event)

        val processedEvent = PageProcessedEvent(
                userId = event.userId,
                uploadUuid = event.uploadUuid,
                totalPageCount = event.totalPageCount,
                pageIndex = event.pageIndex,
                pageUuid = event.pageUuid,
                qrUuid = event.qrUuid,
                success = page != null,
                targetLessonId = lesson?.id,
                targetLessonName = lesson.toString()) // TODO: add more information to targetLessonName

        rabbitTemplate.convertAndSend(
                "internal.edustor",
                "finished.pages.processing",
                processedEvent
        )

        if (page == null || lesson == null) {
            storage.delete(PAGE, event.pageUuid)
            logger.warn("Skipping ${event.pageUuid} page")
            return
        }

        page.fileId?.let { storage.delete(PAGE, it) } // TODO: Preserve old files

        page.fileId = event.pageUuid
        page.uploadedTimestamp = Instant.now()
        page.contentType = "application/pdf"
        page.fileMD5 = event.fileMD5
        lessonRepository.save(lesson)
        logger.info("Page ${page.id} updated. New file id is ${event.pageUuid}")
    }

    private fun getTargetLessonAndPage(event: PageRecognizedEvent): Pair<Lesson?, Page?> {
        if (event.qrUuid == null && event.targetLessonId == null) {
            logger.info("Failed to find target page in database. Skipping")
            storage.delete(PAGE, event.pageUuid)
        }

        val uploaderAccount = accountRepository.getForAccountId(event.userId)

        val lesson = when {
            event.targetLessonId != null -> {
                lessonRepository.findOne(event.targetLessonId) ?: let {
                    logger.warn("Failed to find explicitly specified target lesson ${event.targetLessonId} in database")
                    return null to null
                }
            }
            event.qrUuid != null -> {
                pageRepository.findByQr(event.qrUuid!!)?.lesson ?: let {
                    logger.warn("Failed to find page with ${event.qrUuid} qr in database")
                    return null to null
                }
            }
            else -> {
                logger.warn("Neither targetLessonId nor qrUuid is specified in event")
                return null to null
            }
        }

        if (!uploaderAccount.hasAccess(lesson)) {
            logger.warn("User doesn't have access to target lesson ${lesson.id}. " +
                    "Page file id: ${event.pageUuid}. Uploader: ${event.userId}")
            return null to null
        }

        val page: Page = event.qrUuid?.let {
            pageRepository.findByQr(event.qrUuid!!)
        } ?: let {
            val p = Page(lesson = lesson, qr = event.qrUuid, timestamp = event.uploadedTimestamp ?: Instant.now())
            return@let p
        }

        page.lesson = lesson

        if (page !in lesson.pages) {
            lesson.pages.add(page)
            lesson.pages.setIndexes()
        }

        return lesson to page
    }
}