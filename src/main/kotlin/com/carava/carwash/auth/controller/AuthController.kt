package com.carava.carwash.auth.controller

import com.carava.carwash.auth.dto.*
import com.carava.carwash.auth.service.AuthService
import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.global.dto.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
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
    @SwaggerApiResponse(
        responseCode = "201", 
        description = "회원가입 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(
                value = """
                {
                    "success": true,
                    "data": {
                        "authId": 1,
                        "email": "user@example.com",
                        "name": "홍길동",
                        "nickname": "길동이",
                        "createdAt": "2024-01-01T10:00:00"
                    },
                    "message": "회원가입이 완료되었습니다",
                    "code": null,
                    "errorCode": null
                }
                """
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "400", 
        description = "잘못된 요청",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    name = "유효성 검증 실패",
                    value = """
                    {
                        "success": false,
                        "data": null,
                        "message": "입력값이 올바르지 않습니다",
                        "code": null,
                        "errorCode": "VALIDATION_ERROR"
                    }
                    """
                ),
                ExampleObject(
                    name = "이메일 형식 오류",
                    value = """
                    {
                        "success": false,
                        "data": null,
                        "message": "올바른 이메일 형식이 아닙니다",
                        "code": null,
                        "errorCode": "INVALID_EMAIL_FORMAT"
                    }
                    """
                )
            ]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "409", 
        description = "이메일 또는 전화번호 중복",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(
                value = """
                {
                    "success": false,
                    "data": null,
                    "message": "이미 사용 중인 이메일입니다",
                    "code": null,
                    "errorCode": "EMAIL_ALREADY_EXISTS"
                }
                """
            )]
        )]
    )
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
    @SwaggerApiResponse(
        responseCode = "200", 
        description = "로그인 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(
                value = """
                {
                    "success": true,
                    "data": {
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "authId": 1,
                        "email": "user@example.com",
                        "expiresIn": 86400
                    },
                    "message": "로그인이 완료되었습니다",
                    "code": null,
                    "errorCode": null
                }
                """
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "400", 
        description = "잘못된 요청",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(
                value = """
                {
                    "success": false,
                    "data": null,
                    "message": "이메일과 비밀번호를 입력해주세요",
                    "code": null,
                    "errorCode": "MISSING_CREDENTIALS"
                }
                """
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "401", 
        description = "인증 실패",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    name = "잘못된 비밀번호",
                    value = """
                    {
                        "success": false,
                        "data": null,
                        "message": "이메일 또는 비밀번호가 올바르지 않습니다",
                        "code": null,
                        "errorCode": "INVALID_CREDENTIALS"
                    }
                    """
                ),
                ExampleObject(
                    name = "존재하지 않는 사용자",
                    value = """
                    {
                        "success": false,
                        "data": null,
                        "message": "등록되지 않은 이메일입니다",
                        "code": null,
                        "errorCode": "USER_NOT_FOUND"
                    }
                    """
                )
            ]
        )]
    )
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
    @SwaggerApiResponse(
        responseCode = "200", 
        description = "확인 완료",
        content = [Content(
            mediaType = "application/json",
            examples = [
                ExampleObject(
                    name = "사용 가능한 이메일",
                    value = """
                    {
                        "success": true,
                        "data": true,
                        "message": "사용 가능한 이메일입니다",
                        "code": null,
                        "errorCode": null
                    }
                    """
                ),
                ExampleObject(
                    name = "이미 사용중인 이메일",
                    value = """
                    {
                        "success": true,
                        "data": false,
                        "message": "이미 사용 중인 이메일입니다",
                        "code": null,
                        "errorCode": null
                    }
                    """
                )
            ]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "400", 
        description = "잘못된 요청",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(
                value = """
                {
                    "success": false,
                    "data": null,
                    "message": "올바른 이메일 형식이 아닙니다",
                    "code": null,
                    "errorCode": "INVALID_EMAIL_FORMAT"
                }
                """
            )]
        )]
    )
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
    @SwaggerApiResponse(
        responseCode = "200", 
        description = "토큰 갱신 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(
                value = """
                {
                    "success": true,
                    "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    "message": "토큰이 갱신되었습니다",
                    "code": null,
                    "errorCode": null
                }
                """
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "401", 
        description = "유효하지 않은 리프레시 토큰",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [
                ExampleObject(
                    name = "만료된 토큰",
                    value = """
                    {
                        "success": false,
                        "data": null,
                        "message": "만료된 리프레시 토큰입니다",
                        "code": null,
                        "errorCode": "EXPIRED_REFRESH_TOKEN"
                    }
                    """
                ),
                ExampleObject(
                    name = "유효하지 않은 토큰",
                    value = """
                    {
                        "success": false,
                        "data": null,
                        "message": "유효하지 않은 리프레시 토큰입니다",
                        "code": null,
                        "errorCode": "INVALID_REFRESH_TOKEN"
                    }
                    """
                )
            ]
        )]
    )
    fun refreshToken(
        @RequestBody request: RefreshTokenRequestDto
    ): ResponseEntity<ApiResponse<String>> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }
}