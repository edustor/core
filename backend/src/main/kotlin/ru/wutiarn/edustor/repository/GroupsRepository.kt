package ru.wutiarn.edustor.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.repository.MongoRepository
import ru.wutiarn.edustor.models.Group

interface GroupsRepository : MongoRepository<Group, String>, GroupsRepositoryCustom

interface GroupsRepositoryCustom {
    fun destroyGroup(group: Group)
}

@Suppress("unused")
private class GroupsRepositoryImpl @Autowired constructor(val mongo: MongoOperations, val userRepo: UserRepository) : GroupsRepositoryCustom {
    override fun destroyGroup(group: Group) {
        userRepo.processGroupDestroy(group)
        mongo.remove(group)
    }
}