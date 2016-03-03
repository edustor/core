package ru.wutiarn.edustor.utils

import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User

/**
 * Created by wutiarn on 28.02.16.
 */
fun User.hasAccess(subject: Subject): Boolean {
    return subject.groups.intersect(this.groups).isNotEmpty()
}

fun User.hasAccess(lesson: Lesson): Boolean {
    return this.hasAccess(lesson.subject!!)
}

fun User.hasAccess(document: Document): Boolean {
    return this.hasAccess(document.lesson!!)
}