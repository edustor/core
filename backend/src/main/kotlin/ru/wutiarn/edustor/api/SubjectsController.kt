package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Group
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.GroupsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.repository.SubjectsRepository

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/subjects")
class SubjectsController @Autowired constructor(val repo: SubjectsRepository,
                                                val groupsRepo: GroupsRepository,
                                                val lessonsRepository: LessonsRepository,
                                                val subjectsRepository: SubjectsRepository) {

    @RequestMapping("/list")
    fun listSubjects(@AuthenticationPrincipal user: User): List<Subject> {
        val result = user.groups.flatMap { subjectsRepository.findByGroupsContaining(it) }.toMutableList()
        result.addAll(subjectsRepository.findByOwner(user))
        return result
    }

    @RequestMapping("/{subject}/lessons")
    fun listTimetable(subject: Subject?): List<Lesson> {
        subject ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lessonsRepository.findBySubject(subject).filter { it.documents.isNotEmpty() }
    }

    @RequestMapping("/create")
    fun createSubject(@AuthenticationPrincipal user: User, @RequestParam name: String, @RequestParam year: Int, @RequestParam(required = false) group: Group?): Subject {

        val subject = Subject(name, year, user)

        group?.let {
            if (user !in group.owners) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You're not owner of this group")
            subject.groups.add(group)
        }

        repo.save(subject)

        return subject

    }
}
