package ru.edustor.core.rabbit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.edustor.commons.models.rabbit.processing.documents.DocumentAssembledEvent
import ru.edustor.core.repository.LessonRepository

@Component
open class AssembledDocumentsProcessor(val lessonRepository: LessonRepository) {
    val logger: Logger = LoggerFactory.getLogger(AssembledDocumentsProcessor::class.java)

    @RabbitListener(bindings = arrayOf(QueueBinding(
            value = Queue("meta.edustor/inbox/assembled", durable = "true", arguments = arrayOf(
                    Argument(name = "x-dead-letter-exchange", value = "reject.edustor")
            )),
            exchange = Exchange("internal.edustor", type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = "finished.assemble.documents.processing"
    )))
    @Transactional
    fun handleUploadedPage(event: DocumentAssembledEvent) {
        val lesson = lessonRepository.findOne(event.documentId) ?: return
        lesson.assembled = true
        lessonRepository.save(lesson)

        logger.info("Marked as assembled: $lesson")
    }
}