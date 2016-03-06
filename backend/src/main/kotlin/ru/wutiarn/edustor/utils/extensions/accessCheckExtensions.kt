package ru.wutiarn.edustor.utils.extensions

import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.LessonsRepository

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
    val lesson = lessonsRepository.findByDocumentsContaining(document) ?: return false
    return this.hasAccess(lesson)
}