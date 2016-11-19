package ru.edustor.core.service

import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.edustor.core.model.Page
import ru.edustor.core.model.internal.pdf.PdfUploadPreferences
import ru.edustor.core.pdf.storage.PdfStorage
import ru.edustor.core.pdf.upload.PdfPage
import ru.edustor.core.pdf.upload.PdfProcessor
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.PageRepository
import rx.Observable
import rx.schedulers.Schedulers
import java.io.InputStream
import java.time.Instant
import com.itextpdf.text.Document as PdfDocument

@Service
class PdfUploadService @Autowired constructor(
        private val pdfStorage: PdfStorage,
        private val pageRepo: PageRepository,
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
                telegramService.sendText(uploader, "Found pending upload request. Using ${pendingUpload.lesson} (${pendingUpload.lesson.id}) as target lesson")
                uploadPreferences.lesson = pendingUpload.lesson
                uploader.pendingUpload = null
                accountRepository.save(uploader)
            }
        }

        Observable.range(1, pageCount)
                .observeOn(Schedulers.computation())
                .map {

                    var pdfPage: PdfPage

                    try {
                        pdfPage = document.getPage(it)
                        logger.info("Saving ${pdfPage.pageNumber}")
                        savePage(pdfPage, uploadPreferences)
                    } catch (e: Exception) {
                        pdfPage = PdfPage(it, null, null, null, null, exception = e)
                    }

                    pdfPage
                }
                .map { pdfPage ->
                    val pageNumber = pdfPage.pageNumber

                    if (pdfPage.exception == null) {
                        val shortUUID = pdfPage.qrData?.split("-")?.last()
                        val lessonInfo = pdfPage.lesson?.let { "$it" } ?: "Not registered"
                        var resultString = "[OK] Page $pageNumber. UUID $shortUUID: $lessonInfo"


                        if (pdfPage.qrData == null && pdfPage.lesson == null) {
                            resultString = "[NOT RECOGNISED] Page $pageNumber"
                            telegramService.sendText(uploader, resultString)

                            telegramService.sendImage(uploader, pdfPage.preview!!, "Img $pageNumber")

                            for (i in 0..pdfPage.qrImages!!.lastIndex) {
                                telegramService.sendImage(uploader, pdfPage.qrImages!![i], "Img $pageNumber loc $i")
                            }
                        } else {
                            telegramService.sendText(uploader, resultString)
                        }

                        logger.info(resultString)
                    } else {
                        val resultString = "[FAIL] Page $pageNumber. Cause: ${pdfPage.exception}"
                        telegramService.sendText(uploader, resultString)
                        logger.warn(resultString, pdfPage.exception)
                    }

                    pdfPage.preview = null
                    pdfPage.qrImages = null
                    pdfPage.binary = null

                    pdfPage
                }
                .toList()
                .subscribe {
                    fcmService.sendUserSyncNotification(uploader)
                    telegramService.onUploadingComplete(it, uploadPreferences)
                }
    }

    private fun savePage(pdfPage: PdfPage, uploadPreferences: PdfUploadPreferences) {

        val page: Page = pdfPage.qrData?.let { pageRepo.findByQr(pdfPage.qrData) } ?: let {
            val doc = Page(qr = pdfPage.qrData)
            uploadPreferences.lesson ?: let {
                logger.warn("Not found page ${pdfPage.pageNumber} in database: ${pdfPage.qrData} and explicit " +
                        "lesson was not provided")
                return
            }
            doc
        }

        uploadPreferences.lesson?.let {
            page.lesson = it
        }

        pdfStorage.put(page.id, pdfPage.binary!!.inputStream(), pdfPage.binary!!.size.toLong())

        page.isUploaded = true
        page.uploadedTimestamp = Instant.now()
        page.contentType = "application/pdf"
        page.owner = uploadPreferences.uploader
        pageRepo.save(page)

        pdfPage.lesson = page.lesson
    }
}