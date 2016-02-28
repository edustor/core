package ru.wutiarn.edustor.models

import org.springframework.data.mongodb.core.mapping.DBRef

/**
 * Created by wutiarn on 28.02.16.
 */
data class TimetableEntry(
        @DBRef var subject: Subject? = null,
        var dayOfWeek: Int? = null,
        var start: TimetableTime? = null,
        var end: TimetableTime? = null
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