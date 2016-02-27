package ru.wutiarn.edustor.utils

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.ResultPointCallback
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

    logger.info("Rendering completed")

    val codeReader = QRCodeReader()

    val found = mutableMapOf<Int, String>()
    for (i in 0..qrImages.lastIndex) {
        logger.info("$i processing started")


        val image = qrImages[i] as BufferedImage
        FileOutputStream("$i.png").use { it.write(getImageAsByteArray(image)) }

        val crop_x = (image.width * 0.8f).toInt()
        val crop_y = (image.height * 0.85f).toInt()
        val cropped = image.getSubimage(
                crop_x,
                crop_y,
                image.width - crop_x,
                image.height - crop_y
        )

        val qrImage = BufferedImage(cropped.width, cropped.height, BufferedImage.TYPE_BYTE_BINARY)
        val bwGraphics = qrImage.createGraphics()

        bwGraphics.drawImage(cropped, 0, 0, null)

        FileOutputStream("${i}_bw.png").use { it.write(getImageAsByteArray(qrImage)) }

        logger.info("$i prepared")


        try {

            val bufferedImageLuminanceSource = BufferedImageLuminanceSource(qrImage)

            val hybridBinarizer = HybridBinarizer(bufferedImageLuminanceSource)

            val binaryBitmap = BinaryBitmap(hybridBinarizer)

            val qrResult = codeReader.decode(binaryBitmap, mapOf(
                    DecodeHintType.NEED_RESULT_POINT_CALLBACK to ResultPointCallback {
                        it.toString()
                    }
            ))
            val uuid = qrResult.text

            logger.info("$i found: $uuid")
            found[i] = uuid

            val byteImage = getImageAsByteArray(image)
            result[uuid] = byteImage

        } catch(e: NotFoundException) {
            logger.info("$i not found")
            continue
        } finally {
            logger.info("$i finished")
        }
    }

    return result

}