package ru.wutiarn.edustor.services

import com.mashape.unirest.http.Unirest
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.wutiarn.edustor.utils.UploadPreferences
import ru.wutiarn.edustor.utils.getAsByteArray
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

    private fun sendText(text: String) {
        telegramToken ?: return
        Unirest.post(url + "sendMessage")
                .field("chat_id", "43457173")
                .field("text", text)
                .field("disable_notification", "true")
                .asStringAsync()
    }

    fun onUploadingStarted() {
        sendText("Processing file...")
    }

    fun onUploadingComplete(uploaded: List<PdfUploadService.Page>, uploadPreferences: UploadPreferences) {
        val total = uploaded.count()
        val noUuid = uploaded.count { it.uuid == null }
        val uuids = uploaded.filter { it.uuid != null }.fold("", {
            str, it ->
            val uuid = str + it.uuid?.split("-")?.last()
            val lessonInfo = it.lesson?.let { "${it.subject?.name}. ${it.topic ?: "No topic"}. ${it.date?.format(DateTimeFormatter.ISO_LOCAL_DATE)}" } ?: "Not registered"
            "$uuid: $lessonInfo\n"
        })

        val text = "Uploaded: $total. QR read errors: $noUuid \n$uuids"

        sendText(text)

        uploaded.filter { it.renderedImage != null }
                .forEach {
                    val index = uploaded.indexOf(it).toString()
                    sendImage(it.renderedImage!!, "Img $index")

                    for (i in 0..it.qrImages.lastIndex) {
                        sendImage(it.qrImages[i], "Img $index place $i")
                    }
                }
    }

    fun sendImage(image: BufferedImage, caption: String) {
        val entity = MultipartEntityBuilder.create()
                .addTextBody("chat_id", "43457173")
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