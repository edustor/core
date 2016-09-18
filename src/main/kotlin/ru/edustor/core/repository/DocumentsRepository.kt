package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Document
import java.time.Instant

@Repository
interface DocumentsRepository : MongoRepository<Document, String> {
    fun findByUuid(uuid: String): Document?
    fun findByRemovedOnLessThan(removedOn: Instant): List<Document>

}