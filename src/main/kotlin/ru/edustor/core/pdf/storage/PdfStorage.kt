package ru.edustor.core.pdf.storage

import com.mongodb.gridfs.GridFSDBFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsCriteria
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
open class PdfStorage(val gfs: GridFsOperations) {

    val logger: Logger = LoggerFactory.getLogger(PdfStorage::class.java)

    fun put(id: String, content: InputStream) {
        val existedQuery = Query.query(GridFsCriteria.whereFilename().`is`(id))
        gfs.delete(existedQuery)

        logger.debug("Saving PDF $id...")
        gfs.store(content, id, "application/pdf")
        logger.info("Save finished: $id")
    }

    fun get(id: String): InputStream? {
        logger.debug("Accessing PDF $id")
        return findGFSObject(id)?.inputStream
    }

    fun delete(id: String) {
        gfs.delete(Query.query(GridFsCriteria.whereFilename().`is`(id)))
    }

    fun getMD5(id: String): String? {
        return findGFSObject(id)?.mD5
    }

    private fun findGFSObject(id: String): GridFSDBFile? {
        return gfs.findOne(Query.query(GridFsCriteria.whereFilename().`is`(id)))
    }
}