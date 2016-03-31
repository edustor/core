package ru.wutiarn.edustor.services

import com.mashape.unirest.http.Unirest
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class TelegramService {
    val TOKEN: String = "198639402:AAGQZQcVkSivxYzJJIcBhQDZPBqCdWjRH0Q"
    val url: String
        get() = "https://api.telegram.org/bot$TOKEN/"

    fun onUploaded(uploaded: List<PdfUploadService.Page>) {
        val total = uploaded.count()
        val noUuid = uploaded.count { it.uuid == null }
        val uuids = uploaded.filter { it.uuid != null }.fold("", {
            str, it ->
            val uuid = str + it.uuid?.split("-")?.last()
            val lessonInfo = it.lesson?.let { "${it.subject?.name}. ${it.topic ?: "No topic"}. ${it.date?.format(DateTimeFormatter.ISO_LOCAL_DATE)}" } ?: "Not registered"
            "$uuid: $lessonInfo\n"
        })

        val text = "Uploaded: $total. QR read errors: $noUuid \n$uuids"

        Unirest.post(url + "sendMessage")
                .field("chat_id", "43457173")
                .field("text", text)
                .asStringAsync()
    }
}