package ru.edustor.core.util.extensions

import org.springframework.http.HttpStatus
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.repository.LessonsRepository

fun Account.hasAccess(folder: Folder): Boolean {
    return folder.owner == this
}

fun Account.hasAccess(lesson: Lesson): Boolean {
    return this.hasAccess(lesson.folder)
}

fun Account.hasAccess(page: Page, lessonsRepository: LessonsRepository): Boolean {
    return this.hasAccess(page.lesson)
}

fun Account.assertHasAccess(folder: Folder) {
    if (!this.hasAccess(folder)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Folder access forbidden")
}

fun Account.assertHasAccess(lesson: Lesson) {
    if (!this.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Lesson access forbidden")
}

fun Account.assertHasAccess(page: Page, lessonsRepository: LessonsRepository) {
    if (!this.hasAccess(page, lessonsRepository)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Page access forbidden")
}

fun Page.assertIsOwner(user: Account) {
    if (this.owner.id != user.id) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You're not owner of this page")
}