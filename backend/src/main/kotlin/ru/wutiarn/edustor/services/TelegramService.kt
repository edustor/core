package ru.wutiarn.edustor.services

import com.mashape.unirest.http.Unirest
import org.springframework.stereotype.Service

@Service
class TelegramService {
    val TOKEN: String = "198639402:AAGQZQcVkSivxYzJJIcBhQDZPBqCdWjRH0Q"
    val url: String
        get() = "https://api.telegram.org/bot$TOKEN/"

    fun onUploaded(uploaded: List<PdfUploadService.Page>) {
        val total = uploaded.count()
        val noUuid = uploaded.count { it.uuid == null }
        val uuids = uploaded.filter { it.uuid != null }.fold("", { str, it -> str + it.uuid?.split("-")?.last() + "\n"})

        val text = "Uploaded $total pages. $noUuid pages have not been recognized. \n$uuids"

        Unirest.post(url + "sendMessage")
                .field("chat_id", "43457173")
                .field("text", text)
                .asStringAsync()
    }
}