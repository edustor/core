package ru.wutiarn.edustor.utils

import org.apache.commons.io.IOUtils
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import javax.imageio.ImageIO

/**
 * Created by wutiarn on 26.02.16.
 */
fun Image.getAsByteArray(): ByteArray {
    return IOUtils.toByteArray(this.getAsInputStream())
}

fun Image.getAsInputStream(): InputStream {
    val pipedInputStream = PipedInputStream()
    val pipedOutputStream = PipedOutputStream(pipedInputStream)
    ImageIO.setUseCache(false)
    ImageIO.write(this as BufferedImage, "png", pipedOutputStream)
    return pipedInputStream
}