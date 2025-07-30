package com.carava.carwash.auth.controller

import com.carava.carwash.auth.dto.*
import com.carava.carwash.auth.service.AuthService
import com.carava.carwash.shared.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "인증", description = "고객 인증 관리 API")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = "고객 회원가입을 처리합니다"
    )
    @SwaggerApiResponse(responseCode = "201", description = "회원가입 성공")
    @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청")
    @SwaggerApiResponse(responseCode = "409", description = "이메일 또는 전화번호 중복")
    fun signUp(
        @Valid @RequestBody request: SignUpRequestDto
    ): ResponseEntity<ApiResponse<SignUpResponseDto>> {
        val response = authService.signUp(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/signin")
    @Operation(
        summary = "로그인",
        description = "고객 로그인을 처리하고 JWT 토큰을 발급합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "로그인 성공")
    @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    fun signIn(
        @Valid @RequestBody request: SignInRequestDto
    ): ResponseEntity<ApiResponse<SignInResponseDto>> {
        val response = authService.signIn(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/check-email")
    @Operation(
        summary = "이메일 중복 확인",
        description = "이메일 사용 가능 여부를 확인합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "확인 완료")
    fun checkEmail(
        @RequestParam email: String
    ): ResponseEntity<ApiResponse<Boolean>> {
        val response = authService.checkEmail(email)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "토큰 갱신",
        description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    @SwaggerApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    fun refreshToken(
        @RequestBody request: RefreshTokenRequestDto
    ): ResponseEntity<ApiResponse<String>> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    data class RefreshTokenRequestDto(
        val refreshToken: String
    )
}