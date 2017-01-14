package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Account
import ru.edustor.core.model.Tag
import java.time.Instant

interface SubjectRepository : JpaRepository<Tag, String> {
    fun findByOwner(user: Account): List<Tag>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Tag>
}