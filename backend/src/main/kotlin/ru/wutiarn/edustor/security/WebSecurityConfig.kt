package ru.wutiarn.edustor.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import ru.wutiarn.edustor.api.SecurityFilter
import ru.wutiarn.edustor.repository.UserRepository

/**
 * Created by wutiarn on 23.02.16.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    val usersRepo: UserRepository? = null

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .antMatchers("/api/login/**").permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        http.csrf().disable()
        http.addFilterBefore(SecurityFilter(usersRepo!!), UsernamePasswordAuthenticationFilter::class.java)
    }
}