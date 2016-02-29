package ru.wutiarn.edustor.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Field

/**
 * Created by wutiarn on 22.02.16.
 */
open class User(
        var login: String? = null,
        @Field("password") var mPassword: String? = null,
        var sessions: MutableList<Session> = mutableListOf(),
        @DBRef(lazy = true) var groups: MutableList<Group> = mutableListOf(),
        var timetable: MutableList<TimetableEntry> = mutableListOf(),
        @Id var id: String? = null
)