package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exception.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.GroupsRepository
import ru.wutiarn.edustor.repository.SubjectsRepository

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/subjects")
class SubjectsController @Autowired constructor(val repo: SubjectsRepository,
                                                val groupsRepo: GroupsRepository) {

    @RequestMapping("/list")
    fun listSubjects(@AuthenticationPrincipal user: User): List<Subject> {
        return user.groups.flatMap { it.subjects }
    }

    @RequestMapping("/create")
    fun createSubject(@AuthenticationPrincipal user: User, @RequestParam name: String, @RequestParam year: Int, @RequestParam("group") groupId: String): Subject {
        val group = groupsRepo.findOne(groupId)
        if (user !in group.owners) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN)
        val subject = Subject(name, year, mutableListOf(group))
        group.subjects.add(subject)
        repo.save(subject)
        groupsRepo.save(group)
        return subject
    }
}