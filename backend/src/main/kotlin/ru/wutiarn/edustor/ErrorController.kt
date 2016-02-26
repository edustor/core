package ru.wutiarn.edustor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ErrorAttributes
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest

/**
 * Created by wutiarn on 09.02.16.
 */
@Controller
@RequestMapping("/error")
class ErrorController : org.springframework.boot.autoconfigure.web.ErrorController {
    @Autowired
    private val errorAttributes: ErrorAttributes? = null

    @RequestMapping(produces = arrayOf("text/html"))
    fun errorPage(req: HttpServletRequest, model: Model): String {
        val errorAttributes = getErrorAttributes(req)
        model.addAllAttributes(errorAttributes)
        return "error"
    }

    @RequestMapping
    @ResponseBody
    fun error(req: HttpServletRequest): Map<String, Any> {
        return getErrorAttributes(req)
    }

    private fun getErrorAttributes(request: HttpServletRequest): Map<String, Any> {
        val requestAttributes = ServletRequestAttributes(request)
        return errorAttributes!!.getErrorAttributes(requestAttributes, false)
    }

    override fun getErrorPath(): String {
        return "/error"
    }
}
