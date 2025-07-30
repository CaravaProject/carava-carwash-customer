package com.carava.carwash.auth.dto

import com.carava.carwash.auth.entity.Auth
import com.carava.carwash.member.entity.CustomerMember

data class SignInResponseDto(
    val authId: Long,
    val customerId: Long,
    val email: String,
    val name: String,
    val accessToken: String,
    val refreshToken: String
) {
    companion object {
        fun from(
            auth: Auth, 
            customer: CustomerMember,
            accessToken: String,
            refreshToken: String
        ): SignInResponseDto {
            return SignInResponseDto(
                authId = auth.id,
                customerId = customer.id,
                email = auth.email,
                name = customer.name,
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }
    }
}