package ru.wutiarn.edustor.utils

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import org.slf4j.LoggerFactory
import java.awt.Image
import java.awt.image.BufferedImage

val logger = LoggerFactory.getLogger("ru.wutiarn.edustor.utils.pdfReader")


/**
 * Created by wutiarn on 26.02.16.
 */

fun processPdfUpload(file: ByteArray): Map<String, ByteArray> {
    val result = mutableMapOf<String, ByteArray>()

    val document = PDFDocument()
    document.load(file.inputStream())

    logger.info("Rendering completed")

    for (i in 0..document.pageCount-1) {
        logger.info("${i+1} processing started")
        val (uuid, image) = processPdfPage(document, i)
        logger.info("Converting to bytes")
        val byteImage = image.getAsByteArray()
        logger.info("Converting done")
        result[uuid] = byteImage
    }
    return result
}

val renderer = SimpleRenderer().let { it.resolution = 300; it }
private fun processPdfPage(document: PDFDocument, page: Int): Pair<String, BufferedImage> {
    val image = renderer.render(document, page, page).first() as BufferedImage
//            FileOutputStream("$i.png").use { it.write(image.getAsByteArray()) }
    val uuid = readQR(image)
    return Pair(uuid, image)
}

private val codeReader = QRCodeReader()
private val QR_DOWNSCALE_SIZE = 200
/**
 * @throws com.google.zxing.NotFoundException
 */
private fun readQR(image: BufferedImage): String {
    logger.info("Cropping and scaling")
    val cropped = image.getSubimage(
            (image.width * 0.8f).toInt(),
            (image.height * 0.85f).toInt(),
            (image.width * 0.15f).toInt(),
            (image.height * 0.1f).toInt()
    ).getScaledInstance(QR_DOWNSCALE_SIZE, QR_DOWNSCALE_SIZE, Image.SCALE_DEFAULT)
    logger.info("Drawing")
    val qrImage = BufferedImage(QR_DOWNSCALE_SIZE, QR_DOWNSCALE_SIZE, BufferedImage.TYPE_BYTE_BINARY)
    val bwGraphics = qrImage.createGraphics()
    bwGraphics.drawImage(cropped, 0, 0, null)
    bwGraphics.dispose()
//    FileOutputStream("bw.png").use { it.write(qrImage.getAsByteArray()) }
    logger.info("Preparing scan")
    val binaryBitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(qrImage)))
    logger.info("Scanning")
    val qrResult = codeReader.decode(binaryBitmap, mapOf(
            DecodeHintType.TRY_HARDER to true
    ))
    val result = qrResult.text
    logger.info("found $result")
    return result
}