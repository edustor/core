package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Account
import java.time.Instant

@Repository
interface AccountRepository : MongoRepository<Account, String> {
    fun findByTagsRemovedOnLessThan(removedOn: Instant): List<Account>

}

fun AccountRepository.getForAccountId(id: String): Account {
    return this.findOne(id) ?: let {
        val a = Account(id)
        this.save(a)
        a
    }
}