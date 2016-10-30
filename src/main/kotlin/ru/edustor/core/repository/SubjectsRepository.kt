package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Account
import ru.edustor.core.model.Folder
import java.time.Instant

interface SubjectsRepository : JpaRepository<Folder, String> {
    fun findByOwner(user: Account): List<Folder>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Folder>
}