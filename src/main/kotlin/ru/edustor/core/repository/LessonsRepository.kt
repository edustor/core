package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Account
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import java.time.Instant

interface LessonsRepository : JpaRepository<Lesson, String> {
    fun findByFolder(folder: Folder): List<Lesson>
    fun findByOwner(account: Account): List<Lesson>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Lesson>
}