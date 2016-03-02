package ru.wutiarn.edustor.exceptions

import org.springframework.http.HttpStatus

/**
 * Created by wutiarn on 11/02/16.
 */
class HttpRequestProcessingException(val status: HttpStatus, override val message: String? = null) : RuntimeException(message)
