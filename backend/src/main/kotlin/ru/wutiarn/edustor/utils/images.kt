package ru.wutiarn.edustor.utils

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Created by wutiarn on 26.02.16.
 */
fun Image.getAsByteArray(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    ImageIO.setUseCache(false)
    ImageIO.write(this as BufferedImage, "png", outputStream)
    return outputStream.toByteArray()
}