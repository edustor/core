package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import ru.wutiarn.edustor.models.Document

/**
 * Created by wutiarn on 22.02.16.
 */
@Repository
interface DocumentRepository : MongoRepository<Document, String> {
    @Query()
    fun findByUuid(uuid: String): Document?
}