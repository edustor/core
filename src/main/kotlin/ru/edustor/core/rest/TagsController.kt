package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Tag
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.repository.TagRepository

@RestController
@RequestMapping("/api/tags")
class TagsController @Autowired constructor(val accountRepository: AccountRepository,
                                            val tagRepository: TagRepository) {

    @RequestMapping("/list")
    fun listTags(user: Account): List<Tag> {
        val result = user.tags.filter { !it.removed }
        return result.sorted()
    }

    @RequestMapping("/create")
    fun createTag(account: Account, @RequestParam name: String): Tag.TagDTO {
//        TODO: Add parent tag param
        val tag = Tag(name = name, owner = account)
        tagRepository.save(tag)

        return tag.toDTO()
    }

    @RequestMapping("/{tag}/lessons")
    fun tagLessons(@PathVariable tag: Tag?): List<Lesson> {
        tag ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return tag.lessons.filter { it.pages.isNotEmpty() && !it.removed }.sortedDescending()
    }

    @RequestMapping("/{tag}", method = arrayOf(RequestMethod.DELETE))
    fun delete(user: Account, @PathVariable("tag") tagId: String) {
        val tag = user.tags.firstOrNull { it.id == tagId } ?: throw NotFoundException()
        tag.removed = true
        accountRepository.save(user)
    }

    @RequestMapping("/{tag}/restore")
    fun restore(user: Account, @PathVariable("tag") tagId: String) {
        val tag = user.tags.firstOrNull { it.id == tagId } ?: throw NotFoundException()
        tag.removed = false
        accountRepository.save(user)
    }
}
