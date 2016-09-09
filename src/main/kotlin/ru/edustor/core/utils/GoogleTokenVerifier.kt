package ru.edustor.core.utils

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import org.springframework.stereotype.Component

@Component
open class GoogleTokenVerifier() {
    private val BACKEND_CLIENT_ID = "99685742253-41uieqd0vl3e03l62c7t3impd38gdt4q.apps.googleusercontent.com"

    private val mobileVerifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
            .setAudience(listOf(BACKEND_CLIENT_ID))
            .setIssuer("https://accounts.google.com")
            .build()

    private val webVerifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
            .setAudience(listOf(BACKEND_CLIENT_ID))
            .setIssuer("https://accounts.google.com")
            .build()

    data class GoogleAccount(val email: String,
                             val name: String,
                             val picture: String,
                             val locale: String)


    open fun verify(token: String): GoogleAccount {
        val googleId: GoogleIdToken
        try {
            googleId = mobileVerifier.verify(token) ?:
                    webVerifier.verify(token) ?: throw IllegalArgumentException("Bad token")
        } catch (e: Throwable) {
            throw throw IllegalArgumentException("Token parse error")
        }

        return GoogleAccount(googleId.payload.email,
                googleId.payload["name"] as String,
                googleId.payload["picture"] as String,
                googleId.payload["locale"] as String)
    }
}