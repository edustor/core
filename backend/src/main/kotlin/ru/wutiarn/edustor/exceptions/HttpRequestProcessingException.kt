package ru.wutiarn.edustor.exceptions

import org.springframework.http.HttpStatus

class HttpRequestProcessingException(val status: HttpStatus, override val message: String? = null, override val cause: Throwable? = null) : RuntimeException(message)
