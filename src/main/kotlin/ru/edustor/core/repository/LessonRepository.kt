package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Tag
import java.time.Instant

interface LessonRepository : JpaRepository<Lesson, String> {
    fun findByTag(tag: Tag): List<Lesson>
    fun findByTagIn(tags: List<Tag>): List<Lesson>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Lesson>
}