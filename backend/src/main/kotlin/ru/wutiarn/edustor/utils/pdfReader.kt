package ru.wutiarn.edustor.utils

import com.google.zxing.BinaryBitmap
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.FileOutputStream

val logger = LoggerFactory.getLogger("ru.wutiarn.edustor.utils.pdfReader")


/**
 * Created by wutiarn on 26.02.16.
 */

fun processPdfUpload(file: ByteArray): Map<String, ByteArray> {
    val result = mutableMapOf<String, ByteArray>()

    val document = PDFDocument()
    document.load(file.inputStream())
    val renderer = SimpleRenderer()
    renderer.resolution = 300
    val qrImages = renderer.render(document)

    logger.info("Rendering completed")

    for (i in 0..qrImages.lastIndex) {
        logger.info("$i processing started")
        val image = qrImages[i] as BufferedImage
        //        FileOutputStream("$i.png").use { it.write(getImageAsByteArray(image)) }
        val byteImage = getImageAsByteArray(image)
        result[readQR(image)] = byteImage
    }
    return result
}

private val codeReader = QRCodeReader()

/**
 * @throws NotFoundException code not found
 */
private fun readQR(image: BufferedImage): String {
    val cropped = image.getSubimage(
            (image.width * 0.8f).toInt(),
            (image.height * 0.85f).toInt(),
            (image.width * 0.15f).toInt(),
            (image.height * 0.1f).toInt()
    )
    val qrImage = BufferedImage(cropped.width, cropped.height, BufferedImage.TYPE_BYTE_BINARY)
    val bwGraphics = qrImage.createGraphics()
    bwGraphics.drawImage(cropped, 0, 0, null)
    FileOutputStream("bw.png").use { it.write(getImageAsByteArray(qrImage)) }

    val binaryBitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(qrImage)))
    val qrResult = codeReader.decode(binaryBitmap)
    val result = qrResult.text
    logger.info("found $result")
    return result
}