package ru.wutiarn.edustor.models

import org.springframework.data.annotation.Id

/**
 * Created by wutiarn on 22.02.16.
 */
data class User(
        var login: String = "",
        var password: String = "",
        @Id
        var id: String? = null)