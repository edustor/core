package ru.edustor.core.telegram.commands

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Component
import ru.edustor.core.repository.UserRepository
import ru.edustor.core.telegram.EventsRouter
import ru.edustor.core.telegram.TelegramHandler
import ru.edustor.core.util.extensions.cid
import ru.edustor.core.util.extensions.replyText

@Component
open class LogoutCommandHandler(eventsRouter: EventsRouter, val userRepository: UserRepository) : TelegramHandler {

    init {
        eventsRouter.registerCommand("logout", this)
    }

    override fun process(msg: Message): AbstractSendRequest<SendMessage>? {
        val user = userRepository.findByTelegramChatId(msg.cid()) ?: return msg.replyText("You're not logged in")
        user.telegramChatId = null
        userRepository.save(user)
        return msg.replyText("Logged out")
    }
}