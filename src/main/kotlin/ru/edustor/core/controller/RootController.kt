package ru.edustor.core.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class RootController {
    @RequestMapping
    fun root(): String {
        return "redirect:https://wutiarn.ru/edustor-overview-a8acc7f6e09b"
    }
}