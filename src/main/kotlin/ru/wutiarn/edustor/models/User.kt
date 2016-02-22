package ru.wutiarn.edustor.models

import org.springframework.data.annotation.Id

/**
 * Created by wutiarn on 22.02.16.
 */
data class User(
        var login: String?=null,
        var password: String?=null,
        @Id
        var id: String?=null)