package ru.edustor.core.pdf.upload

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import org.ghost4j.renderer.SimpleRenderer
import org.slf4j.LoggerFactory
import ru.edustor.core.util.extensions.getAsByteArray
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.ghost4j.document.PDFDocument as G4JDocument


class PdfProcessor(val inStream: InputStream) {

    private val logger = LoggerFactory.getLogger(PdfProcessor::class.java)

    private val docReader: PdfReader
    private val g4jdoc = G4JDocument()
    private val renderer = SimpleRenderer()

    companion object {
        private val ghostscriptLock = ReentrantLock()
    }

    init {
        renderer.resolution = 150

        val inBytes = ByteArrayOutputStream().use {
            inStream.copyTo(it)
            it.toByteArray()
        }
        docReader = PdfReader(inBytes)
        g4jdoc.load(inBytes.inputStream())
    }

    fun getPage(n: Int): PdfPage {
        Thread.sleep(500)
        val pageBin = extractPage(n)
        val preview = renderPage(n)
        val (uuid, qrImages) = readQR(preview)

        return PdfPage(n, pageBin, preview, qrImages, uuid)
    }

    val pageCount: Int
        get() = g4jdoc.pageCount

    private fun renderPage(n: Int): BufferedImage {
        ghostscriptLock.withLock {
            return renderer.render(g4jdoc, n - 1, n - 1)[0] as BufferedImage
        }
    }

    private fun extractPage(n: Int): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()

        val document = Document()
        val pdfCopy = PdfCopy(document, byteArrayOutputStream)
        document.open()

        val importedPage = pdfCopy.getImportedPage(docReader, n)
        pdfCopy.addPage(importedPage)
        document.close()
        pdfCopy.close()

        return byteArrayOutputStream.toByteArray()
    }

    private fun readQR(image: BufferedImage): Pair<String?, List<BufferedImage>> {
        val QR_REGION_SIZE = 150

        val qrCodeLocations = listOf(
                image.width - QR_REGION_SIZE - 10 to image.height - QR_REGION_SIZE - 20, // Right bottom
                image.width - QR_REGION_SIZE - 10 to 20, // Right top
                10 to 20, // Left top
                10 to image.height - QR_REGION_SIZE - 20  // Left bottom
        )

        var uuid: String? = null
        val qrImages = mutableListOf<BufferedImage>()

        for (location in qrCodeLocations) {
            val cropped = image.getSubimage(
                    location.first,
                    location.second,
                    QR_REGION_SIZE,
                    QR_REGION_SIZE
            )

            val tempFile = File.createTempFile("edustor-qr", ".tmp.png")
            tempFile.writeBytes(cropped.getAsByteArray())

            val process = Runtime.getRuntime().exec(arrayOf(
                    "zbarimg", "-q", "--raw", tempFile.absolutePath
            ))


            val result = process.inputStream.reader().readLines()

            tempFile.delete()

            val foundCode = result.getOrNull(0)
            if (foundCode != null) {

                if (!foundCode.startsWith("edustor://d/")) {
                    try {
                        uuid = UUID.fromString(foundCode).toString()
                        logger.info("Found old qr code payload: $foundCode")
                    } catch (e: IllegalAccessException) {
                        logger.info("QR code payload is invalid $foundCode, skipping")
                        continue
                    }
                } else {
                    uuid = foundCode.split("/").last()
                }

                break
            }

            qrImages.add(cropped)
        }

        return uuid to qrImages.toList()
    }

}