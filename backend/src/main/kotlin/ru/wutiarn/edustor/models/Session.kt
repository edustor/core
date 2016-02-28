package ru.wutiarn.edustor.models

import java.util.*

/**
 * Created by wutiarn on 28.02.16.
 */
data class Session(
        val token: String = UUID.randomUUID().toString()
)