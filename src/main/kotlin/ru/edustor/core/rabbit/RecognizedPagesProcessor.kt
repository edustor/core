package ru.edustor.core.rabbit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.stereotype.Component
import ru.edustor.commons.protobuf.proto.internal.EdustorPdfProcessingProtos.PageRecognizedEvent
import ru.edustor.commons.storage.service.BinaryObjectStorageService
import ru.edustor.commons.storage.service.BinaryObjectStorageService.ObjectType.PAGE
import ru.edustor.core.repository.PageRepository
import java.time.Instant

@Component
open class RecognizedPagesProcessor(val pageRepository: PageRepository,
                                    var storage: BinaryObjectStorageService) {
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

        if (event.qrUuid != null) {
            val page = pageRepository.findByQr(event.qrUuid) ?: let {
                logger.warn("Can't find page with qr ${event.qrUuid}. Skipping")
                storage.delete(PAGE, event.pageUuid)
                return
            }
            if (page.owner.id != event.userId) {
                logger.warn("Unauthorized upload. Page: $page. " +
                        "Page file id: ${event.pageUuid}. Uploader: ${event.userId}")
                storage.delete(PAGE, event.pageUuid)
                return
            }

            page.fileId?.let { storage.delete(PAGE, it) } // TODO: Preserve old files

            page.fileId = event.pageUuid
            page.isUploaded = true
            page.uploadedTimestamp = Instant.now()
            page.contentType = "application/pdf"
            pageRepository.save(page)
            logger.info("Page ${page.id} updated. New file id is ${event.pageUuid}")
        } else {
            logger.info("Failed to find target page in database. Skipping")
            storage.delete(PAGE, event.pageUuid)
        }
    }
}