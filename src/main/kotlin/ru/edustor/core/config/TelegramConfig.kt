package ru.edustor.core.config

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramBotAdapter
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
open class TelegramConfig(@Value("\${edustor.core.telegram-token}") val telegramToken: String) {
    @Bean
    open fun telegramBot(): TelegramBot {
        val client = OkHttpClient().newBuilder().readTimeout(70, TimeUnit.SECONDS).build()
        return TelegramBotAdapter.buildCustom(telegramToken, client)
    }
}