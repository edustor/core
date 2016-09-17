package ru.edustor.core.config

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramBotAdapter
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
open class TelegramConfig {

    lateinit var telegramToken: String

    @Bean
    open fun telegramBot(): TelegramBot {
        telegramToken = System.getenv("TELEGRAM_TOKEN") ?: throw IllegalArgumentException("Telegram token was not provided. Please set TELEGRAM_TOKEN environment variable.")
        val client = OkHttpClient().newBuilder().readTimeout(70, TimeUnit.SECONDS).build()
        return TelegramBotAdapter.buildCustom(telegramToken, client)
    }
}