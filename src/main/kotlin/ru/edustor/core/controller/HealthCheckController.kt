package ru.edustor.core.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/health")
class HealthCheckController {
    @RequestMapping
    @ResponseBody
    fun check(): String {
        return "OK"
    }
}