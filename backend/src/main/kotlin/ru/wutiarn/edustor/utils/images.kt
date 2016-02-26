package ru.wutiarn.edustor.utils

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Created by wutiarn on 26.02.16.
 */
fun getImageAsByteArray(image: BufferedImage): ByteArray {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(image, "png", outputStream)
    return outputStream.toByteArray()
}