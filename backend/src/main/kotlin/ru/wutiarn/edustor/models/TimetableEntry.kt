package ru.wutiarn.edustor.models

import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Created by wutiarn on 28.02.16.
 */
data class TimetableEntry(
        @DBRef var subject: Subject? = null,
        var dayOfWeek: DayOfWeek? = null,
        var start: LocalTime? = null,
        var end: LocalTime? = null
) : Comparable<TimetableEntry> {
    override fun compareTo(other: TimetableEntry): Int {
        return start!!.compareTo(other.start!!)
    }

    fun isIntersects(other: TimetableEntry): Boolean {
        if (dayOfWeek!! != other.dayOfWeek!!) return false

        val before = (start!! > other.start!!) and (start!! > other.end!!) //other before this.start
        val after = (end!! < other.start!!) and (end!! < other.end!!) // other after this.start
        return !(before or after)
    }
}