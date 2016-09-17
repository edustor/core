package ru.edustor.core.telegram.commands

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Component
import ru.edustor.core.EdustorApplication
import ru.edustor.core.telegram.TelegramEventsRouter
import ru.edustor.core.telegram.TelegramHandler
import ru.edustor.core.util.extensions.replyText

@Component
open class VersionCommandHandler(telegramEventsRouter: TelegramEventsRouter) : TelegramHandler {

    init {
        telegramEventsRouter.registerCommand("version", this)
    }

    override fun process(msg: Message): AbstractSendRequest<SendMessage>? {
        return msg.replyText("Edustor Core v${EdustorApplication.VERSION} by Dmitry Romanov (@wutiarn)")
    }
}