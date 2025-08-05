package com.carava.carwash.auth.service

import com.carava.carwash.auth.dto.SignInRequestDto
import com.carava.carwash.auth.dto.SignUpRequestDto
import com.carava.carwash.auth.entity.Auth
import com.carava.carwash.auth.entity.UserType
import com.carava.carwash.auth.repository.AuthRepository
import com.carava.carwash.global.config.security.JwtUtil
import com.carava.carwash.member.entity.CustomerMember
import com.carava.carwash.member.repository.CustomerMemberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthServiceTest {

    private val authRepository = mockk<AuthRepository>()
    private val customerMemberRepository = mockk<CustomerMemberRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtUtil = mockk<JwtUtil>()
    
    private val authService = AuthService(
        authRepository = authRepository,
        customerMemberRepository = customerMemberRepository,
        passwordEncoder = passwordEncoder,
        jwtUtil = jwtUtil
    )

    @Test
    fun `given_valid_signup_request_when_signup_then_return_success_response`() {
        // given
        val request = SignUpRequestDto(
            email = "test@example.com",
            password = "Password123!",
            name = "홍길동",
            phone = "010-1234-5678",
            birthDate = LocalDate.of(1990, 1, 1),
            nickname = "길동이"
        )
        
        val encodedPassword = "encoded_password"
        val savedAuth = Auth(
            id = 1L,
            email = request.email,
            password = encodedPassword,
            userType = UserType.CUSTOMER
        )
        val savedCustomer = CustomerMember(
            id = 1L,
            authId = 1L,
            name = request.name,
            phone = request.phone,
            birthDate = request.birthDate,
            nickname = request.nickname
        )

        every { authRepository.existsByEmail(request.email) } returns false
        every { customerMemberRepository.existsByPhone(request.phone) } returns false
        every { passwordEncoder.encode(request.password) } returns encodedPassword
        every { authRepository.save(any()) } returns savedAuth
        every { customerMemberRepository.save(any()) } returns savedCustomer

        // when
        val response = authService.signUp(request)

        // then
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(request.email, response.data?.email)
        assertEquals(request.name, response.data?.name)
        
        verify { authRepository.save(any()) }
        verify { customerMemberRepository.save(any()) }
    }

    @Test
    fun `given_existing_email_when_signup_then_throw_exception`() {
        // given
        val request = SignUpRequestDto(
            email = "existing@example.com",
            password = "Password123!",
            name = "홍길동",
            phone = "010-1234-5678"
        )

        every { authRepository.existsByEmail(request.email) } returns true

        // when & then
        assertThrows<IllegalArgumentException> {
            authService.signUp(request)
        }
    }

    @Test
    fun `given_valid_signin_request_when_signin_then_return_tokens`() {
        // given
        val request = SignInRequestDto(
            email = "test@example.com",
            password = "Password123!"
        )
        
        val auth = Auth(
            id = 1L,
            email = request.email,
            password = "encoded_password",
            userType = UserType.CUSTOMER
        )
        val customer = CustomerMember(
            id = 1L,
            authId = 1L,
            name = "홍길동",
            phone = "010-1234-5678"
        )
        
        val accessToken = "access_token"
        val refreshToken = "refresh_token"

        every { authRepository.findByEmail(request.email) } returns auth
        every { auth.isActive() } returns true
        every { passwordEncoder.matches(request.password, auth.password) } returns true
        every { customerMemberRepository.findByAuthId(auth.id) } returns customer
        every { jwtUtil.generateAccessToken(auth.email, auth.userType.name) } returns accessToken
        every { jwtUtil.generateRefreshToken(auth.email, auth.userType.name) } returns refreshToken
        every { authRepository.save(any()) } returns auth

        // when
        val response = authService.signIn(request)

        // then
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(accessToken, response.data?.accessToken)
        assertEquals(refreshToken, response.data?.refreshToken)
    }

    @Test
    fun `given_wrong_password_when_signin_then_throw_exception`() {
        // given
        val request = SignInRequestDto(
            email = "test@example.com",
            password = "wrong_password"
        )
        
        val auth = Auth(
            id = 1L,
            email = request.email,
            password = "encoded_password",
            userType = UserType.CUSTOMER
        )

        every { authRepository.findByEmail(request.email) } returns auth
        every { auth.isActive() } returns true
        every { passwordEncoder.matches(request.password, auth.password) } returns false

        // when & then
        assertThrows<IllegalArgumentException> {
            authService.signIn(request)
        }
    }
} 