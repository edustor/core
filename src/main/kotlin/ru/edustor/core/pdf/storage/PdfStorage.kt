package ru.edustor.core.pdf.storage

import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsCriteria
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
open class PdfStorage(val gfs: GridFsOperations) {
    fun put(id: String, content: InputStream) {
        val existedQuery = Query.query(GridFsCriteria.whereFilename().`is`(id))
        gfs.delete(existedQuery)

        gfs.store(content, id, "application/pdf")
    }
}