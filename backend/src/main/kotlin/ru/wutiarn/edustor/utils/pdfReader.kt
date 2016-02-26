package ru.wutiarn.edustor.utils

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

val logger = LoggerFactory.getLogger("ru.wutiarn.edustor.utils.pdfGenerator")


/**
 * Created by wutiarn on 26.02.16.
 */

fun processPdfUpload(file: ByteArray): Map<String, ByteArray> {
    val result = mutableMapOf<String, ByteArray>()

    val document = PDFDocument()
    document.load(file.inputStream())
    val renderer = SimpleRenderer()
    renderer.resolution = 100
    val qrImages = renderer.render(document)

    val qrCodeReader = QRCodeReader()

    renderer.resolution = 300
    for (i in 0..qrImages.lastIndex) {
        //            FileOutputStream("$i.png").use { it.write(getImageAsByteArray(page_img)) }

        val qrImage = qrImages[i] as BufferedImage

        try {
            val qrResult = qrCodeReader.decode(BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(qrImage))),
                    mapOf(DecodeHintType.TRY_HARDER to true))
            val uuid = qrResult.text

            logger.info("found: $uuid")
            val image = renderer.render(document, i, i).first() as BufferedImage
            val byteImage = getImageAsByteArray(image)
            logger.info("Done converting")

            result[uuid] = byteImage
        } catch(e: NotFoundException) {
            logger.info("not found")
            continue
        }
    }

    return result

}