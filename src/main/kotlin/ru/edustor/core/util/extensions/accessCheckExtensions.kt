package ru.edustor.core.util.extensions

import org.springframework.http.HttpStatus
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.model.Tag

fun Account.hasAccess(tag: Tag): Boolean {
    return tag.owner == this
}

fun Account.hasAccess(lesson: Lesson): Boolean {
    return this.hasAccess(lesson.tag)
}

fun Account.hasAccess(page: Page): Boolean {
    return this.hasAccess(page.lesson)
}

fun Account.assertHasAccess(tag: Tag) {
    if (!this.hasAccess(tag)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Subject access forbidden")
}

fun Account.assertHasAccess(lesson: Lesson) {
    if (!this.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Lesson access forbidden")
}

fun Account.assertHasAccess(page: Page) {
    if (!this.hasAccess(page)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Page access forbidden")
}

fun Page.assertIsOwner(user: Account) {
    if (this.owner.id != user.id) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You're not owner of this page")
}