package ru.wutiarn.edustor.utils.extensions

import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.TimetableEntry
import ru.wutiarn.edustor.repository.LessonsRepository
import rx.Observable
import rx.lang.kotlin.toObservable
import java.time.LocalDate
import java.time.LocalTime

fun Observable<TimetableEntry>.getActiveTimetableEntry(time: LocalTime): Observable<TimetableEntry> {
    return this.toSortedList()
            .flatMap { it.toObservable() }
            .last { it.start!! > time }
}

fun Observable<TimetableEntry>.getLesson(lessonsRepo: LessonsRepository, today: LocalDate): Observable<Lesson> {
    return this.map {
        lessonsRepo.findLesson(it.subject!!, today, it.start!!, it.end!!)
                ?: Lesson(it.subject, it.start, it.end, today)
    }
}