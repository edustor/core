package ru.edustor.core.pdf.qr

import org.springframework.stereotype.Component
import ru.edustor.core.util.extensions.getAsByteArray
import java.awt.image.BufferedImage
import java.io.File

@Component
open class QRReader {
    fun readQR(image: BufferedImage): String? {
        val tempFile = File.createTempFile("edustor-qr", ".tmp.png")
        tempFile.writeBytes(image.getAsByteArray())

        val process = Runtime.getRuntime().exec(arrayOf(
                "zbarimg", "-q", "--raw", tempFile.absolutePath
        ))


        val result = process.inputStream.reader().readLines()

        tempFile.delete()

        return result.getOrNull(0)
    }
}