package ru.wutiarn.edustor.utils

import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.LessonsRepository
import rx.lang.kotlin.toObservable

/**
 * Created by wutiarn on 28.02.16.
 */
fun User.hasAccess(subject: Subject): Boolean {
    return subject.groups.intersect(this.groups).isNotEmpty()
}

fun User.hasAccess(lesson: Lesson): Boolean {
    return this.hasAccess(lesson.subject!!)
}

fun User.hasAccess(document: Document, lessonsRepository: LessonsRepository): Boolean {
    val lessons = lessonsRepository.findByDocumentsContaining(document)
    return lessons.toObservable()
            .exists { this.hasAccess(it) }
            .toBlocking().first()
}