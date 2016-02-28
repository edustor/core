package ru.wutiarn.edustor.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.util.*

/**
 * Created by wutiarn on 28.02.16.
 */
data class Lesson(
        @DBRef var subject: Subject? = null,
        var start: TimetableTime? = null,
        var end: TimetableEntry? = null,
        var date: Calendar = GregorianCalendar.getInstance(),
        @Id var id: String? = null
)