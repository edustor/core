package ru.edustor.core.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import ru.edustor.commons.auth.EdustorTokenValidator
import ru.edustor.core.repository.AccountRepository
import ru.edustor.core.util.EdustorCoreAccountResolver

@Configuration
open class ArgumentResolverConfig(val repo: AccountRepository,
                                  val validator: EdustorTokenValidator) : WebMvcConfigurerAdapter() {
    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        val resolver = EdustorCoreAccountResolver(repo, validator)
        argumentResolvers.add(resolver)
    }
}