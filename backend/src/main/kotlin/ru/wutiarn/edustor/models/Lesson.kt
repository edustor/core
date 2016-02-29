package ru.wutiarn.edustor.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by wutiarn on 28.02.16.
 */
data class Lesson(
        @DBRef var subject: Subject? = null,
        var start: LocalTime? = null,
        var end: LocalTime? = null,
        var date: LocalDate? = null,
        @Id var id: String? = null
)