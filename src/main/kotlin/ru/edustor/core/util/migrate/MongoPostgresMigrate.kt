package ru.edustor.core.util.migrate

import ru.edustor.core.model.Account
import ru.edustor.core.model.Subject
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.SubjectsRepository
import ru.edustor.core.repository.mongo.MongoSubjectsRepository
import ru.edustor.core.repository.mongo.MongoUserRepository
import javax.annotation.PostConstruct

//@Configuration
open class MongoPostgresMigrate(
        val mongoAccountRepository: MongoUserRepository,
        val accountRepository: AccountRepository,

        val mongoSubjectsRepository: MongoSubjectsRepository,
        val subjectsRepository: SubjectsRepository
) {
    @PostConstruct
    fun run() {
        migrateAccounts()
        migrateSubjects()
    }

    fun migrateAccounts() {
        val all = mongoAccountRepository.findAll()
        all.forEach { old ->
            val new = Account()
            new.email = old.email
            new.id = old.id
            new.telegramChatId = old.telegramChatId
            new.telegramLinkToken = old.telegramLinkToken

            accountRepository.save(new)
        }
    }

    fun migrateSubjects() {
        mongoSubjectsRepository.findAll().forEach { old ->
            val new = Subject()
            new.id = old.id
            new.name = old.name
            new.owner = accountRepository.findOne(old.owner.id)
            new.removed = old.removed
            new.removedOn = old.removedOn

            subjectsRepository.save(new)
        }
    }
}