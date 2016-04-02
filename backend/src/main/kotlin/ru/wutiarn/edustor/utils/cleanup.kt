package ru.wutiarn.edustor.utils

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CleanupUtils {

    val logger = LoggerFactory.getLogger(CleanupUtils::class.java)
    @Autowired lateinit var mongoOperations: MongoOperations

    @Autowired
    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Moscow")
    fun CleanupUnusedLessons() {
        logger.info("Lesson's cleanup initiated")
        mongoOperations.remove(Query.query(Criteria.where("documents").size(0)), "lesson")
        logger.info("Lesson's cleanup completed")
    }
}