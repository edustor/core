package ru.wutiarn.edustor.utils.extensions

import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.TimetableEntry
import ru.wutiarn.edustor.repository.LessonsRepository
import rx.Observable
import rx.lang.kotlin.firstOrNull
import rx.lang.kotlin.onErrorReturnNull
import rx.lang.kotlin.toObservable
import java.time.LocalDate
import java.time.LocalDateTime

fun Observable<TimetableEntry>.getActiveTimetableEntry(time: LocalDateTime): Observable<TimetableEntry> {
    return this.toSortedList()
            .flatMap { it.toObservable() }
            .filter { it.dayOfWeek == time.dayOfWeek }
            .last { it.start!! < time.toLocalTime() }
}

fun Observable<TimetableEntry>.getLesson(lessonsRepo: LessonsRepository, today: LocalDate): Observable<Lesson> {
    return this.map {
        lessonsRepo.findLesson(it.subject!!, today, it.start!!, it.end!!)?.let { return@map it }

        val lesson = Lesson(it.subject!!, it.start!!, it.end!!, today)
        lessonsRepo.save(lesson)
        return@map lesson
    }
}

fun List<TimetableEntry>.getActiveLesson(lessonsRepo: LessonsRepository, userNow: LocalDateTime): Lesson? {
    return this.toObservable()
            .getActiveTimetableEntry(userNow)
            .getLesson(lessonsRepo, userNow.toLocalDate())
            .onErrorReturnNull()
            .toBlocking().firstOrNull()
}