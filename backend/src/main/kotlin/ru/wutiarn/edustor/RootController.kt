package ru.wutiarn.edustor

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import ru.wutiarn.edustor.utils.getAsByteArray
import ru.wutiarn.edustor.utils.getPdf
import ru.wutiarn.edustor.utils.getQR

/**
 * Created by wutiarn on 22.02.16.
 */
@Controller
class RootController {

    @RequestMapping("/qr", produces = arrayOf(MediaType.IMAGE_PNG_VALUE))
    @ResponseBody
    fun qr(): ByteArray {
        return getQR().getAsByteArray()

    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") // Kotlin's Int can't be == null
    @RequestMapping("/pdf", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun pdf(@RequestParam(required = false) c: Integer?): ByteArray {
        val count = c?.toInt() ?: 1
        if (!(count >= 1 && count <= 100)) {
            throw RuntimeException("Too much pages")
        }
        val pdf = getPdf(count)
        return pdf
    }
}