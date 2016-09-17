package ru.edustor.core.telegram

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.AbstractSendRequest

interface Command {
    fun process(msg: Message): AbstractSendRequest<*>?
}