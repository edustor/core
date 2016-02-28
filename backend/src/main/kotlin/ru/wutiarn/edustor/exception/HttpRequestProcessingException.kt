package ru.wutiarn.edustor.exception

import org.springframework.http.HttpStatus

/**
 * Created by wutiarn on 11/02/16.
 */
class HttpRequestProcessingException(val status: HttpStatus, override val message: String? = null) : RuntimeException(message)
