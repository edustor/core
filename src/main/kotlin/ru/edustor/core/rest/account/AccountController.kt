package ru.edustor.core.rest.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.edustor.core.model.User
import ru.edustor.core.repository.SessionRepository
import ru.edustor.core.repository.UserRepository
import java.util.*

@RestController
@RequestMapping("/api/account")
class AccountController @Autowired constructor(val sessionRepository: SessionRepository,
                                               val userRepository: UserRepository) {
    @RequestMapping("/getMe")
    fun getMe(@AuthenticationPrincipal user: User): User {
        return user
    }

    @RequestMapping("/FCMToken", method = arrayOf(RequestMethod.PUT))
    fun setFCMToken(@RequestParam token: String?, @AuthenticationPrincipal user: User) {
        val session = user.currentSession!!
        session.FCMToken = token
        sessionRepository.save(session)
    }

    @RequestMapping("/telegram/link")
    fun getTelegramLink(@AuthenticationPrincipal user: User): String {
        val token = UUID.randomUUID().toString()
        user.telegramLinkToken = token
        userRepository.save(user)

        return "https://telegram.me/edustor_bot?start=$token"
    }
}