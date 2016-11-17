package ru.edustor.core.util.extensions

import org.springframework.http.HttpStatus
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.model.Subject
import ru.edustor.core.repository.LessonRepository

fun Account.hasAccess(subject: Subject): Boolean {
    return subject.owner == this
}

fun Account.hasAccess(lesson: Lesson): Boolean {
    return this.hasAccess(lesson.subject)
}

fun Account.hasAccess(page: Page, lessonRepository: LessonRepository): Boolean {
    return this.hasAccess(page.lesson)
}

fun Account.assertHasAccess(subject: Subject) {
    if (!this.hasAccess(subject)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Subject access forbidden")
}

fun Account.assertHasAccess(lesson: Lesson) {
    if (!this.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Lesson access forbidden")
}

fun Account.assertHasAccess(page: Page, lessonRepository: LessonRepository) {
    if (!this.hasAccess(page, lessonRepository)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Page access forbidden")
}

fun Page.assertIsOwner(user: Account) {
    if (this.owner.id != user.id) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You're not owner of this page")
}