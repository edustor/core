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
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.DocumentsRepository
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
        private val accountRepository: AccountRepository,
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

        if (uploadPreferences.lesson == null) {
            val pendingUpload = uploader.pendingUpload
            if (pendingUpload != null && pendingUpload.validUntil > Instant.now()) {
                telegramService.sendText(uploader, "Found pending upload request. Using ${pendingUpload.lesson.id} as target lesson")
                uploadPreferences.lesson = pendingUpload.lesson
                uploader.pendingUpload = null
                accountRepository.save(uploader)
            }
        }

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
                        val shortUUID = page.qrData?.split("-")?.last()
                        val lessonInfo = page.lesson?.let { "${it.subject.name}. ${it.topic ?: "No topic"}. ${it.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}" } ?: "Not registered"
                        var resultString = "[OK] Page $pageNumber. UUID $shortUUID: $lessonInfo"


                        if (page.qrData == null && page.lesson == null) {
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

        val document: Document = page.qrData?.let { documentRepo.findByQr(page.qrData) } ?: let {
            val doc = Document(qr = page.qrData)
            uploadPreferences.lesson ?: let {
                logger.warn("Not found page ${page.pageNumber} in database: ${page.qrData} and explicit " +
                        "lesson was not provided")
                return
            }
            doc
        }

        uploadPreferences.lesson?.let {
            document.lesson = it
        }

        pdfStorage.put(document.id, page.binary!!.inputStream())

        document.isUploaded = true
        document.uploadedTimestamp = Instant.now()
        document.contentType = "application/pdf"
        document.owner = uploadPreferences.uploader
        documentRepo.save(document)

        page.lesson = document.lesson
    }
}