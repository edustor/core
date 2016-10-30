package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Document
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import java.time.Instant
import java.time.LocalDate

interface LessonsRepository : JpaRepository<Lesson, String> {
    fun findByFolder(folder: Folder): List<Lesson>
    fun findByFolderIn(folders: List<Folder>): List<Lesson>
    fun findByDocumentsContaining(document: Document): Lesson?
    fun findLessonByFolderAndDate(folder: Folder, date: LocalDate): Lesson?
    fun findByRemovedOnLessThan(removedOn: Instant): List<Lesson>
}