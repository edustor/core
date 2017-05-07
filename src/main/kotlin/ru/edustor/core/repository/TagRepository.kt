package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Tag
import java.time.Instant

@Repository
interface TagRepository : JpaRepository<Tag, String> {
    fun findByRemovedOnLessThan(removedOn: Instant): List<Tag>
}