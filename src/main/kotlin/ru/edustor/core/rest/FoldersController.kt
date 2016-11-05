package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import ru.edustor.core.repository.FoldersRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.util.extensions.assertHasAccess

@RestController
@RequestMapping("/api/subjects")
class FoldersController @Autowired constructor(val foldersRepository: FoldersRepository, val lessonsRepo: LessonsRepository) {

    @RequestMapping("/list")
    fun listRootFolders(@AuthenticationPrincipal user: Account): List<Folder> {
        val result = foldersRepository.findByOwner(user).filter { !it.removed && it.parent == null }
        return result.sorted()
    }

    @RequestMapping("/create")
    fun createFolder(@AuthenticationPrincipal user: Account,
                     @RequestParam name: String,
                     @RequestParam(required = false) parent: Folder? = null
    ): Folder {
        val folder = Folder(name, user, parent)
        foldersRepository.save(folder)

        return folder
    }

    @RequestMapping("/{folder}/lessons")
    fun subjectLessons(@PathVariable folder: Folder?): List<Lesson> {
        folder ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lessonsRepo.findByFolder(folder).filter { it.documents.isNotEmpty() && !it.removed }.sortedDescending()
    }

    @RequestMapping("/{folder}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: Account, @PathVariable folder: Folder) {
        user.assertHasAccess(folder)
        folder.removed = true
        foldersRepository.save(folder)
    }

    @RequestMapping("/{folder}/restore")
    fun restore(@AuthenticationPrincipal user: Account, @PathVariable folder: Folder) {
        user.assertHasAccess(folder)
        folder.removed = false
        foldersRepository.save(folder)
    }
}
