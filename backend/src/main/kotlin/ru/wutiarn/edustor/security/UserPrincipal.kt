package ru.wutiarn.edustor.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.wutiarn.edustor.models.User
import java.util.*

/**
 * Created by wutiarn on 29.02.16.
 */
class UserPrincipal(val user: User) : User(
        login = user.login,
        mPassword = user.mPassword,
        groups = user.groups,
        sessions = user.sessions,
        id = user.id,
        timetable = user.timetable
), UserDetails {
    override fun getUsername(): String? {
        return user.login
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
        return user.mPassword
    }
}