package com.carava.carwash.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "고객 회원가입 요청")
data class SignUpRequestDto(
    @Schema(description = "이메일 주소", example = "user@example.com")
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @Schema(description = "비밀번호 (8자 이상, 대소문자/숫자/특수문자 포함)", example = "Password123!")
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8~20자리여야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]",
        message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다"
    )
    val password: String,

    @Schema(description = "이름", example = "홍길동")
    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(min = 2, max = 20, message = "이름은 2~20자리여야 합니다")
    val name: String,

    @Schema(description = "전화번호", example = "010-1234-5678")
    @field:NotBlank(message = "전화번호는 필수입니다")
    @field:Pattern(
        regexp = "^01[0-9]-[0-9]{4}-[0-9]{4}$",
        message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)"
    )
    val phone: String,

    @Schema(description = "생년월일", example = "1990-01-01")
    val birthDate: LocalDate? = null,

    @Schema(description = "닉네임", example = "길동이")
    @field:Size(max = 20, message = "닉네임은 20자리를 초과할 수 없습니다")
    val nickname: String? = null
)