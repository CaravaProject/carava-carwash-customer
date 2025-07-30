package com.carava.carwash.auth.dto

import com.carava.carwash.auth.entity.Auth
import com.carava.carwash.member.entity.CustomerMember

data class SignUpResponseDto(
    val authId: Long,
    val customerId: Long,
    val email: String,
    val name: String,
    val phone: String,
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