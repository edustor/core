package ru.edustor.core.telegram

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.AbstractSendRequest
import com.pengrad.telegrambot.request.SendMessage

class StartCommand : Command {
    override fun process(msg: Message): AbstractSendRequest<SendMessage>? {
        val token = msg.text().split(" ").last()
        return null
    }
}