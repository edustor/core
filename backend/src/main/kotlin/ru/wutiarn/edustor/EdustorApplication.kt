package ru.wutiarn.edustor

import com.mongodb.WriteConcern
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.mongodb.core.MongoTemplate
import java.util.*

@SpringBootApplication
open class EdustorApplication {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @Autowired
    fun configureMongoTemplate(mongoTemplate: MongoTemplate) {
        mongoTemplate.setWriteConcern(WriteConcern.ACKNOWLEDGED)
    }
}