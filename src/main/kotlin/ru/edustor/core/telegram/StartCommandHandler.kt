package ru.edustor.core.telegram

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Component
import ru.edustor.core.repository.UserRepository
import ru.edustor.core.util.extensions.replyText

@Component
open class StartCommandHandler(eventsRouter: EventsRouter, val userRepository: UserRepository) : TelegramHandler {

    val tokenRegex = "/start ([\\w-]*)".toRegex()

    init {
        eventsRouter.registerCommand("start", this)
    }

    override fun process(msg: Message): AbstractSendRequest<SendMessage>? {
        val token = tokenRegex.find(msg.text())?.groupValues?.get(1) ?: return msg.replyText("Cannot find initialization token")
        val user = userRepository.findByTelegramLinkToken(token) ?: return msg.replyText("Unknown initialization token")

        user.telegramChatId = msg.chat().id().toString()
        user.telegramLinkToken = null

        userRepository.save(user)

        return msg.replyText("Greetings, ${user.email}. Now your Edustor account is linked with this Telegram account.")
    }
}