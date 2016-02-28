package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.wutiarn.edustor.models.Lesson

/**
 * Created by wutiarn on 28.02.16.
 */
interface LessonsRepository : MongoRepository<Lesson, String>
