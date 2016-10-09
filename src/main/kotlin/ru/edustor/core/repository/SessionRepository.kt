package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Account
import ru.edustor.core.model.Session

@Repository
interface SessionRepository : JpaRepository<Session, String> {
    fun findByToken(token: String): Session?
    fun findByUser(user: Account): List<Session>
}