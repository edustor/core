package ru.edustor.core.models

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsCriteria
import org.springframework.data.mongodb.gridfs.GridFsOperations
import java.time.Instant
import java.util.*

@Configuration
@org.springframework.data.mongodb.core.mapping.Document
open class Document(
        owner: User? = null,
        @Indexed var uuid: String? = null,
        var isUploaded: Boolean = false,
        var contentType: String? = null,
        var timestamp: Instant = Instant.now(),
        var uploadedTimestamp: Instant? = null,
        @Id var id: String = UUID.randomUUID().toString()
) {
    @DBRef @JsonIgnore lateinit var owner: User

    val fileMD5: String?
        get() = gridFs.findOne(Query.query(GridFsCriteria.whereFilename().`is`(id)))?.mD5

    companion object {
        lateinit var gridFs: GridFsOperations
    }

    @Autowired
    fun setGridFS(gridFs: GridFsOperations) {
        Document.gridFs = gridFs
    }

    init {
        owner?.let {
            this.owner = it
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Document) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}