package ru.edustor.core.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.edustor.core.models.Subject
import ru.edustor.core.models.User
import ru.edustor.core.repository.SubjectsRepository

@RestController
@RequestMapping("/api/subjects")
class SubjectsController @Autowired constructor(val repo: SubjectsRepository,
                                                val subjectsRepository: SubjectsRepository) {

    @RequestMapping("/list")
    fun listSubjects(@AuthenticationPrincipal user: User): List<Subject> {
        val result = subjectsRepository.findByOwner(user)
        return result.sorted()
    }

    @RequestMapping("/create")
    fun createSubject(@AuthenticationPrincipal user: User, @RequestParam name: String): Subject {

        val subject = Subject(name, user)
        repo.save(subject)

        return subject

    }
}
