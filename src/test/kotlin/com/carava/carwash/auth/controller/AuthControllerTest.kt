package com.carava.carwash.auth.controller

import com.carava.carwash.auth.dto.SignInRequestDto
import com.carava.carwash.auth.dto.SignInResponseDto
import com.carava.carwash.auth.dto.SignUpRequestDto
import com.carava.carwash.auth.dto.SignUpResponseDto
import com.carava.carwash.auth.service.AuthService
import com.carava.carwash.global.dto.ApiResponse
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

    // ===== 회원가입 성공 케이스 =====
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

    // ===== 회원가입 실패 케이스들 =====
    @Test
    fun `given_invalid_email_when_signup_then_return_400`() {
        // given
        val request = SignUpRequestDto(
            email = "invalid-email",
            password = "Password123!",
            name = "홍길동",
            phone = "010-1234-5678",
            birthDate = LocalDate.of(1990, 1, 1),
            nickname = "길동이"
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
    fun `given_existing_email_when_signup_then_return_409`() {
        // given
        val request = SignUpRequestDto(
            email = "existing@example.com",
            password = "Password123!",
            name = "홍길동",
            phone = "010-1234-5678",
            birthDate = LocalDate.of(1990, 1, 1),
            nickname = "길동이"
        )

        every { authService.signUp(request) } returns ApiResponse.error(
            "EMAIL_ALREADY_EXISTS",
            "이미 사용 중인 이메일입니다"
        )

        // when & then
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"))
            .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다"))
    }

    @Test
    fun `given_weak_password_when_signup_then_return_400`() {
        // given
        val request = SignUpRequestDto(
            email = "test@example.com",
            password = "123", // 약한 비밀번호
            name = "홍길동",
            phone = "010-1234-5678",
            birthDate = LocalDate.of(1990, 1, 1),
            nickname = "길동이"
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
    fun `given_empty_required_fields_when_signup_then_return_400`() {
        // given
        val request = SignUpRequestDto(
            email = "",
            password = "",
            name = "",
            phone = "",
            birthDate = LocalDate.of(1990, 1, 1),
            nickname = ""
        )

        // when & then
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // ===== 로그인 성공 케이스 =====
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

    // ===== 로그인 실패 케이스들 =====
    @Test
    fun `given_invalid_credentials_when_signin_then_return_401`() {
        // given
        val request = SignInRequestDto(
            email = "test@example.com",
            password = "wrongpassword"
        )

        every { authService.signIn(request) } returns ApiResponse.error(
            "INVALID_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다"
        )

        // when & then
        mockMvc.perform(
            post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다"))
    }

    @Test
    fun `given_nonexistent_user_when_signin_then_return_401`() {
        // given
        val request = SignInRequestDto(
            email = "nonexistent@example.com",
            password = "Password123!"
        )

        every { authService.signIn(request) } returns ApiResponse.error(
            "USER_NOT_FOUND",
            "등록되지 않은 이메일입니다"
        )

        // when & then
        mockMvc.perform(
            post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("등록되지 않은 이메일입니다"))
    }

    @Test
    fun `given_empty_credentials_when_signin_then_return_400`() {
        // given
        val request = SignInRequestDto(
            email = "",
            password = ""
        )

        // when & then
        mockMvc.perform(
            post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    // ===== 이메일 확인 성공 케이스 =====
    @Test
    fun `given_available_email_when_check_email_then_return_true`() {
        // given
        val email = "available@example.com"
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
    fun `given_existing_email_when_check_email_then_return_false`() {
        // given
        val email = "existing@example.com"
        every { authService.checkEmail(email) } returns ApiResponse.success(false, "이미 사용 중인 이메일입니다")

        // when & then
        mockMvc.perform(
            get("/auth/check-email")
                .param("email", email)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(false))
            .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다"))
    }

    // ===== 이메일 확인 실패 케이스들 =====
    @Test
    fun `given_invalid_email_format_when_check_email_then_return_400`() {
        // given
        val invalidEmail = "invalid-email-format"

        every { authService.checkEmail(invalidEmail) } returns ApiResponse.error(
            "INVALID_EMAIL_FORMAT",
            "올바른 이메일 형식이 아닙니다"
        )

        // when & then
        mockMvc.perform(
            get("/auth/check-email")
                .param("email", invalidEmail)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("INVALID_EMAIL_FORMAT"))
            .andExpect(jsonPath("$.message").value("올바른 이메일 형식이 아닙니다"))
    }

    @Test
    fun `given_empty_email_when_check_email_then_return_400`() {
        // when & then
        mockMvc.perform(
            get("/auth/check-email")
                .param("email", "")
        )
            .andExpect(status().isBadRequest)
    }

    // ===== 토큰 갱신 성공 케이스 =====
    @Test
    fun `given_valid_refresh_token_when_refresh_then_return_new_access_token`() {
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

    // ===== 토큰 갱신 실패 케이스들 =====
    @Test
    fun `given_expired_refresh_token_when_refresh_then_return_401`() {
        // given
        val expiredToken = "expired_refresh_token"
        
        every { authService.refreshToken(expiredToken) } returns ApiResponse.error(
            "EXPIRED_REFRESH_TOKEN",
            "만료된 리프레시 토큰입니다"
        )

        // when & then
        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "$expiredToken"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("EXPIRED_REFRESH_TOKEN"))
            .andExpect(jsonPath("$.message").value("만료된 리프레시 토큰입니다"))
    }

    @Test
    fun `given_invalid_refresh_token_when_refresh_then_return_401`() {
        // given
        val invalidToken = "invalid_refresh_token"
        
        every { authService.refreshToken(invalidToken) } returns ApiResponse.error(
            "INVALID_REFRESH_TOKEN",
            "유효하지 않은 리프레시 토큰입니다"
        )

        // when & then
        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": "$invalidToken"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"))
            .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다"))
    }

    @Test
    fun `given_empty_refresh_token_when_refresh_then_return_400`() {
        // when & then
        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken": ""}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `given_malformed_json_when_refresh_then_return_400`() {
        // when & then
        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"invalidJson":}""")
        )
            .andExpect(status().isBadRequest)
    }
} 