package com.carava.carwash.auth.dto

data class SignInResponseDto (
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)