package ru.wutiarn.edustor.models

/**
 * Created by wutiarn on 28.02.16.
 */
data class TimetableTime(
        val hour: Int,
        val minute: Int
) : Comparable<TimetableTime> {


    override fun compareTo(other: TimetableTime): Int {
        val hourComparison = hour.compareTo(other.hour)
        if (hourComparison != 0) return hourComparison
        return minute.compareTo(other.minute)
    }

    override fun toString(): String {
        var h = hour.toString()
        var m = minute.toString()

        if (h.length < 2) h = "0" + h
        if (m.length < 2) m = "0" + m


        return "$h:$m"
    }
}