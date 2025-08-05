package com.carava.carwash.notification.dto

import com.carava.carwash.notification.entity.NotificationSetting
import com.carava.carwash.notification.entity.NotificationType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * 알림 설정 응답 DTO
 */
@Schema(description = "알림 설정 정보")
data class NotificationSettingResponseDto(
    @Schema(description = "설정 ID", example = "1")
    val id: Long,
    
    @Schema(description = "알림 유형", example = "RESERVATION_CONFIRMED")
    val notificationType: NotificationType,
    
    @Schema(description = "알림 유형 표시명", example = "예약 확정")
    val notificationTypeName: String,
    
    @Schema(description = "푸시 알림 활성화", example = "true")
    val pushEnabled: Boolean,
    
    @Schema(description = "SMS 알림 활성화", example = "true")
    val smsEnabled: Boolean,
    
    @Schema(description = "이메일 알림 활성화", example = "false")
    val emailEnabled: Boolean,
    
    @Schema(description = "방해금지 시작 시간", example = "22")
    val quietStartHour: Int?,
    
    @Schema(description = "방해금지 종료 시간", example = "8")
    val quietEndHour: Int?,
    
    @Schema(description = "전체 활성화 여부", example = "true")
    val enabled: Boolean
) {
    companion object {
        fun from(setting: NotificationSetting): NotificationSettingResponseDto {
            return NotificationSettingResponseDto(
                id = setting.id,
                notificationType = setting.notificationType,
                notificationTypeName = setting.notificationType.displayName,
                pushEnabled = setting.pushEnabled,
                smsEnabled = setting.smsEnabled,
                emailEnabled = setting.emailEnabled,
                quietStartHour = setting.quietStartHour,
                quietEndHour = setting.quietEndHour,
                enabled = setting.enabled
            )
        }
    }
}

/**
 * 알림 설정 수정 요청 DTO
 */
@Schema(description = "알림 설정 수정 요청")
data class NotificationSettingUpdateRequestDto(
    @Schema(description = "푸시 알림 활성화", example = "true")
    val pushEnabled: Boolean? = null,
    
    @Schema(description = "SMS 알림 활성화", example = "true")
    val smsEnabled: Boolean? = null,
    
    @Schema(description = "이메일 알림 활성화", example = "false")
    val emailEnabled: Boolean? = null,
    
    @Schema(description = "방해금지 시작 시간 (0-23)", example = "22")
    @field:Min(value = 0, message = "시간은 0 이상이어야 합니다")
    @field:Max(value = 23, message = "시간은 23 이하여야 합니다")
    val quietStartHour: Int? = null,
    
    @Schema(description = "방해금지 종료 시간 (0-23)", example = "8")
    @field:Min(value = 0, message = "시간은 0 이상이어야 합니다")
    @field:Max(value = 23, message = "시간은 23 이하여야 합니다")
    val quietEndHour: Int? = null,
    
    @Schema(description = "전체 활성화 여부", example = "true")
    val enabled: Boolean? = null
)

/**
 * 전체 알림 설정 응답 DTO
 */
@Schema(description = "전체 알림 설정 응답")
data class NotificationSettingsResponseDto(
    @Schema(description = "알림 설정 목록")
    val settings: List<NotificationSettingResponseDto>,
    
    @Schema(description = "전체 푸시 알림 활성화", example = "true")
    val globalPushEnabled: Boolean,
    
    @Schema(description = "전체 SMS 알림 활성화", example = "true")
    val globalSmsEnabled: Boolean,
    
    @Schema(description = "전체 이메일 알림 활성화", example = "false")
    val globalEmailEnabled: Boolean
) {
    companion object {
        fun from(settings: List<NotificationSetting>): NotificationSettingsResponseDto {
            val settingDtos = settings.map { NotificationSettingResponseDto.from(it) }
            
            return NotificationSettingsResponseDto(
                settings = settingDtos,
                globalPushEnabled = settings.any { it.pushEnabled && it.enabled },
                globalSmsEnabled = settings.any { it.smsEnabled && it.enabled },
                globalEmailEnabled = settings.any { it.emailEnabled && it.enabled }
            )
        }
    }
}

/**
 * 전체 알림 설정 수정 요청 DTO
 */
@Schema(description = "전체 알림 설정 수정 요청")
data class GlobalNotificationSettingUpdateRequestDto(
    @Schema(description = "전체 푸시 알림 활성화", example = "true")
    val globalPushEnabled: Boolean? = null,
    
    @Schema(description = "전체 SMS 알림 활성화", example = "true")
    val globalSmsEnabled: Boolean? = null,
    
    @Schema(description = "전체 이메일 알림 활성화", example = "false")
    val globalEmailEnabled: Boolean? = null,
    
    @Schema(description = "마케팅 알림 수신 동의", example = "false")
    val marketingEnabled: Boolean? = null
)

/**
 * FCM 토큰 등록 요청 DTO
 */
@Schema(description = "FCM 토큰 등록 요청")
data class FcmTokenRequestDto(
    @Schema(description = "FCM 토큰", example = "fGHG3...")
    val token: String,
    
    @Schema(description = "디바이스 정보", example = "iPhone 15 Pro")
    val deviceInfo: String? = null
)