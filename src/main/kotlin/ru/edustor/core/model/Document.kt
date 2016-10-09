package ru.edustor.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import ru.edustor.core.pdf.storage.PdfStorage
import java.time.Instant
import java.util.*
import javax.persistence.*

@Configuration
@Entity
open class Document() {

    @OneToOne(cascade = arrayOf(CascadeType.ALL), optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore lateinit var owner: Account

    @Id var localId: String = UUID.randomUUID().toString()
    var uuid: String = UUID.randomUUID().toString()


    @Column(nullable = false)
    var isUploaded: Boolean = false


    @Column(nullable = false)
    var timestamp: Instant = Instant.now()

    var qr: String? = null
    var contentType: String? = null
    var uploadedTimestamp: Instant? = null
    var removedOn: Instant? = null

    val fileMD5: String?
        get() = ps.getMD5(uuid)

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

    constructor(qr: String?, owner: Account, timestamp: Instant, uuid: String) : this() {
        this.qr = qr
        this.owner = owner
        this.timestamp = timestamp
        this.uuid = uuid
    }

    constructor(qr: String?) : this() {
        this.qr = qr
    }

    @Autowired
    fun setPdfStorage(ps: PdfStorage) {
        Document.ps = ps
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Document) return false
        return localId == other.localId
    }

    override fun hashCode(): Int {
        return localId.hashCode()
    }
}