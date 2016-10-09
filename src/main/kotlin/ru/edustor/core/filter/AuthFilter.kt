package ru.edustor.core.filter

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import ru.edustor.core.repository.mongo.MongoUserRepository
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
open class AuthFilter @Autowired constructor(val repo: MongoUserRepository) : GenericFilterBean() {

    val regex = "^/+api/(?!account/login([/]|$)).*".toRegex()
    val publicKey: PublicKey

    init {
        val keyBytes = javaClass.classLoader.getResourceAsStream("jwk.pub.der").readBytes()
        val spec = X509EncodedKeySpec(keyBytes)
        publicKey = KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        if (req is HttpServletRequest) {
            val urlSecured = checkUrlSecured(req.requestURI)
            val httpResp = res as HttpServletResponse

            val token = req.getHeader("Authorization")

            if (token == null) {
                if (urlSecured) {
                    httpResp.sendError(HttpStatus.UNAUTHORIZED.value(), "Token is not provided")
                } else {
                    chain.doFilter(req, res)
                }
                return
            }

            val claims: Claims
            try {
                claims = Jwts.parser()
                        .setSigningKey(publicKey)
                        .parseClaimsJws(token)
                        .body
            } catch (e: JwtException) {
                httpResp.sendError(HttpStatus.UNAUTHORIZED.value(), e.message ?: "Failed to validate token")
                return
            }

            val user = repo.findOne(claims.subject)
            val auth = UsernamePasswordAuthenticationToken(user, null, userAuthorities)
            SecurityContextHolder.getContext().authentication = auth
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
