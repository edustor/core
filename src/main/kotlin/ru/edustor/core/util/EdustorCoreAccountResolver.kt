package ru.edustor.core.util

import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import ru.edustor.commons.auth.EdustorTokenValidator
import ru.edustor.commons.auth.internal.EdustorAccountResolver
import ru.edustor.commons.protobuf.proto.internal.EdustorAccountsProtos.EdustorAccount
import ru.edustor.core.model.Account
import ru.edustor.core.repository.AccountRepository

open class EdustorCoreAccountResolver(val repo: AccountRepository,
                                      validator: EdustorTokenValidator) : EdustorAccountResolver(validator) {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == Account::class.java
    }

    override fun resolveArgument(parameter: MethodParameter?, mavContainer: ModelAndViewContainer?, webRequest: NativeWebRequest, binderFactory: WebDataBinderFactory?): Any? {
        val protoAccount = super.resolveArgument(parameter, mavContainer, webRequest, binderFactory) as EdustorAccount
        val account = repo.findOne(protoAccount.uuid)

        return account ?: Account(protoAccount.uuid)
    }
}