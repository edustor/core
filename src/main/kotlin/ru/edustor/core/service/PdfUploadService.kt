package ru.edustor.core.service

import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.edustor.core.model.Document
import ru.edustor.core.model.internal.pdf.PdfUploadPreferences
import ru.edustor.core.pdf.storage.PdfStorage
import ru.edustor.core.pdf.upload.PdfPage
import ru.edustor.core.pdf.upload.PdfProcessor
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import rx.Observable
import rx.schedulers.Schedulers
import java.io.InputStream
import java.time.Instant
import java.time.format.DateTimeFormatter
import com.itextpdf.text.Document as PdfDocument

@Service
class PdfUploadService @Autowired constructor(
        private val pdfStorage: PdfStorage,
        private val documentRepo: DocumentsRepository,
        private val lessonsRepository: LessonsRepository,
        private val telegramService: TelegramService,
        private val fcmService: FCMService
) {
    private val logger = LoggerFactory.getLogger(PdfUploadService::class.java)
    private val httpClient = OkHttpClient()

    fun processFromURL(url: String, uploadPreferences: PdfUploadPreferences) {
        telegramService.sendText(uploadPreferences.uploader, "Downloading file...")

        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()

        val bodyStream = response.body().byteStream()
        processPdfUpload(bodyStream, uploadPreferences)
    }

    fun processPdfUpload(fileStream: InputStream, uploadPreferences: PdfUploadPreferences) {
        val document = PdfProcessor(fileStream)
        val pageCount = document.pageCount

        val uploader = uploadPreferences.uploader
        telegramService.sendText(uploader, "Processing $pageCount pages")

        Observable.range(1, pageCount)
                .observeOn(Schedulers.computation())
                .map {

                    var page: PdfPage

                    try {
                        page = document.getPage(it)
                        logger.info("Saving ${page.pageNumber}")
                        savePage(page, uploadPreferences)
                    } catch (e: Exception) {
                        page = PdfPage(it, null, null, null, null, exception = e)
                    }

                    page
                }
                .map { page ->
                    val pageNumber = page.pageNumber

                    if (page.exception == null) {
                        val shortUUID = page.uuid?.split("-")?.last()
                        val lessonInfo = page.lesson?.let { "${it.subject?.name}. ${it.topic ?: "No topic"}. ${it.date?.format(DateTimeFormatter.ISO_LOCAL_DATE)}" } ?: "Not registered"
                        var resultString = "[OK] Page $pageNumber. UUID $shortUUID: $lessonInfo"


                        if (page.uuid == null && page.lesson == null) {
                            resultString = "[NOT RECOGNISED] Page $pageNumber"
                            telegramService.sendText(uploader, resultString)

                            telegramService.sendImage(uploader, page.preview!!, "Img $pageNumber")

                            for (i in 0..page.qrImages!!.lastIndex) {
                                telegramService.sendImage(uploader, page.qrImages!![i], "Img $pageNumber loc $i")
                            }
                        } else {
                            telegramService.sendText(uploader, resultString)
                        }

                        logger.info(resultString)
                    } else {
                        val resultString = "[FAIL] Page $pageNumber. Cause: ${page.exception}"
                        telegramService.sendText(uploader, resultString)
                        logger.warn(resultString, page.exception)
                    }

                    page.preview = null
                    page.qrImages = null
                    page.binary = null

                    page
                }
                .toList()
                .subscribe {
                    fcmService.sendUserSyncNotification(uploader)
                    telegramService.onUploadingComplete(it, uploadPreferences)
                }
    }

    private fun savePage(page: PdfPage, uploadPreferences: PdfUploadPreferences) {

        var document: Document? = null

        if (uploadPreferences.lesson != null) {
            document = Document(uuid = page.uuid)
        } else if (page.uuid != null) {
            document = documentRepo.findByUuid(page.uuid)
        } else {
            logger.warn("Page ${page.pageNumber}: No uuid found")
        }

        document?.let {
            pdfStorage.put(it.id, page.binary!!.inputStream())

            it.isUploaded = true
            it.uploadedTimestamp = Instant.now()
            it.contentType = "application/pdf"
            it.owner = uploadPreferences.uploader
            documentRepo.save(it)
            uploadPreferences.lesson?.let {
                uploadPreferences.lesson?.documents?.add(document!!)
                lessonsRepository.save(it)
            }

            page.lesson = lessonsRepository.findByDocumentsContaining(it)

            return
        }

        logger.warn("Not found page ${page.pageNumber} in database: ${page.uuid}")
    }
}