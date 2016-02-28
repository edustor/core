package ru.wutiarn.edustor.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

/**
 * Created by wutiarn on 22.02.16.
 */
data class User(
        var login: String? = null,
        private var password: String? = null,
        var sessions: MutableList<Session> = mutableListOf(),
        @DBRef(lazy = true) var groups: MutableList<Group> = mutableListOf(),
        var timetable: MutableList<TimetableEntry> = mutableListOf(),
        @Id var id: String? = null
) : UserDetails {
    override fun getUsername(): String? {
        return login
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority>? {
        val grantedAuthorities = ArrayList<GrantedAuthority>()
        grantedAuthorities.add(SimpleGrantedAuthority("ROLE_USER"))
        return Collections.unmodifiableList(grantedAuthorities)
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun getPassword(): String? {
        return password
    }
}

