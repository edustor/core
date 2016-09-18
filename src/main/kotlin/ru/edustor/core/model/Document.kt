package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import ru.edustor.core.pdf.storage.PdfStorage
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
        get() = ps.getMD5(id)

    var removedOn: Instant? = null

    var removed: Boolean = false
        set(value) {
            field = value
            if (value) {
                removedOn = Instant.now()
            } else {
                removedOn = null
            }
        }

    companion object {
        lateinit private var ps: PdfStorage
    }

    @Autowired
    fun setPdfStorage(ps: PdfStorage) {
        Document.ps = ps
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