package ru.edustor.core.pdf.upload

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import org.ghost4j.renderer.SimpleRenderer
import org.slf4j.LoggerFactory
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.pdf.gen.BlankPdfGenerator
import ru.edustor.core.pdf.qr.QRReader
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.ghost4j.document.PDFDocument as G4JDocument


open class PdfProcessor {

    protected constructor() {
    }

    constructor(inStream: InputStream) {
        init(inStream)
    }

    protected lateinit var inStream: InputStream

    val QR_REGION_SIZE = 150

    private val logger = LoggerFactory.getLogger(PdfProcessor::class.java)

    private val qrReader = QRReader()
    private lateinit var docReader: PdfReader
    private val g4jdoc = G4JDocument()
    private val renderer = SimpleRenderer()

    companion object {
        private val ghostscriptLock = ReentrantLock()
    }

    protected fun init(inStream: InputStream) {
        this.inStream = inStream

        renderer.resolution = 150

        val inBytes = ByteArrayOutputStream().use {
            inStream.copyTo(it)
            it.toByteArray()
        }
        docReader = PdfReader(inBytes)
        g4jdoc.load(inBytes.inputStream())
    }

    fun getPage(n: Int): PdfPage {
        val pageBin = extractPage(n)
        val preview = renderPage(n)
        val (uuid, qrImages) = readQR(preview)

        return PdfPage(n, uuid, pageBin, preview, qrImages)
    }

    val pageCount: Int
        get() = g4jdoc.pageCount

    protected fun renderPage(n: Int): BufferedImage {
        ghostscriptLock.withLock {
            return renderer.render(g4jdoc, n - 1, n - 1)[0] as BufferedImage
        }
    }

    protected fun extractPage(n: Int): ByteArray {
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

    protected fun readQR(image: BufferedImage): Pair<String?, List<BufferedImage>> {

        val qrImages = mutableListOf<BufferedImage>()

        var uuid: String? = null

        for (location in BlankPdfGenerator.QRLocations.values()) {
            val (foundUuid, qrImage) = readQRAtLocation(image, location)
            qrImages.add(qrImage)
            if (foundUuid != null) {
                uuid = foundUuid
                break
            }
        }
        return uuid to qrImages.toList()
    }

    protected fun readQRAtLocation(image: BufferedImage, location: BlankPdfGenerator.QRLocations): Pair<String?, BufferedImage> {
        val searchPosition = getSearchPosition(image, location)
        val cropped = image.getSubimage(
                searchPosition.first,
                searchPosition.second,
                QR_REGION_SIZE,
                QR_REGION_SIZE
        )
        var uuid: String? = null

        val qrData = qrReader.readQR(cropped)
        if (qrData != null) {
            if (!qrData.startsWith("edustor://d/")) {
                try {
                    uuid = UUID.fromString(qrData).toString()
                    logger.info("Found old qr code payload: $qrData")
                } catch (e: IllegalAccessException) {
                    logger.info("QR code payload is invalid $qrData, skipping")
                }
            } else {
                uuid = qrData.split("/").last()
            }
        }

        return uuid to cropped
    }

    protected fun getSearchPosition(image: BufferedImage, putLocation: BlankPdfGenerator.QRLocations): Pair<Int, Int> {
        return when (putLocation.name) {
            "RIGHT_BOTTOM" -> image.width - QR_REGION_SIZE - 10 to image.height - QR_REGION_SIZE - 20
            "RIGHT_TOP" -> image.width - QR_REGION_SIZE - 10 to 20
            "LEFT_TOP" -> 10 to 20
            "LEFT_BOTTOM" -> 10 to image.height - QR_REGION_SIZE - 20
            else -> throw NotFoundException("Unknown qr location: ${putLocation.name}")
        }
    }
}