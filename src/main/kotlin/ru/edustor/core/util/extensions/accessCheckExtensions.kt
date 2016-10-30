package ru.edustor.core.util.extensions

import org.springframework.http.HttpStatus
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Document
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import ru.edustor.core.repository.LessonsRepository

fun Account.hasAccess(folder: Folder): Boolean {
    return folder.owner == this
}

fun Account.hasAccess(lesson: Lesson): Boolean {
    return this.hasAccess(lesson.folder)
}

fun Account.hasAccess(document: Document, lessonsRepository: LessonsRepository): Boolean {
    val lesson = lessonsRepository.findByDocumentsContaining(document) ?: return false
    return this.hasAccess(lesson)
}

fun Account.assertHasAccess(folder: Folder) {
    if (!this.hasAccess(folder)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Subject access forbidden")
}

fun Account.assertHasAccess(lesson: Lesson) {
    if (!this.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Lesson access forbidden")
}

fun Account.assertHasAccess(document: Document, lessonsRepository: LessonsRepository) {
    if (!this.hasAccess(document, lessonsRepository)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Document access forbidden")
}

fun Document.assertIsOwner(user: Account) {
    if (this.owner.id != user.id) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You're not owner of this document")
}