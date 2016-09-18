package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.edustor.core.model.Subject
import ru.edustor.core.model.User
import ru.edustor.core.repository.SubjectsRepository
import ru.edustor.core.util.extensions.assertHasAccess

@RestController
@RequestMapping("/api/subjects")
class SubjectsController @Autowired constructor(val subjectsRepository: SubjectsRepository) {

    @RequestMapping("/list")
    fun listSubjects(@AuthenticationPrincipal user: User): List<Subject> {
        val result = subjectsRepository.findByOwner(user).filter { !it.removed }
        return result.sorted()
    }

    @RequestMapping("/create")
    fun createSubject(@AuthenticationPrincipal user: User, @RequestParam name: String): Subject {

        val subject = Subject(name, user)
        subjectsRepository.save(subject)

        return subject
    }

    @RequestMapping("/{subject}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: User, @PathVariable subject: Subject) {
        user.assertHasAccess(subject)
        subject.removed = true
        subjectsRepository.save(subject)
    }

    @RequestMapping("/{subject}/restore")
    fun restore(@AuthenticationPrincipal user: User, @PathVariable subject: Subject) {
        user.assertHasAccess(subject)
        subject.removed = false
        subjectsRepository.save(subject)
    }
}
