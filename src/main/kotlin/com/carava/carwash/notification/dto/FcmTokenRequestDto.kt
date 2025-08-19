package com.carava.carwash.notification.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * FCM 토큰 등록 요청 DTO
 */
@Schema(description = "FCM 토큰 등록 요청")
data class FcmTokenRequestDto(
    
    @field:NotBlank(message = "FCM 토큰은 필수입니다")
    @field:Size(max = 255, message = "FCM 토큰은 255자 이하여야 합니다")
    @Schema(description = "FCM 토큰", example = "dA1B2c3D4e5F6g7H8i9J0k1L2m3N4o5P6q7R8s9T0u1V2w3X4y5Z6a7B8c9D0e1F", required = true)
    val token: String,
    
    @field:Size(max = 100, message = "디바이스 ID는 100자 이하여야 합니다")
    @Schema(description = "디바이스 ID", example = "device_12345", required = false)
    val deviceId: String? = null,
    
    @field:Size(max = 20, message = "디바이스 타입은 20자 이하여야 합니다")
    @Schema(description = "디바이스 타입", example = "android", allowableValues = ["android", "ios", "web"], required = false)
    val deviceType: String? = null,
    
    @field:Size(max = 20, message = "앱 버전은 20자 이하여야 합니다")
    @Schema(description = "앱 버전", example = "1.0.0", required = false)
    val appVersion: String? = null
)