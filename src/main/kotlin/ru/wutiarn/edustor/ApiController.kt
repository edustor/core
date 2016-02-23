package ru.wutiarn.edustor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.UserRepository
import javax.servlet.http.HttpServletRequest

/**
 * Created by wutiarn on 22.02.16.
 */
@RestController
@RequestMapping("/api")
open class ApiController @Autowired constructor(val repo: UserRepository, val gfs: GridFsOperations) {


    @RequestMapping("/register")
    fun register(@RequestParam login: String, @RequestParam password: String): String {
        if (repo.countByLogin(login) > 0) {
            return "Already exists"
        }

        val user = User(login, password)
        repo.save(user)
        return "Registered $login $password"
    }

    @RequestMapping("/login")
    fun login(@RequestParam login: String, @RequestParam password: String): String {
        val user = repo.findByLogin(login)
        if (user?.password == password) {
            return "Logged in as $login"
        }
        return "Login error"
    }

    @RequestMapping("/deregister")
    fun deregister(@RequestParam login: String): String {
        repo.deleteByLogin(login)
        return "Successfully deregistered"
    }

    @RequestMapping(value = "/upload", method = arrayOf(RequestMethod.POST))
    fun upload(req: HttpServletRequest, @RequestParam file: MultipartFile) {
        gfs.store(file.inputStream, "upload_${file.originalFilename}")
    }
}