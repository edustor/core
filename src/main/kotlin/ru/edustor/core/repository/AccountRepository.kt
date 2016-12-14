package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Account

@Repository
interface AccountRepository : JpaRepository<Account, String> {
    fun findByTelegramLinkToken(token: String): Account?
    fun findByTelegramChatId(token: String): Account?
}

fun AccountRepository.getForAccountId(id: String): Account {
    return this.findOne(id) ?: let {
        val a = Account(id)
        this.save(a)
        a
    }
}