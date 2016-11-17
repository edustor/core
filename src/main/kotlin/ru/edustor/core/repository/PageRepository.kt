package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Page
import java.time.Instant

@Repository
interface PageRepository : JpaRepository<Page, String> {
    fun findByQr(uuid: String): Page?
    fun findByRemovedOnLessThan(removedOn: Instant): List<Page>

}