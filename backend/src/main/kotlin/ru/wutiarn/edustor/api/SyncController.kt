package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.repository.SubjectsRepository

@RestController
@RequestMapping("/api/sync")
class SyncController @Autowired constructor(
        val subjectRepo: SubjectsRepository,
        val lessonRepo: LessonsRepository
) {
    @RequestMapping("/fetch")
    fun fetch(@AuthenticationPrincipal user: User): Map<*, *> {
        val subjects = subjectRepo.findByOwner(user)
        val lessons = lessonRepo.findBySubjectIn(subjects)
        return mapOf(
                "user" to user,
                "subjects" to subjects,
                "lessons" to lessons
        )
    }
}
