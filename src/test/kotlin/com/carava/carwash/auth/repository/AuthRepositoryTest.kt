package com.carava.carwash.auth.repository

import com.carava.carwash.auth.entity.Auth
import com.carava.carwash.auth.entity.AuthStatus
import com.carava.carwash.auth.entity.UserType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
class AuthRepositoryTest {

    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Autowired
    private lateinit var authRepository: AuthRepository

    @Test
    fun `given_auth_when_save_then_return_saved_auth`() {
        // given
        val auth = Auth(
            email = "test@example.com",
            password = "encodedPassword",
            userType = UserType.CUSTOMER
        )

        // when
        val savedAuth = authRepository.save(auth)

        // then
        assertNotNull(savedAuth.id)
        assertEquals("test@example.com", savedAuth.email)
        assertEquals(UserType.CUSTOMER, savedAuth.userType)
        assertEquals(AuthStatus.ACTIVE, savedAuth.status)
    }

    @Test
    fun `given_existing_email_when_find_by_email_then_return_auth`() {
        // given
        val auth = Auth(
            email = "test@example.com",
            password = "encodedPassword",
            userType = UserType.CUSTOMER
        )
        testEntityManager.persistAndFlush(auth)

        // when
        val foundAuth = authRepository.findByEmail("test@example.com")

        // then
        assertNotNull(foundAuth)
        assertEquals("test@example.com", foundAuth?.email)
    }

    @Test
    fun `given_non_existing_email_when_find_by_email_then_return_null`() {
        // when
        val foundAuth = authRepository.findByEmail("nonexistent@example.com")

        // then
        assertNull(foundAuth)
    }

    @Test
    fun `given_existing_email_when_exists_by_email_then_return_true`() {
        // given
        val auth = Auth(
            email = "test@example.com",
            password = "encodedPassword",
            userType = UserType.CUSTOMER
        )
        testEntityManager.persistAndFlush(auth)

        // when
        val exists = authRepository.existsByEmail("test@example.com")

        // then
        assertTrue(exists)
    }

    @Test
    fun `given_refresh_token_when_find_by_refresh_token_then_return_auth`() {
        // given
        val refreshToken = "valid_refresh_token"
        val auth = Auth(
            email = "test@example.com",
            password = "encodedPassword",
            userType = UserType.CUSTOMER,
            refreshToken = refreshToken
        )
        testEntityManager.persistAndFlush(auth)

        // when
        val foundAuth = authRepository.findByRefreshToken(refreshToken)

        // then
        assertNotNull(foundAuth)
        assertEquals(refreshToken, foundAuth?.refreshToken)
    }
} 