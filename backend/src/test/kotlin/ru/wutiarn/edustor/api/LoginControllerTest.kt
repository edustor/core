package ru.wutiarn.edustor.api

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.SessionRepository
import ru.wutiarn.edustor.repository.UserRepository
import ru.wutiarn.edustor.utils.GoogleTokenVerifier

class LoginControllerTest {

    lateinit var userRepo: UserRepository;
    lateinit var sessionRepository: SessionRepository;
    lateinit var googleTokenVerifier: GoogleTokenVerifier;
    lateinit var loginController: LoginController;

    @Before
    fun init() {
        userRepo = Mockito.mock(UserRepository::class.java)
        sessionRepository = Mockito.mock(SessionRepository::class.java)
        googleTokenVerifier = Mockito.mock(GoogleTokenVerifier::class.java)

        loginController = LoginController(userRepo, sessionRepository, googleTokenVerifier)
    }

    @Test
    fun checkTokenNotLoggedIn() {
        try {
            loginController.checkToken(null)
        } catch(e: HttpRequestProcessingException) {
            assertEquals(e.status, HttpStatus.FORBIDDEN)
        }
    }

    @Test
    fun checkTokenLoggedIn() {
        val user = User("test@example.com")
        val resp = loginController.checkToken(user)
        assertEquals(resp, "You're logged in as test@example.com")
    }

}