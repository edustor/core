package ru.edustor.core.service

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.edustor.core.model.User
import ru.edustor.core.model.internal.pdf.PdfUploadPreferences
import ru.edustor.core.pdf.upload.PdfPage
import ru.edustor.core.util.extensions.getAsByteArray
import java.awt.image.BufferedImage
import java.time.format.DateTimeFormatter

@Service
class TelegramService {

    @Autowired
    lateinit var bot: TelegramBot

    private val logger = LoggerFactory.getLogger(TelegramService::class.java)

    private fun sendText(user: User, text: String) {
        bot.execute(SendMessage(user.telegramChatId, text).disableNotification(true))
    }

    fun onUploadingStarted(user: User) {
        sendText(user, "Processing file...")
    }

    fun sendImage(user: User, image: BufferedImage, caption: String) {

        bot.execute(SendPhoto(user.telegramChatId, image.getAsByteArray()).caption(caption))
    }

    fun onUploadingComplete(uploaded: List<PdfPage>, uploadPreferences: PdfUploadPreferences) {
        val user = uploadPreferences.uploader

        val total = uploaded.count()
        val noUuid = uploaded.count { it.uuid == null }
        val uuids = uploaded.filter { it.uuid != null }.fold("", {
            str, it ->
            val uuid = str + it.uuid?.split("-")?.last()
            val lessonInfo = it.lesson?.let { "${it.subject?.name}. ${it.topic ?: "No topic"}. ${it.date?.format(DateTimeFormatter.ISO_LOCAL_DATE)}" } ?: "Not registered"
            "$uuid: $lessonInfo\n"
        })

        val text = "Uploaded: $total. QR read errors: $noUuid \n$uuids"

        sendText(user, text)

        if (uploadPreferences.lesson == null) {
            uploaded.filter { it.uuid == null }
                    .forEach {
                        val index = uploaded.indexOf(it).toString()
                        sendImage(user, it.preview, "Img $index")

                        for (i in 0..it.qrImages.lastIndex) {
                            sendImage(user, it.qrImages[i], "Img $index place $i")
                        }
                    }
        } else {
            val lesson = uploadPreferences.lesson!!
            sendText(user, "QR read errors has been suppressed due to target lesson was explicitly specified: ${lesson.subject?.name} on ${lesson.date}")
        }
    }
}