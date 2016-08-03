package ru.wutiarn.edustor.interceptor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.services.FCMService
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
open class FCMInterceptor : HandlerInterceptorAdapter() {
    @Autowired private lateinit var fcmService: FCMService

    val regex = "^/+api/(?!((sync)|(account/login))([/]|$)).*".toRegex()

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        val user = SecurityContextHolder.getContext().authentication?.principal as User?
        if (request.method in arrayOf(
                HttpMethod.POST.name,
                HttpMethod.PUT.name,
                HttpMethod.PATCH.name,
                HttpMethod.DELETE.name
        ) && request.requestURI.matches(regex)) {
            user?.let { fcmService.sendUserSyncNotification(it) }
        }
    }
}