package ru.wutiarn.edustor.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import ru.wutiarn.edustor.repository.UserRepository
import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

/**
 * Created by wutiarn on 23.02.16.
 */
class SecurityFilter @Autowired constructor(val repo: UserRepository) : Filter {

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        if (req is HttpServletRequest /*&& req.requestURI.contains(Regex("^/api/"))*/) {
            val token = req.getHeader("token")
            if (token != null) {
                val user = repo.findBySession(token)
                if (user != null) {

                    val auth = UsernamePasswordAuthenticationToken(user, null, userAuthorities)
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        chain.doFilter(req, res)
    }

    override fun destroy() {
    }

    override fun init(p0: FilterConfig?) {
    }

    val userAuthorities: MutableCollection<out GrantedAuthority>
        get() {
            val grantedAuthorities = ArrayList<GrantedAuthority>()
            grantedAuthorities.add(SimpleGrantedAuthority("ROLE_USER"))
            return Collections.unmodifiableList(grantedAuthorities)
        }
}