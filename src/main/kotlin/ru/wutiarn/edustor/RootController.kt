package ru.wutiarn.edustor

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * Created by wutiarn on 22.02.16.
 */
@Controller
class RootController {
    @RequestMapping("/")
    @ResponseBody
    fun root() = "Hello world"
}