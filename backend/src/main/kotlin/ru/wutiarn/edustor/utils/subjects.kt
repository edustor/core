package ru.wutiarn.edustor.utils

import org.springframework.http.HttpStatus
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User

/**
 * Created by wutiarn on 28.02.16.
 */
fun assertHasAccess(user: User, subject: Subject) {
    val intersect = subject.groups.intersect(user.groups)
    if (intersect.isEmpty()) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN)
}