package ru.wutiarn.edustor.services

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsCriteria
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.utils.getAsByteArray
import rx.Observable
import rx.lang.kotlin.toObservable
import rx.schedulers.Schedulers
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.concurrent.Executors

@Service
class PdfReaderService @Autowired constructor(
        private val gfs: GridFsOperations,
        private val documentRepo: DocumentsRepository
) {
    private val logger = LoggerFactory.getLogger(PdfReaderService::class.java)
    private val renderThreadExecutor = Executors.newSingleThreadExecutor(CustomizableThreadFactory("pdf-render"));
    private val renderer = SimpleRenderer().let { it.resolution = 300; it }
    private val codeReader = QRCodeReader()
    private val QR_DOWNSCALE_SIZE = 200

    data class Page(
            val index: Int,
            var qrResult: String = "",
            var renderedImage: BufferedImage? = null
    )

    /**
     * Created by wutiarn on 26.02.16.
     */
    fun processPdfUpload(fileStream: InputStream) {
        val document = PDFDocument()
        document.load(fileStream)

        Observable.defer { getPageRanges(document.pageCount).toObservable() }
                .observeOn(Schedulers.from(renderThreadExecutor))
                .flatMap { renderer.render(document, it.first, it.second).toObservable() }
                .observeOn(Schedulers.computation())
                .map { it as BufferedImage }
                .map { Pair(readQR(it), it) }
                .map { Pair(it.first, it.second.getAsByteArray()) }
                .subscribe() {
                    savePage(it.first, it.second)
                    logger.info("completed: ${it.first}")
                }
    }

    private fun savePage(uuid: String?, image: ByteArray) {
        uuid?.let {
            val findByUuid = documentRepo.findByUuid(uuid)

            findByUuid?.let({
                val existedQuery = Query.query(GridFsCriteria.whereFilename().`is`(uuid))
                val existed = gfs.findOne(existedQuery)
                existed?.let {
                    val newMD5 = DigestUtils.md5DigestAsHex(image)
                    if (newMD5 == existed.mD5) {
                        logger.info("Old and new files are the same: $uuid")
                        return
                    } else {
                        gfs.delete(existedQuery)
                    }
                }
                gfs.store(image.inputStream(), uuid)
                it.isUploaded = true
                documentRepo.save(it)
                return
            })
            logger.warn("Not found in database: $uuid")
        }
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
                (image.height * 0.85f).toInt(),
                (image.width * 0.15f).toInt(),
                (image.height * 0.1f).toInt()
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