package ru.edustor.core.rest

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.User
import ru.edustor.core.repository.SessionRepository
import ru.edustor.core.repository.UserRepository
import ru.edustor.core.rest.account.LoginController
import ru.edustor.core.util.GoogleTokenVerifier

class LoginControllerTest {

    lateinit var userRepo: UserRepository
    lateinit var sessionRepository: SessionRepository
    lateinit var googleTokenVerifier: GoogleTokenVerifier
    lateinit var loginController: LoginController

    val GOOGLE_TOKEN = "Fake token"
    val GOOGLE_BAD_TOKEN = "Bad fake token"
    val GOOGLE_ACCOUNT = GoogleTokenVerifier.GoogleAccount("test@example.com",
            "Ivan Petrov",
            "http://example.com/photo.png",
            "ru")
    @Before
    fun init() {
        userRepo = Mockito.mock(UserRepository::class.java)
        sessionRepository = Mockito.mock(SessionRepository::class.java)
        googleTokenVerifier = Mockito.mock(GoogleTokenVerifier::class.java)

        loginController = LoginController(userRepo, sessionRepository, googleTokenVerifier)
    }

    fun prepareLogin() {
        Mockito.`when`(googleTokenVerifier.verify(GOOGLE_TOKEN)).thenReturn(GOOGLE_ACCOUNT)
        Mockito.`when`(googleTokenVerifier.verify(GOOGLE_BAD_TOKEN)).thenThrow(IllegalArgumentException())
    }

    @Test
    fun checkLogin() {
        prepareLogin()

        val user = User(GOOGLE_ACCOUNT.email)
        user.id = "Fake ID"
        Mockito.`when`(userRepo.findByEmail(GOOGLE_ACCOUNT.email)).thenReturn(user)

        val result = loginController.login(GOOGLE_TOKEN)
        assertSame(result.user, user)
        Mockito.verify(sessionRepository).save(result)
    }

    @Test(expected = HttpRequestProcessingException::class)
    fun checkLoginFailed() {
        prepareLogin()
        loginController.login(GOOGLE_BAD_TOKEN)
    }

    @Test
    @Ignore // TODO: Remove on public release
    fun checkLoginNew() {
        prepareLogin()
        Mockito.`when`(userRepo.findByEmail(GOOGLE_ACCOUNT.email)).thenReturn(null)
        val result = loginController.login(GOOGLE_TOKEN)

        assertNotNull(result.user)

        Mockito.verify(userRepo).save(result.user)

    }
}