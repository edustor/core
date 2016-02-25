package ru.wutiarn.edustor

import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.utils.getQR
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Created by wutiarn on 22.02.16.
 */
@Controller
class RootController {
    @RequestMapping("/")
    @ResponseBody
    fun root(@AuthenticationPrincipal user: User?): String {
        return if(user != null) "Hello ${user.login}" else "Hello world"
    }

    @RequestMapping("/pdf", produces = arrayOf(MediaType.IMAGE_PNG_VALUE))
    @ResponseBody
    fun pdf(): ByteArray? {
        val bufferedImage = getQR()
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "png", outputStream)
        return outputStream.toByteArray()
    }
}