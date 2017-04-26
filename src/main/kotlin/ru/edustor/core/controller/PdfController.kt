package ru.edustor.core.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class PdfController {

    val logger: Logger = LoggerFactory.getLogger(PdfController::class.java)

    @RequestMapping("/pdf/{lessonId}")
    fun getPdf(@PathVariable lessonId: String): String {
        logger.info("Accessing lesson PDF: $lessonId")

        return "redirect:https://storage.edustor.ru/pdf/$lessonId"
    }
}