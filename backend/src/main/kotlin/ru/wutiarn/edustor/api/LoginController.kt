package ru.wutiarn.edustor.api

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
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
import ru.wutiarn.edustor.repository.UserRepository

@RestController
@RequestMapping("/api/login")
class LoginController @Autowired constructor(val repo: UserRepository) {

    val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
            .setAudience(listOf("99685742253-41uieqd0vl3e03l62c7t3impd38gdt4q.apps.googleusercontent.com"))
            .setIssuer("https://accounts.google.com")
            .build()

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun login(@RequestParam token: String): Session {
        val googleId: GoogleIdToken
        try {
            googleId = verifier.verify(token) ?: throw IllegalArgumentException()
        } catch (e: Throwable) {
            throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Bad token")
        }

        val email = googleId.payload.email

        val user = repo.findByEmail(email)

        user ?: throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "User is not found")

        val session = Session(user = user)
        user.sessions.add(session)
        repo.save(user)

        return session
    }
    @RequestMapping("/check_token")
    fun checkToken(@AuthenticationPrincipal user: User?): String {
        user?.let { return "You're logged in as ${user.email}" }
        return "Looks like you're not logged in"
    }
}
