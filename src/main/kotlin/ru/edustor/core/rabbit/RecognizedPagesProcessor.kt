package ru.edustor.core.rabbit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.stereotype.Component
import ru.edustor.commons.protobuf.proto.internal.EdustorPdfProcessingProtos.PageRecognizedEvent
import ru.edustor.commons.storage.service.BinaryObjectStorageService
import ru.edustor.commons.storage.service.BinaryObjectStorageService.ObjectType.PAGE
import ru.edustor.core.model.Page
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.PageRepository
import ru.edustor.core.repository.getForAccountId
import ru.edustor.core.util.extensions.hasAccess
import java.time.Instant

@Component
open class RecognizedPagesProcessor(val pageRepository: PageRepository,
                                    var storage: BinaryObjectStorageService,
                                    val accountRepository: AccountRepository,
                                    val lessonRepository: LessonRepository) {
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
    fun handleUploadedPage(msg: ByteArray) {
        val event = PageRecognizedEvent.parseFrom(msg)

        val page = getTargetPage(event) ?: let {
            storage.delete(PAGE, event.pageUuid)
            logger.warn("Skipping ${event.pageUuid} page")
            return
        }

        page.fileId?.let { storage.delete(PAGE, it) } // TODO: Preserve old files

        page.fileId = event.pageUuid
        page.isUploaded = true
        page.uploadedTimestamp = Instant.now()
        page.contentType = "application/pdf"
        pageRepository.save(page)
        logger.info("Page ${page.id} updated. New file id is ${event.pageUuid}")
    }

    private fun getTargetPage(event: PageRecognizedEvent): Page? {
        val targetLessonId = event.targetLessonId

        if (event.qrUuid == null && targetLessonId == null) {
            logger.info("Failed to find target page in database. Skipping")
            storage.delete(PAGE, event.pageUuid)
        }

        // ?: is used to handle case when event.qrUuid is presented, but pageRepository.findByQr returned null
        val page = (if (event.qrUuid != null) pageRepository.findByQr(event.qrUuid) else null) ?: let {
            if (targetLessonId == null) {
                logger.warn("Can't find page with qr ${event.qrUuid}. Skipping")
                return null
            }

            val p = Page(null)
            p.owner = accountRepository.getForAccountId(event.userId)
            if (event.uploadedTimestamp != 0L) {
                p.timestamp = Instant.ofEpochSecond(event.uploadedTimestamp)
            }
            return@let p
        }

        if (page.owner.id != event.userId) {
            logger.warn("Unauthorized upload. Page: $page. " +
                    "Page file id: ${event.pageUuid}. Uploader: ${event.userId}")
            return null
        }

        targetLessonId?.let {
            val lesson = lessonRepository.findOne(targetLessonId) ?: let {
                logger.warn("Failed to find explicitly specified target lesson in database. Skipping")
                return null
            }

            if (!page.owner.hasAccess(lesson)) {
                logger.warn("User doesn't have access to target lesson ${lesson.id}. Page: ${page.id}. " +
                        "Page file id: ${event.pageUuid}. Uploader: ${event.userId}")
                return null
            }

            page.lesson = lesson
        }

        return page
    }
}