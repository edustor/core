package ru.edustor.core.util

import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import ru.edustor.commons.auth.EdustorTokenValidator
import ru.edustor.commons.auth.internal.EdustorAuthProfileResolver
import ru.edustor.commons.auth.model.EdustorAuthProfile
import ru.edustor.core.model.Account
import ru.edustor.core.repository.AccountRepository

open class EdustorCoreAccountResolver(val repo: AccountRepository,
                                      validator: EdustorTokenValidator) : EdustorAuthProfileResolver(validator) {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Account::class.java
    }

    override fun resolveArgument(parameter: MethodParameter?, mavContainer: ModelAndViewContainer?, webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?): Any? {
        val authProfile = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory) as EdustorAuthProfile
        val account = repo.findOne(authProfile.accountId)

        return account ?: let {
            val account = Account(authProfile.accountId)
            repo.save(account)
            return@let account
        }
    }
}