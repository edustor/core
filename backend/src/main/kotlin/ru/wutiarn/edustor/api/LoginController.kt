package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Session
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.SessionRepository
import ru.wutiarn.edustor.repository.UserRepository
import ru.wutiarn.edustor.utils.GoogleTokenVerifier

@RestController
@RequestMapping("/api/login")
class LoginController @Autowired constructor(val userRepo: UserRepository,
                                             val sessionRepo: SessionRepository,
                                             val googleVerifier: GoogleTokenVerifier) {

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun login(@RequestParam token: String): Session {
        val googleAccount: GoogleTokenVerifier.GoogleAccount
        try {
            googleAccount = googleVerifier.verify(token)
        } catch (e: IllegalArgumentException) {
            throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Bad token")
        }

        val user = userRepo.findByEmail(googleAccount.email) ?: let {
            val u = User(googleAccount.email)
            userRepo.save(u)
            u
        }

        val session = Session(user = user)
        sessionRepo.save(session)

        return session
    }

    @RequestMapping("/check_token")
    fun checkToken(@AuthenticationPrincipal user: User?): String {
        user?.let { return "You're logged in as ${user.email}" }
        return "Looks like you're not logged in"
    }
}
