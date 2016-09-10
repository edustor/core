package ru.edustor.core.util.extensions

import org.springframework.http.HttpStatus
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import ru.edustor.core.model.User
import ru.edustor.core.repository.LessonsRepository

fun User.hasAccess(subject: Subject): Boolean {
    return subject.owner == this
}

fun User.hasAccess(lesson: Lesson): Boolean {
    return this.hasAccess(lesson.subject!!)
}

fun User.hasAccess(document: Document, lessonsRepository: LessonsRepository): Boolean {
    val lesson = lessonsRepository.findByDocumentsContaining(document) ?: return false
    return this.hasAccess(lesson)
}

fun User.assertHasAccess(subject: Subject) {
    if (!this.hasAccess(subject)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Subject access forbidden")
}

fun User.assertHasAccess(lesson: Lesson) {
    if (!this.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Lesson access forbidden")
}

fun User.assertHasAccess(document: Document, lessonsRepository: LessonsRepository) {
    if (!this.hasAccess(document, lessonsRepository)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Document access forbidden")
}

fun Document.assertIsOwner(user: User) {
    if (this.owner.id != user.id) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You're not owner of this document")
}