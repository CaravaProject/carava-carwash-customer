package com.carava.carwash.auth.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthTest {
    
    @Test
    fun `given_valid_auth_data_when_create_auth_then_success`() {
        // given
        val email = "test@example.com"
        val password = "encodedPassword123"
        val userType = UserType.CUSTOMER
        
        // when
        val auth = Auth(
            email = email,
            password = password,
            userType = userType
        )
        
        // then
        assertEquals(email, auth.email)
        assertEquals(password, auth.password)
        assertEquals(userType, auth.userType)
        assertEquals(AuthStatus.ACTIVE, auth.status) // 기본값
        assertFalse(auth.emailVerified) // 기본값
        assertNotNull(auth.createdAt)
        assertNotNull(auth.updatedAt)
    }
    
    @Test
    fun `given_auth_when_verify_email_then_email_verified_true`() {
        // given
        val auth = Auth(
            email = "test@example.com",
            password = "password",
            userType = UserType.CUSTOMER
        )
        
        // when
        auth.verifyEmail()
        
        // then
        assertTrue(auth.emailVerified)
    }
    
    @Test
    fun `given_auth_when_update_refresh_token_then_token_updated`() {
        // given
        val auth = Auth(
            email = "test@example.com",
            password = "password",
            userType = UserType.CUSTOMER
        )
        val refreshToken = "new_refresh_token"
        
        // when
        auth.updateRefreshToken(refreshToken)
        
        // then
        assertEquals(refreshToken, auth.refreshToken)
    }
    
    @Test
    fun `given_auth_when_suspend_then_status_suspended`() {
        // given
        val auth = Auth(
            email = "test@example.com",
            password = "password",
            userType = UserType.CUSTOMER
        )
        
        // when
        auth.suspend()
        
        // then
        assertEquals(AuthStatus.SUSPENDED, auth.status)
    }
    
    @Test
    fun `given_auth_when_activate_then_status_active`() {
        // given
        val auth = Auth(
            email = "test@example.com",
            password = "password",
            userType = UserType.CUSTOMER,
            status = AuthStatus.INACTIVE
        )
        
        // when
        auth.activate()
        
        // then
        assertEquals(AuthStatus.ACTIVE, auth.status)
    }
    
    @Test
    fun `given_auth_when_update_last_login_then_last_login_at_updated`() {
        // given
        val auth = Auth(
            email = "test@example.com",
            password = "password",
            userType = UserType.CUSTOMER
        )
        
        // when
        auth.updateLastLogin()
        
        // then
        assertNotNull(auth.lastLoginAt)
    }
} 