package ru.wutiarn.edustor.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import ru.wutiarn.edustor.repository.SessionRepository
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
open class SecurityFilter @Autowired constructor(val repo: SessionRepository) : GenericFilterBean() {

    val regex = "^/+api/(?!account/login([/]|$)).*".toRegex()

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        if (req is HttpServletRequest) {
            val urlSecured = checkUrlSecured(req.requestURI)
            val httpResp = res as HttpServletResponse

            val token = req.getHeader("token")

            if (token == null && urlSecured) {
                httpResp.sendError(HttpStatus.UNAUTHORIZED.value(), "Token was not provided")
                return
            }

            token?.let {
                val session = repo.findByToken(token)

                if (session == null && urlSecured) {
                    httpResp.sendError(HttpStatus.FORBIDDEN.value(), "Session was not found")
                    return
                }

                session?.let {
                    session.user.currentSession = session
                    val auth = UsernamePasswordAuthenticationToken(session.user, null, userAuthorities)
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        chain.doFilter(req, res)
    }

    fun checkUrlSecured(url: String): Boolean {
        val matches = regex.matches(url)
        return matches
    }

    val userAuthorities: MutableCollection<out GrantedAuthority>
        get() {
            val grantedAuthorities = ArrayList<GrantedAuthority>()
            grantedAuthorities.add(SimpleGrantedAuthority("ROLE_USER"))
            return Collections.unmodifiableList(grantedAuthorities)
        }
}
