package ru.wutiarn.edustor.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.repository.UserRepository

/**
 * Created by wutiarn on 23.02.16.
 */
@Component
class UserDetailsService @Autowired constructor(val repo: UserRepository) : org.springframework.security.core.userdetails.UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails? {
        val user = repo.findByLogin(username) ?: throw UsernameNotFoundException(String.format("%s user is not found", username))
        return user
    }
}