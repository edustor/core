package ru.edustor.core.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.GetUpdates
import com.pengrad.telegrambot.request.SendMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import kotlin.concurrent.thread

@Service
class EventsRouter(val bot: TelegramBot) {

    private val commandRegex = "/(\\w*)".toRegex()
    val logger = LoggerFactory.getLogger(EventsRouter::class.java)

    val handlers = mapOf<String, Command>(
            "start" to StartCommand()
    )

    @PostConstruct
    fun registerCommands() {
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

            } else if (msg.text() != null) {
                routeTextMessage(msg)
            }
        }
    }

    fun routeTextMessage(msg: Message) {
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