package ru.edustor.core.exceptions

import org.springframework.http.HttpStatus

open class HttpRequestProcessingException(val status: HttpStatus, val payload: Map<out String, Any?>,
                                          override val cause: Throwable? = null) : RuntimeException() {

    constructor(status: HttpStatus, msg: String? = null, cause: Throwable? = null) : this(
            status,
            mapOf("message" to (msg ?: "No message available")),
            cause
    )
}