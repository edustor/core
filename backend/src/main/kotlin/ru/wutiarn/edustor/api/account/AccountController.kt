package ru.wutiarn.edustor.api.account

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.models.User

@RestController
@RequestMapping("/api/account")
class AccountController {
    @RequestMapping("/getMe")
    fun getMe(@AuthenticationPrincipal user: User): User {
        return user
    }
}