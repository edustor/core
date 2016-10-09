package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Account

@Repository
interface AccountRepository : JpaRepository<Account, String> {
    fun findByEmail(email: String): Account?
    fun findByTelegramLinkToken(token: String): Account?
    fun findByTelegramChatId(token: String): Account?
}