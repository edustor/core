package ru.edustor.core.util

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.annotation.PostConstruct

@Component
open class CleanupUtils {

    val logger = LoggerFactory.getLogger(CleanupUtils::class.java)
    @Autowired lateinit var mongoOperations: MongoOperations

    @PostConstruct
    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Moscow")
    fun CleanupUnusedLessons() {
        logger.info("Lesson's cleanup initiated")
        val result = mongoOperations.remove(Query.query(Criteria
                .where("documents").size(0)
                .and("date").lt(LocalDate.now().minusMonths(1))
        ), "lesson")

        logger.info("Lesson's cleanup completed. Affected: ${result.n}")
    }
}