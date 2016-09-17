package ru.edustor.core.util.extensions

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.request.SendMessage

fun Message.replyText(text: String): SendMessage {
    return SendMessage(this.chat().id(), text)
}

fun Message.cid(): String {
    return this.chat().id().toString()
}