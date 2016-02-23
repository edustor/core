package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.models.Session
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.UserRepository
import javax.servlet.http.HttpServletRequest

/**
 * Created by wutiarn on 23.02.16.
 */
@RestController
@RequestMapping("/api/login")
class LoginController @Autowired constructor(val repo: UserRepository) {
    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun login(@RequestParam login: String, @RequestParam password: String): String {
        val user = repo.findByLogin(login)
        if (user != null && user.password == password) {
            val session = Session()
            user.sessions.add(session)
            repo.save(user)
            return "Logged in as $login with token ${session.token.toString()}"
        }
        return "Login error"
    }

    @RequestMapping("/register")
    fun register(@RequestParam login: String, @RequestParam password: String): String {
        if (repo.countByLogin(login) > 0) {
            return "Already exists"
        }

        val user = User(login, password)
        repo.save(user)
        return "Registered $login $password"
    }

    @RequestMapping("/check_token")
    fun checkToken(req: HttpServletRequest): String {
        val user = req.getAttribute("user")
        if(user != null && user is User) {
            return "You're logged in as ${user.login}"
        }
        return "Looks like you're not logged in"
    }
}
