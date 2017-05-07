package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Lesson
import java.time.Instant

interface LessonRepository : JpaRepository<Lesson, String> {
    fun findByRemovedOnLessThan(removedOn: Instant): List<Lesson>
}