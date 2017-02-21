package ru.edustor.core.util.extensions

import org.springframework.http.HttpStatus
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson


fun Account.hasAccess(lesson: Lesson): Boolean {
    return lesson.ownerId == id
}

fun Account.assertHasAccess(lesson: Lesson) {
    if (!this.hasAccess(lesson)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "Lesson access forbidden")
}