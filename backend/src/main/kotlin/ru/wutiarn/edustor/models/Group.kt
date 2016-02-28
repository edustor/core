package ru.wutiarn.edustor.models

import org.springframework.data.mongodb.core.mapping.DBRef

/**
 * Created by wutiarn on 28.02.16.
 */
data class Group(
        var name: String? = null,
        var timetable: MutableList<TimetableEntry> = mutableListOf(),
        @DBRef
        val subjects: MutableList<Subject> = mutableListOf()
)