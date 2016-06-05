package ru.wutiarn.edustor.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.repository.SessionRepository
import java.util.*
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

@Component
open class SecurityFilter @Autowired constructor(val repo: SessionRepository) : Filter {

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        if (req is HttpServletRequest /*&& req.requestURI.contains(Regex("^/api/"))*/) {
            val token = req.getHeader("token")
            if (token != null) {
                val session = repo.findByToken(token)
                session?.let {
                    val auth = UsernamePasswordAuthenticationToken(session.user, null, userAuthorities)
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