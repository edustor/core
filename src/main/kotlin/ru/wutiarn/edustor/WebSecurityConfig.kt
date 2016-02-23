package ru.wutiarn.edustor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import ru.wutiarn.edustor.service.UserDetailsService

/**
 * Created by wutiarn on 23.02.16.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder, uds: UserDetailsService) {
        auth.userDetailsService(uds)
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .anyRequest().permitAll()
        http.formLogin()
        http.csrf().disable()
    }
}