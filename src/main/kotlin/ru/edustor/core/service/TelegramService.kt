package ru.edustor.core.service

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.edustor.core.model.Account
import ru.edustor.core.util.extensions.getAsByteArray
import java.awt.image.BufferedImage

@Service
class TelegramService {

    @Autowired
    lateinit var bot: TelegramBot

    private val logger = LoggerFactory.getLogger(TelegramService::class.java)

    fun sendText(user: Account, text: String, disableNotification: Boolean = true) {
        bot.execute(SendMessage(user.telegramChatId, text).disableNotification(disableNotification))
    }

    fun sendImage(user: Account, image: BufferedImage, caption: String, disableNotification: Boolean = true) {
        bot.execute(
                SendPhoto(user.telegramChatId, image.getAsByteArray())
                        .caption(caption)
                        .disableNotification(disableNotification)
        )
    }

//    fun onUploadingComplete(uploaded: List<PdfPage>, uploadPreferences: PdfUploadPreferences) {
//        val user = uploadPreferences.uploader
//
//        val total = uploaded.count()
//        val noUuid = uploaded.count { it.qrData == null }
//        val failed = uploaded.count { it.exception != null }
//
//        val text = "Processing finished. Total pages: $total. Read errors: $noUuid. Failed: $failed"
//
//        sendText(user, text)
//    }
}