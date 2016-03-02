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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import ru.wutiarn.edustor.repository.DocumentsRepository
import rx.Observable
import rx.schedulers.Schedulers
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.InputStream
import java.util.concurrent.Executors

val logger = LoggerFactory.getLogger("ru.wutiarn.edustor.utils.pdfReader")


/**
 * Created by wutiarn on 26.02.16.
 */
val renderThreadExecutor = Executors.newSingleThreadExecutor(CustomizableThreadFactory("pdf-render"));
fun processPdfUpload(fileStream: InputStream) {
    val document = PDFDocument()
    document.load(fileStream)

    Observable.range(0, document.pageCount)
            .observeOn(Schedulers.from(renderThreadExecutor))
            .map { processPdfPage(document, it) }
            .observeOn(Schedulers.computation())
            .map { Pair(it.first, it.second.getAsByteArray()) }
            //            .map { savePage(it.first, it.second) }
            .subscribe() { logger.info("completed: ${it.first}") }
}

@Autowired var gfs: GridFsOperations? = null
@Autowired var documentRepo: DocumentsRepository? = null
fun savePage(uuid: String?, image: ByteArray) {
    uuid?.let {
        val findByUuid = documentRepo!!.findByUuid(uuid)

        findByUuid?.let {
            val gridFSFile = gfs!!.store(image.inputStream(), uuid)
            it.fileId = gridFSFile
            documentRepo!!.save(it)
        }
    }
}

val renderer = SimpleRenderer().let { it.resolution = 300; it }
private fun processPdfPage(document: PDFDocument, page: Int): Pair<String?, BufferedImage> {
    val image = renderer.render(document, page, page).first() as BufferedImage
    //            FileOutputStream("$i.png").use { it.write(image.getAsByteArray()) }
    try {
        val uuid = readQR(image)
        return Pair(uuid, image)
    } catch (e: NotFoundException) {
        logger.warn("not found")
        return Pair(null, image)
    }

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