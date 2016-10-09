package ru.edustor.core.rest.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.edustor.core.model.Account
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.SessionRepository
import java.util.*

@RestController
@RequestMapping("/api/account")
class AccountController @Autowired constructor(val sessionRepository: SessionRepository,
                                               val accountRepository: AccountRepository) {
    @RequestMapping("/getMe")
    fun getMe(@AuthenticationPrincipal user: Account): Account {
        return user
    }

    @RequestMapping("/FCMToken", method = arrayOf(RequestMethod.PUT))
    fun setFCMToken(@RequestParam token: String?, @AuthenticationPrincipal user: Account) {
        val session = user.currentSession!!
        session.FCMToken = token
        sessionRepository.save(session)
    }

    @RequestMapping("/telegram/link")
    fun getTelegramLink(@AuthenticationPrincipal user: Account): String {
        val token = UUID.randomUUID().toString()
        user.telegramLinkToken = token
        accountRepository.save(user)

        return "https://telegram.me/edustor_bot?start=$token"
    }
}