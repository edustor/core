package ru.edustor.core.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.GetFile
import com.pengrad.telegrambot.request.GetUpdates
import com.pengrad.telegrambot.request.SendMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.edustor.core.model.internal.pdf.PdfUploadPreferences
import ru.edustor.core.repository.UserRepository
import ru.edustor.core.service.PdfUploadService
import ru.edustor.core.util.extensions.cid
import ru.edustor.core.util.extensions.replyText
import javax.annotation.PostConstruct
import kotlin.concurrent.thread

@Service
open class EventsRouter(val bot: TelegramBot, val pdfUploadService: PdfUploadService, val userRepository: UserRepository) {

    private val commandRegex = "/(\\w*)".toRegex()
    val logger = LoggerFactory.getLogger(EventsRouter::class.java)

    val handlers = mutableMapOf<String, TelegramHandler>()

    fun registerCommand(command: String, handler: TelegramHandler) {
        if (handlers.containsKey(command)) throw IllegalStateException("TelegramHandler $command redeclaration")
        handlers[command] = handler
    }

    @PostConstruct
    private fun handleEvents() {
        thread(isDaemon = true) {
            var lastUpdateId = 0
            while (true) {
                bot.execute(GetUpdates().offset(lastUpdateId).timeout(60)).updates().forEach {
                    lastUpdateId = it.updateId() + 1
                    try {
                        processUpdate(it)
                    } catch (e: Exception) {
                        logger.warn("Exception occurred while processing telegram message", e)
                    }
                }
            }
        }
    }

    fun processUpdate(update: Update) {
        val msg = update.message()
        if (msg != null) {
            if (msg.document() != null) {
                val fileId = msg.document().fileId()
                val file = bot.execute(GetFile(fileId)).file()
                val url = bot.getFullFilePath(file)

                val user = userRepository.findByTelegramChatId(msg.cid())
                if (user == null) {
                    bot.execute(msg.replyText("You're not logged in"))
                    return
                }

                pdfUploadService.processFromURL(url, PdfUploadPreferences(user))

            } else if (msg.text() != null) {
                routeTextMessage(msg)
            }
        }
    }

    private fun routeTextMessage(msg: Message) {
        val text = msg.text()
        val command = commandRegex.find(text)?.groupValues?.get(1)

        if (command != null) {
            val handler = handlers[command]
            val resp: AbstractSendRequest<*>?

            if (handler != null) {
                resp = handler.process(msg)
            } else {
                resp = SendMessage(msg.chat().id(), "Unsupported command")
            }
            resp?.let { bot.execute(it) }
        } else {
            bot.execute(SendMessage(msg.chat().id(), "Invalid message"))
        }
    }

}