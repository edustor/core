package ru.wutiarn.edustor

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import ru.wutiarn.edustor.models.User

/**
 * Created by wutiarn on 22.02.16.
 */
@Controller
class RootController {
    @RequestMapping("/")
    @ResponseBody
    fun root(@AuthenticationPrincipal user: User?): String {
        return if(user != null) "Hello ${user.login}" else "Hello world"
    }
}