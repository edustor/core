package ru.wutiarn.edustor.models

import org.springframework.data.mongodb.core.mapping.DBRef

/**
 * Created by wutiarn on 28.02.16.
 */
data class TimetableEntry(
        @DBRef
        var subject: Subject? = null,
        var dayOfWeek: Int? = null,
        var start: TimetableTime? = null,
        var end: TimetableTime? = null
)