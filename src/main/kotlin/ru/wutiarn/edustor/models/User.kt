package ru.wutiarn.edustor.models

import org.springframework.data.annotation.Id
import java.util.*

/**
 * Created by wutiarn on 22.02.16.
 */
data class User(
        var login: String = "",
        var password: String = "",
        var sessions: MutableList<Session> = mutableListOf(),
        @Id var id: String? = null
)

data class Session(
        val token: String = UUID.randomUUID().toString()
)