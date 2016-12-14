package ru.edustor.core.telegram.commands

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Component
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.telegram.TelegramEventsRouter
import ru.edustor.core.telegram.TelegramHandler
import ru.edustor.core.util.extensions.cid
import ru.edustor.core.util.extensions.replyText

@Component
open class StartCommandHandler(telegramEventsRouter: TelegramEventsRouter, val accountRepository: AccountRepository) : TelegramHandler {

    val tokenRegex = "/start ([\\w-]*)".toRegex()

    init {
        telegramEventsRouter.registerCommand("start", this)
    }

    override fun process(msg: Message): AbstractSendRequest<SendMessage>? {
        val token = tokenRegex.find(msg.text())?.groupValues?.get(1) ?: return msg.replyText("Cannot find initialization token")
        val user = accountRepository.findByTelegramLinkToken(token) ?: return msg.replyText("Unknown initialization token")

        user.telegramChatId = msg.cid()
        user.telegramLinkToken = null

        accountRepository.save(user)

        return msg.replyText("Greetings, ${user.id}. Now your Edustor account is linked with this Telegram account.")
    }
}