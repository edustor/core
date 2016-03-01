package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef

/**
 * Created by wutiarn on 22.02.16.
 */
open class User(
        var login: String? = null,
        @JsonIgnore var password: String? = null,
        @JsonIgnore var sessions: MutableList<Session> = mutableListOf(),
        @JsonIgnore @DBRef(lazy = true) var groups: MutableList<Group> = mutableListOf(),
        @JsonIgnore var timetable: MutableList<TimetableEntry> = mutableListOf(),
        @Id var id: String? = null
)