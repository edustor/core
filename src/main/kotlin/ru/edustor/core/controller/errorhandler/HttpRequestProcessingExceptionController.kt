package ru.edustor.core.controller.errorhandler

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver
import ru.edustor.core.exceptions.HttpRequestProcessingException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Converts occurred [HttpRequestProcessingException] to HTML/JSON and sends it to client
 */
@ControllerAdvice
open class HttpRequestProcessingExceptionController {
    @Autowired
    private lateinit var errorAttributes: ErrorAttributes

    @Autowired
    private lateinit var viewResolver: FreeMarkerViewResolver

    @Autowired
    var mapper: ObjectMapper? = null

    @ExceptionHandler(HttpRequestProcessingException::class)
    @ResponseBody
    @Throws(Exception::class)
    fun handleHRPEPage(req: HttpServletRequest,
                       resp: HttpServletResponse,
                       ex: HttpRequestProcessingException,
                       locale: Locale) {
        resp.status = ex.status.value()

        val attributes = errorAttributes.getErrorAttributes(ServletRequestAttributes(req), false)
        val exceptionStatus = ex.status
        attributes.put("status", exceptionStatus.value())
        attributes.put("error", exceptionStatus.reasonPhrase)
        attributes.remove("exception")

        val acceptHeader = req.getHeader("Accept")
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            viewResolver.resolveViewName("error", locale).render(attributes, req, resp)
            return
        }
        mapper!!.writeValue(resp.writer, attributes)

    }
}
