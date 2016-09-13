package ru.edustor.core.rest.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Session
import ru.edustor.core.repository.SessionRepository
import ru.edustor.core.repository.UserRepository
import ru.edustor.core.util.GoogleTokenVerifier

@RestController
@RequestMapping("/api/account/login")
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

        val user = userRepo.findByEmail(googleAccount.email) //?: let {
//            val u = User(googleAccount.email)
//            userRepo.save(u)
//            u
//        }
//      TODO: Enable new users registration on public release
        user ?: throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "This project is in alpha state. Registration is forbidden")

        val session = Session(user = user)
        sessionRepo.save(session)

        return session
    }
}
