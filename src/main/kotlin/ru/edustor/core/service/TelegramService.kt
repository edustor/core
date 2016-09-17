package ru.edustor.core.service

import com.mashape.unirest.http.Unirest
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.edustor.core.model.User
import ru.edustor.core.model.internal.pdf.PdfUploadPreferences
import ru.edustor.core.pdf.upload.PdfPage
import ru.edustor.core.util.extensions.getAsByteArray
import rx.Observable
import rx.schedulers.Schedulers
import java.awt.image.BufferedImage
import java.time.format.DateTimeFormatter
import javax.annotation.PostConstruct

@Service
class TelegramService {
    val telegramToken: String? = System.getenv("TELEGRAM_TOKEN")
    val url: String
        get() = "https://api.telegram.org/bot$telegramToken/"

    private val logger = LoggerFactory.getLogger(TelegramService::class.java)

    @PostConstruct
    private fun checkTokenProvided() {
        telegramToken ?: logger.warn("Telegram token was not provided. Please set TELEGRAM_TOKEN environment variable.")
    }

    private fun sendText(user: User, text: String) {
        telegramToken ?: return
        Unirest.post(url + "sendMessage")
                .field("chat_id", user.telegramChatId)
                .field("text", text)
                .field("disable_notification", "true")
                .asStringAsync()
    }

    fun onUploadingStarted(user: User) {
        sendText(user, "Processing file...")
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

    fun sendImage(user: User, image: BufferedImage, caption: String) {
        val entity = MultipartEntityBuilder.create()
                .addTextBody("chat_id", user.telegramChatId)
                .addTextBody("caption", caption)
                .addBinaryBody("photo", image.getAsByteArray(), ContentType.APPLICATION_OCTET_STREAM, "img.png")
                .build()
        val httpPost = HttpPost(url + "sendPhoto")
        httpPost.entity = entity
        Observable.just(httpPost)
                .observeOn(Schedulers.io())
                .subscribe { HttpClients.createDefault().execute(it).close() }
    }
}