package ru.wutiarn.edustor.exceptions

import org.springframework.http.HttpStatus

open class HttpRequestProcessingException(open val status: HttpStatus, open override val message: String? = null,
                                          open override val cause: Throwable? = null) : RuntimeException(message)
