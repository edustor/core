package ru.wutiarn.edustor.api.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.SessionRepository

@RestController
@RequestMapping("/api/account")
class AccountController @Autowired constructor(val sessionRepository: SessionRepository) {
    @RequestMapping("/getMe")
    fun getMe(@AuthenticationPrincipal user: User): User {
        return user
    }

    @RequestMapping("/FCMToken", method = arrayOf(RequestMethod.PUT))
    fun setFCMToken(@RequestParam token: String, @AuthenticationPrincipal user: User) {
        val session = user.currentSession!!
        session.FCMToken = token
        sessionRepository.save(session)
    }
}