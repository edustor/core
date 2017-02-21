package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.model.Tag
import java.time.Instant

interface LessonRepository : MongoRepository<Lesson, String> {
    fun findByTagId(tag: Tag): List<Lesson>
    fun findByTagIdIn(tags: List<Tag>): List<Lesson>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Lesson>

    fun findByPagesQr(qr: String): Lesson?
    fun findByPagesRemovedOnLessThan(removedOn: Instant): List<Page>
}