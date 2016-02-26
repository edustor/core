package ru.wutiarn.edustor.utils

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.slf4j.LoggerFactory
import java.io.FileOutputStream

val logger = LoggerFactory.getLogger("wu.wutiarn.edustor.utils.pdfGenerator")


/**
 * Created by wutiarn on 26.02.16.
 */

fun processPdfUpload(file: ByteArray): Map<String, ByteArray> {
    val images = mutableMapOf<String, ByteArray>()

    val pdDocument = PDDocument.load(file)
    pdDocument.use {
        val pdfRenderer = PDFRenderer(pdDocument)
        val qrCodeReader = QRCodeReader()

        for (i in 0..pdDocument.numberOfPages-1) {
            logger.info("Processing page $i")
            val qr_image = pdfRenderer.renderImageWithDPI(i, 100f)
            logger.info("Done qr image $i")

//            FileOutputStream("$i.png").use { it.write(getImageAsByteArray(page_img)) }

            try {
                val qrResult = qrCodeReader.decode(BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(qr_image))),
                        mapOf(DecodeHintType.TRY_HARDER to false))
                val uuid = qrResult.text

                logger.info("found: $uuid")
                val byteImage = getImageAsByteArray(pdfRenderer.renderImageWithDPI(i, 300f))
                logger.info("Done converting $i")

                images[uuid] = byteImage
            } catch(e: NotFoundException) {
                logger.info("not found")
                continue
            }
        }
    }

    return images

}