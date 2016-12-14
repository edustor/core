package ru.edustor.core.telegram.rabbit

import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.stereotype.Component
import ru.edustor.commons.models.internal.processing.pdf.PdfUploadedEvent
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.getForAccountId
import ru.edustor.core.service.TelegramService

@Component
open class PdfUploadListener(val accountRepository: AccountRepository,
                             val telegramService: TelegramService) {
    @RabbitListener(bindings = arrayOf(QueueBinding(
            value = Queue("telegram.edustor/inbox/events/upload", durable = "true", arguments = arrayOf(
                    Argument(name = "x-dead-letter-exchange", value = "reject.edustor")
            )),
            exchange = Exchange("internal.edustor", type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = "uploaded.pdf.pages.processing"
    )))
    fun processPdfUploadEvent(event: PdfUploadedEvent) {
        val shortUploadId = event.uuid.split("-").last()
        telegramService.sendText(accountRepository.getForAccountId(event.userId),
                "New upload received. Id: $shortUploadId. Target id: ${event.targetLessonId}. Timestamp: ${event.timestamp}")
    }
}