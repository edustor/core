package ru.edustor.core.util.migrate

import org.slf4j.LoggerFactory
import ru.edustor.core.model.Account
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.repository.SubjectsRepository
import ru.edustor.core.repository.mongo.MongoLessonsRepository
import ru.edustor.core.repository.mongo.MongoSubjectsRepository
import ru.edustor.core.repository.mongo.MongoUserRepository
import javax.annotation.PostConstruct

//@Configuration
open class MongoPostgresMigrate(
        val mongoAccountRepository: MongoUserRepository,
        val accountRepository: AccountRepository,

        val mongoSubjectsRepository: MongoSubjectsRepository,
        val subjectsRepository: SubjectsRepository,

        val documentsRepository: DocumentsRepository,

        val mongoLessonsRepository: MongoLessonsRepository,
        val lessonsRepository: LessonsRepository
) {
    private val logger = LoggerFactory.getLogger(MongoPostgresMigrate::class.java)

    @PostConstruct
    fun run() {
        migrateAccounts()
        migrateSubjects()
        migrateLessons()
    }

    fun migrateAccounts() {
        val all = mongoAccountRepository.findAll()
        all.forEach { old ->
            val new = Account()
            new.email = old.email
            new.id = old.id
            new.telegramChatId = old.telegramChatId
            new.telegramLinkToken = old.telegramLinkToken

            logger.info("Migrate account: ${new.id}")

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

            logger.info("Migrate subject: ${new.id}")

            subjectsRepository.save(new)
        }
    }

    fun migrateLessons() {
        mongoLessonsRepository.findAll().forEach { old ->
            val new = Lesson()
            new.id = old.id
            new.date = old.date!!
            new.removed = old.removed
            new.removedOn = old.removedOn
            new.topic = old.topic

            new.subject = subjectsRepository.findOne(old.subject!!.id)
            new.documents.clear()
            old.documents.forEach { oldDocument ->
                val newDoc = Document()

                newDoc.localId = oldDocument.id
                newDoc.uuid = oldDocument.id
                newDoc.qr = oldDocument.uuid
                newDoc.contentType = oldDocument.contentType
                newDoc.isUploaded = oldDocument.isUploaded
                newDoc.removed = oldDocument.removed
                newDoc.removedOn = oldDocument.removedOn
                newDoc.timestamp = oldDocument.timestamp
                newDoc.uploadedTimestamp = oldDocument.uploadedTimestamp
                newDoc.owner = accountRepository.findOne(oldDocument.owner.id)

                logger.info("Migrate document: ${newDoc.localId}")

                new.documents.add(newDoc)
            }

            new.recalculateDocumentsIndexes()

            logger.info("Migrate lesson: ${new.id}")

            documentsRepository.save(new.documents)
            lessonsRepository.save(new)
        }
    }
}