package ru.edustor.core.util.extensions

import org.springframework.http.HttpStatus
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page


fun Account.hasAccess(lesson: Lesson): Boolean {
    return lesson.owner.id == id
}

fun Account.assertHasAccess(lesson: Lesson) {
    if (!this.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Lesson access forbidden")
}


fun Account.assertHasAccess(page: Page) {
    if (!this.hasAccess(page.lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Page access forbidden")
}