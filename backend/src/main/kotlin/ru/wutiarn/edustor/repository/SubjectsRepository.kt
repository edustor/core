package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.wutiarn.edustor.models.Subject

/**
 * Created by wutiarn on 28.02.16.
 */
interface SubjectsRepository : MongoRepository<Subject, String>
