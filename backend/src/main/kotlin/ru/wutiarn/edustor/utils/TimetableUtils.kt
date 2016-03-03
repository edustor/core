package ru.wutiarn.edustor.utils

import ru.wutiarn.edustor.models.TimetableEntry
import rx.Observable
import rx.lang.kotlin.toObservable
import java.time.LocalTime

fun Observable<TimetableEntry>.getActive(time: LocalTime): Observable<TimetableEntry> {
    return this.toSortedList()
            .flatMap { it.toObservable() }
            .last { it.start!! > time }
}