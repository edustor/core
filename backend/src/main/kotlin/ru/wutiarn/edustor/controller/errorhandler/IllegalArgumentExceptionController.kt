package ru.wutiarn.edustor.controller.errorhandler

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ControllerAdvice
class IllegalArgumentExceptionController : HttpRequestProcessingExceptionController() {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIAE(req: HttpServletRequest,
                  resp: HttpServletResponse,
                  locale: Locale) {
        val exception = HttpRequestProcessingException(HttpStatus.BAD_REQUEST)

        handleHRPEPage(req, resp, exception, locale)

    }
}
