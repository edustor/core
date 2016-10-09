package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Document
import java.time.Instant

@Repository
interface DocumentsRepository : JpaRepository<Document, String> {
    fun findByLocalId(localId: String): Document?
    fun findByUuid(uuid: String): Document?
    fun findByRemovedOnLessThan(removedOn: Instant): List<Document>

}