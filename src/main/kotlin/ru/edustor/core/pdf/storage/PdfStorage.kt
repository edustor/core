package ru.edustor.core.pdf.storage

import io.minio.MinioClient
import io.minio.errors.MinioException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
open class PdfStorage() {

    val BUCKET_NAME = "edustor-pages"

    val binaryStorage: MinioClient
    val logger: Logger = LoggerFactory.getLogger(PdfStorage::class.java)

    init {
        val storageURL = System.getenv("BS_URL") ?: throw IllegalArgumentException("Binary storage URL was not provided. Please set BS_URL environment variable.")
        val storageAccessKey = System.getenv("BS_ACCESS_KEY") ?: throw IllegalArgumentException("Binary storage access token was not provided. Please set BS_ACCESS_KEY environment variable.")
        val storageSecretKey = System.getenv("BS_SECRET_KEY") ?: throw IllegalArgumentException("Binary storage secret token was not provided. Please set BS_SECRET_KEY environment variable.")

        binaryStorage = MinioClient(storageURL, storageAccessKey, storageSecretKey)
        if (!binaryStorage.bucketExists(BUCKET_NAME)) {
            binaryStorage.makeBucket(BUCKET_NAME)
        }
    }

    fun put(id: String, content: InputStream, size: Long) {
        logger.debug("Saving PDF $id...")
        binaryStorage.putObject(BUCKET_NAME, "$id.pdf", content, size, "application/pdf")
        logger.info("Save finished: $id")
    }

    fun get(id: String): InputStream? {
        logger.debug("Accessing PDF $id")
        val fileName = "$id.pdf"

        try {
            return binaryStorage.getObject(BUCKET_NAME, fileName)
        } catch (e: MinioException) {
            return null
        }
    }

    fun delete(id: String) {
        binaryStorage.removeObject(BUCKET_NAME, "$id.pdf")
    }
}