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
import rx.lang.kotlin.onError
import rx.schedulers.Schedulers
import java.io.InputStream
import java.time.Instant
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

        telegramService.onUploadingStarted(uploadPreferences.uploader)

        val document = PdfProcessor(fileStream)

        Observable.range(1, document.pageCount)
                .observeOn(Schedulers.computation())
                .map { document.getPage(it) }
                .map {
                    logger.info("Saving ${it.pageNumber}")
                    savePage(it, uploadPreferences)
                    logger.info("completed: ${it.pageNumber} ${it.uuid}")
                    it
                }
                .onError {
                    logger.warn("Error occurred while processing page", it)
                }
                .toList()
                .subscribe {
                    fcmService.sendUserSyncNotification(uploadPreferences.uploader)
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
            pdfStorage.put(it.id, page.binary.inputStream())

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