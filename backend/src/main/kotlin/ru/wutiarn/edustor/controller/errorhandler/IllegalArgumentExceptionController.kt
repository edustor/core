package ru.wutiarn.edustor.controller.errorhandler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ControllerAdvice
class IllegalArgumentExceptionController {

    @Autowired
    lateinit var hrpeController: HttpRequestProcessingExceptionController

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIAE(req: HttpServletRequest,
                  resp: HttpServletResponse,
                  ex: IllegalArgumentException,
                  locale: Locale) {
        val exception = HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "One of required argument is not specified or couldn't be resolved in database")

        hrpeController.handleHRPEPage(req, resp, exception, locale)

    }
}
