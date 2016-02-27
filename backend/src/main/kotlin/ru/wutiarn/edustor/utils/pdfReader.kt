package ru.wutiarn.edustor.utils

import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.FileOutputStream

val logger = LoggerFactory.getLogger("ru.wutiarn.edustor.utils.pdfGenerator")


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

    val codeReader = QRCodeReader()

    val found = mutableMapOf<Int, String>()
    for (i in 0..qrImages.lastIndex) {

        val qrImage = qrImages[i] as BufferedImage
        FileOutputStream("$i.png").use { it.write(getImageAsByteArray(qrImage)) }


        try {
            val qrResult = codeReader.decode(BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(qrImage))))
            val uuid = qrResult.text

            logger.info("found: $uuid")
            found[i] = uuid

            val byteImage = getImageAsByteArray(qrImage)
            result[uuid] = byteImage

        } catch(e: NotFoundException) {
            logger.info("not found")
            continue
        }
    }

    return result

}