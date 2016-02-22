package ru.wutiarn.edustor

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Created by wutiarn on 22.02.16.
 */
@RestController
@RequestMapping("/api")
class ApiController {
    @RequestMapping("/register")
    fun register(@RequestParam login: String, @RequestParam password: String): String {
        return "Registered $login $password"
    }

    @RequestMapping("/login")
    fun login(@RequestParam login: String, @RequestParam password: String): String {
        return "Logged in as $login"
    }

    @RequestMapping("/deregister")
    fun deregister(@RequestParam login: String): String {
        return "Successfully deregistered"
    }
}