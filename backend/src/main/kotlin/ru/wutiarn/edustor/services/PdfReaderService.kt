package ru.wutiarn.edustor.services

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsCriteria
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import org.springframework.stereotype.Service
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.utils.UploadPreferences
import rx.Observable
import rx.lang.kotlin.toObservable
import rx.schedulers.Schedulers
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.Instant
import java.util.concurrent.Executors
import com.itextpdf.text.Document as PdfDocument

@Service
class PdfReaderService @Autowired constructor(
        private val gfs: GridFsOperations,
        private val documentRepo: DocumentsRepository,
        private val lessonsRepository: LessonsRepository
) {
    private val logger = LoggerFactory.getLogger(PdfReaderService::class.java)
    private val renderThreadExecutor = Executors.newSingleThreadExecutor(CustomizableThreadFactory("pdf-render"));
    private val renderer = SimpleRenderer().let { it.resolution = 150; it }
    private val codeReader = QRCodeReader()
    private val QR_DOWNSCALE_SIZE = 200

    data class Page(
            val index: Int,
            val renderedImage: BufferedImage,
            var uuid: String? = null
    )

    /**
     * Created by wutiarn on 26.02.16.
     */
    fun processPdfUpload(fileStream: InputStream, uploadPreferences: UploadPreferences? = null) {

        val byteOut = ByteArrayOutputStream()
        val bytes: ByteArray
        fileStream.use {
            fileStream.copyTo(byteOut)
        }
        bytes = byteOut.toByteArray()

        val rendererDocument = PDFDocument()
        rendererDocument.load(bytes.inputStream())

        val document = PdfReader(bytes)

        Observable.defer { getPageRanges(rendererDocument.pageCount).toObservable() }
                .observeOn(Schedulers.from(renderThreadExecutor))
                .flatMap {
                    renderer.render(rendererDocument, it.first, it.second).zip(it.first..it.second).toObservable()
                }
                .observeOn(Schedulers.computation())
                .map { Page(index = it.second, renderedImage = it.first as BufferedImage) }
                .map { it.uuid = readQR(it.renderedImage); it }
                .subscribe() {
                    logger.info("Saving ${it.index}")
                    savePage(it, document, uploadPreferences)
                    logger.info("completed: ${it.index} ${it.uuid}")
                }
    }

    private fun savePage(page: Page, reader: PdfReader, uploadPreferences: UploadPreferences? = null) {

        var document: Document? = null

        if (uploadPreferences?.lesson != null) {
            document = Document(uuid = page.uuid)
            uploadPreferences?.lesson?.documents?.add(document)
        } else if (page.uuid != null) {
            document = documentRepo.findByUuid(page.uuid!!)
        } else {
            logger.warn("Page ${page.index}: No uuid found")
        }

        document?.let {
            val existedQuery = Query.query(GridFsCriteria.whereFilename().`is`(page.uuid))
            gfs.delete(existedQuery)

            val bytes = getPageAsBytes(page, reader)

            gfs.store(bytes.inputStream(), it.id, "application/pdf")
            it.isUploaded = true
            it.uploadedTimestamp = Instant.now()
            it.contentType = "application/pdf"
            it.owner = uploadPreferences?.uploader
            documentRepo.save(it)
            uploadPreferences?.lesson?.let {
                lessonsRepository.save(it)
            }
            return
        }

        logger.warn("Not found page ${page.index} in database: ${page.uuid}")
    }

    private fun getPageAsBytes(page: Page, reader: PdfReader): ByteArray {
        val document = PdfDocument()
        val byteArrayOutputStream = ByteArrayOutputStream()
        val pdfCopy = PdfCopy(document, byteArrayOutputStream)
        val importedPage = pdfCopy.getImportedPage(reader, page.index + 1)

        document.open()
        pdfCopy.addPage(importedPage)
        document.close()

        return byteArrayOutputStream.toByteArray()
    }

    private fun getPageRanges(pageCount: Int, itemsPerRange: Int = 3): MutableList<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()

        for (n in 0..((pageCount - 1) / itemsPerRange)) {

            val first = n * itemsPerRange
            var last = first + itemsPerRange - 1

            if (last > pageCount) last = pageCount - 1

            result.add(Pair(first, last))
        }

        return result
    }


    /**
     * @throws com.google.zxing.NotFoundException
     */
    private fun readQR(image: BufferedImage): String? {
        logger.trace("Cropping and scaling")
        val cropped = image.getSubimage(
                (image.width * 0.8f).toInt(),
                (image.height * 0.83f).toInt(),
                (image.width * 0.15f).toInt(),
                (image.height * 0.12f).toInt()
        ).getScaledInstance(QR_DOWNSCALE_SIZE, QR_DOWNSCALE_SIZE, Image.SCALE_DEFAULT)
        logger.trace("Drawing")
        val qrImage = BufferedImage(QR_DOWNSCALE_SIZE, QR_DOWNSCALE_SIZE, BufferedImage.TYPE_BYTE_BINARY)
        val bwGraphics = qrImage.createGraphics()
        bwGraphics.drawImage(cropped, 0, 0, null)
        bwGraphics.dispose()
        //    FileOutputStream("bw.png").use { it.write(qrImage.getAsByteArray()) }
        logger.trace("Preparing scan")
        val binaryBitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(qrImage)))
        logger.trace("Scanning")
        try {
            val qrResult = codeReader.decode(binaryBitmap, mapOf(
                    DecodeHintType.TRY_HARDER to true
            ))
            val result = qrResult.text
            logger.trace("found $result")
            return result
        } catch (e: NotFoundException) {
            logger.trace("not found")
            return null
        }

    }
}