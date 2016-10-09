package ru.edustor.migration.repository.mongo

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Document
import ru.edustor.migration.model.mongo.MongoDocument
import java.time.Instant

@Repository
interface MongoDocumentsRepository : MongoRepository<MongoDocument, String> {
    fun findByUuid(uuid: String): Document?
    fun findByRemovedOnLessThan(removedOn: Instant): List<Document>

}