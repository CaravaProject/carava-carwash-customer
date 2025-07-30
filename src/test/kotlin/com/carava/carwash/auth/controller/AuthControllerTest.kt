package com.carava.carwash.auth.controller

import com.carava.carwash.auth.dto.SignInRequestDto
import com.carava.carwash.auth.dto.SignInResponseDto
import com.carava.carwash.auth.dto.SignUpRequestDto
import com.carava.carwash.auth.dto.SignUpResponseDto
import com.carava.carwash.auth.service.AuthService
import com.carava.carwash.shared.dto.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var authService: AuthService

    @Test
    fun `given_valid_signup_request_when_signup_then_return_201`() {
        // given
        val request = SignUpRequestDto(
            email = "test@example.com",
            password = "Password123!",
            name = "홍길동",
            phone = "010-1234-5678",
            birthDate = LocalDate.of(1990, 1, 1),
            nickname = "길동이"
        )
        
        val response = SignUpResponseDto(
            authId = 1L,
            customerId = 1L,
            email = "test@example.com",
            name = "홍길동",
            phone = "010-1234-5678",
            nickname = "길동이"
        )

        every { authService.signUp(request) } returns ApiResponse.success(response, "회원가입이 완료되었습니다")

        // when & then
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.name").value("홍길동"))
            .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"))
    }

    @Test
    fun `given_invalid_email_when_signup_then_return_400`() {
        // given
        val request = SignUpRequestDto(
            email = "invalid-email",
            password = "Password123!",
            name = "홍길동",
            phone = "010-1234-5678"
        )

        // when & then
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `given_valid_signin_request_when_signin_then_return_200`() {
        // given
        val request = SignInRequestDto(
            email = "test@example.com",
            password = "Password123!"
        )
        
        val response = SignInResponseDto(
            authId = 1L,
            customerId = 1L,
            email = "test@example.com",
            name = "홍길동",
            accessToken = "access_token",
            refreshToken = "refresh_token"
        )

        every { authService.signIn(request) } returns ApiResponse.success(response, "로그인이 완료되었습니다")

        // when & then
        mockMvc.perform(
            post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access_token"))
            .andExpect(jsonPath("$.data.refreshToken").value("refresh_token"))
            .andExpect(jsonPath("$.message").value("로그인이 완료되었습니다"))
    }

    @Test
    fun `given_email_when_check_email_then_return_availability`() {
        // given
        val email = "test@example.com"
        every { authService.checkEmail(email) } returns ApiResponse.success(true, "사용 가능한 이메일입니다")

        // when & then
        mockMvc.perform(
            get("/auth/check-email")
                .param("email", email)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(true))
            .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다"))
    }

    @Test
    fun `given_refresh_token_when_refresh_then_return_new_access_token`() {
        // given
        val refreshToken = "valid_refresh_token"
        val newAccessToken = "new_access_token"
        
        every { authService.refreshToken(refreshToken) } returns ApiResponse.success(newAccessToken, "토큰이 갱신되었습니다")

        // when & then
        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "$refreshToken"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(newAccessToken))
            .andExpect(jsonPath("$.message").value("토큰이 갱신되었습니다"))
    }
} 