package ru.wutiarn.edustor.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef

/**
 * Created by wutiarn on 28.02.16.
 */
data class Group(
        var name: String? = null,
        @JsonIgnore var timetable: MutableList<TimetableEntry> = mutableListOf(),
        @DBRef(lazy = true) @JsonIgnore var owners: MutableList<User> = mutableListOf(),
        @Id var id: String? = null
)