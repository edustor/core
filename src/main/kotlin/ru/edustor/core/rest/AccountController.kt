package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.edustor.core.model.Account
import ru.edustor.core.repository.AccountRepository

@RestController
@RequestMapping("/api/account")
class AccountController @Autowired constructor(val accountRepository: AccountRepository) {
    @RequestMapping("/getMe")
    fun getMe(user: Account): Account.AccountDTO {
        return user.toDTO()
    }

    @RequestMapping("/FCMToken", method = arrayOf(RequestMethod.PUT))
    fun setFCMToken(@RequestParam token: String?, account: Account) {
        if (token != null) {
//            TODO: Somehow hibernate duplicates tokens. It is not connected with Edustor code, I'm sure.
//            Temporary solution is to use Set
            account.fcmTokens.add(token)
            accountRepository.save(account)
        }
    }
}