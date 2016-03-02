package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Group
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.GroupsRepository
import ru.wutiarn.edustor.repository.UserRepository

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/groups")
class GroupsController @Autowired constructor(val repo: GroupsRepository,
                                              val userRepo: UserRepository) {

    @RequestMapping("/list")
    fun listGroups(@AuthenticationPrincipal user: User): List<Group> {
        return user.groups
    }

    @RequestMapping("/create")
    fun createGroup(@AuthenticationPrincipal user: User, @RequestParam name: String): Group {
        val group = Group(name)
        group.owners.add(user)
        user.groups.add(group)
        repo.save(group)
        userRepo.save(user)
        return group
    }

    @RequestMapping("/destroy")
    fun destroyGroup(@AuthenticationPrincipal user: User, @RequestParam id: String) {
        val group = repo.findOne(id)
        if (user !in group.owners) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You're not owner of this group")
        repo.destroyGroup(group)
    }
}
