package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Document

@Repository
interface DocumentsRepository : MongoRepository<Document, String> {
    fun findByUuid(uuid: String): Document?
}