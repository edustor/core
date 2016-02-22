package ru.wutiarn.edustor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.UserRepository

/**
 * Created by wutiarn on 22.02.16.
 */
@RestController
@RequestMapping("/api")
class ApiController {

    @Autowired
    var repo: UserRepository? = null


    @RequestMapping("/register")
    fun register(@RequestParam login: String, @RequestParam password: String): String {
        if (repo!!.countByLogin(login) > 0) {
            return "Already exists"
        }

        val user = User(login, password)
        repo!!.save(user)
        return "Registered $login $password"
    }

    @RequestMapping("/login")
    fun login(@RequestParam login: String, @RequestParam password: String): String {
        val user = repo?.findByLogin(login)
        if (user?.password == password) {
            return "Logged in as $login"
        }
        return "Login error"
    }

    @RequestMapping("/deregister")
    fun deregister(@RequestParam login: String): String {
        repo?.deleteByLogin(login)
        return "Successfully deregistered"
    }
}