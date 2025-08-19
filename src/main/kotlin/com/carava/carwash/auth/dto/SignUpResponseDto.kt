package com.carava.carwash.auth.dto

import com.carava.carwash.auth.entity.Auth
import com.carava.carwash.member.entity.CustomerMember
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "고객 회원가입 응답")
data class SignUpResponseDto(
    @Schema(description = "인증 ID", example = "1")
    val authId: Long,
    
    @Schema(description = "고객 ID", example = "1")
    val customerId: Long,
    
    @Schema(description = "이메일", example = "user@example.com")
    val email: String,
    
    @Schema(description = "이름", example = "홍길동")
    val name: String,
    
    @Schema(description = "전화번호", example = "010-1234-5678")
    val phone: String,
    
    @Schema(description = "닉네임", example = "길동이")
    val nickname: String?
) {
    companion object {
        fun from(auth: Auth, customer: CustomerMember): SignUpResponseDto {
            return SignUpResponseDto(
                authId = auth.id,
                customerId = customer.id,
                email = auth.email,
                name = customer.name,
                phone = customer.phone,
                nickname = customer.nickname
            )
        }
    }
}