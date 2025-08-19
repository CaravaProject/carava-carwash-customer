package com.carava.carwash.notification.service

import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.notification.dto.*
import com.carava.carwash.notification.entity.*
import com.carava.carwash.notification.repository.NotificationSettingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationSettingService(
    private val notificationSettingRepository: NotificationSettingRepository
) {

    /**
     * 사용자 알림 설정 목록 조회
     */
    fun getUserNotificationSettings(
        userId: Long,
        userType: RecipientType
    ): ApiResponse<NotificationSettingsResponseDto> {
        return try {
            val settings = getUserSettings(userId, userType)
            val responseDto = NotificationSettingsResponseDto.from(settings)
            ApiResponse.success(responseDto, "알림 설정 조회가 완료되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("SETTINGS_FETCH_FAILED", "알림 설정 조회 중 오류가 발생했습니다")
        }
    }

    /**
     * 특정 알림 유형 설정 수정
     */
    @Transactional
    fun updateNotificationTypeSetting(
        userId: Long,
        userType: RecipientType,
        notificationType: NotificationType,
        request: NotificationSettingUpdateRequestDto
    ): ApiResponse<NotificationSettingResponseDto> {
        return try {
            val setting = getOrCreateSetting(userId, userType, notificationType)
            
            // 설정 업데이트
            request.pushEnabled?.let { setting.pushEnabled = it }
            request.smsEnabled?.let { setting.smsEnabled = it }
            request.emailEnabled?.let { setting.emailEnabled = it }
            request.quietStartHour?.let { setting.quietStartHour = it }
            request.quietEndHour?.let { setting.quietEndHour = it }
            request.enabled?.let { setting.enabled = it }
            
            val savedSetting = notificationSettingRepository.save(setting)
            val responseDto = NotificationSettingResponseDto.from(savedSetting)
            
            ApiResponse.success(responseDto, "알림 설정이 성공적으로 수정되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("SETTINGS_UPDATE_FAILED", "알림 설정 수정 중 오류가 발생했습니다")
        }
    }

    /**
     * 전체 알림 설정 수정
     */
    @Transactional
    fun updateGlobalNotificationSettings(
        userId: Long,
        userType: RecipientType,
        request: GlobalNotificationSettingUpdateRequestDto
    ): ApiResponse<NotificationSettingsResponseDto> {
        return try {
            val settings = getUserSettings(userId, userType)
            
            // 전체 설정 업데이트
            request.globalPushEnabled?.let { enabled ->
                settings.forEach { setting ->
                    setting.pushEnabled = enabled
                }
            }
            
            request.globalSmsEnabled?.let { enabled ->
                settings.forEach { setting ->
                    setting.smsEnabled = enabled
                }
            }
            
            request.globalEmailEnabled?.let { enabled ->
                settings.forEach { setting ->
                    setting.emailEnabled = enabled
                }
            }
            
            // 마케팅 알림 수신 동의 처리
            request.marketingEnabled?.let { enabled ->
                settings.filter { it.notificationType.isMarketingNotification() }
                    .forEach { setting ->
                        setting.enabled = enabled
                    }
            }
            
            notificationSettingRepository.saveAll(settings)
            
            val updatedSettings = getUserSettings(userId, userType)
            val responseDto = NotificationSettingsResponseDto.from(updatedSettings)
            
            ApiResponse.success(responseDto, "전체 알림 설정이 성공적으로 수정되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("GLOBAL_SETTINGS_UPDATE_FAILED", "전체 알림 설정 수정 중 오류가 발생했습니다")
        }
    }

    /**
     * 마케팅 알림 수신 거부
     */
    @Transactional
    fun disableMarketingNotifications(
        userId: Long,
        userType: RecipientType
    ): ApiResponse<Nothing> {
        return try {
            val marketingTypes = NotificationType.values()
                .filter { it.isMarketingNotification() }
            
            val updatedCount = notificationSettingRepository.disableMarketingNotifications(
                userId, userType, marketingTypes
            )
            
            ApiResponse.success(null, "${updatedCount}개의 마케팅 알림이 비활성화되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("MARKETING_DISABLE_FAILED", "마케팅 알림 비활성화 중 오류가 발생했습니다")
        }
    }

    /**
     * 사용자 회원 가입 시 기본 알림 설정 생성
     */
    @Transactional
    fun createDefaultSettings(
        userId: Long,
        userType: RecipientType
    ): List<NotificationSetting> {
        return try {
            val existingSettings = notificationSettingRepository.findByUserIdAndUserType(userId, userType)
            if (existingSettings.isNotEmpty()) {
                return existingSettings
            }
            
            val defaultSettings = NotificationType.values().map { type ->
                NotificationSetting.createDefault(userId, userType, type)
            }
            
            notificationSettingRepository.saveAll(defaultSettings)
            
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 사용자 탈퇴 시 알림 설정 삭제
     */
    @Transactional
    fun deleteUserSettings(
        userId: Long,
        userType: RecipientType
    ): ApiResponse<Nothing> {
        return try {
            val deletedCount = notificationSettingRepository.deleteAllByUser(userId, userType)
            ApiResponse.success(null, "${deletedCount}개의 알림 설정이 삭제되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("SETTINGS_DELETE_FAILED", "알림 설정 삭제 중 오류가 발생했습니다")
        }
    }

    /**
     * 사용자 알림 설정 조회 (내부용)
     */
    fun getUserSettings(
        userId: Long,
        userType: RecipientType
    ): List<NotificationSetting> {
        val settings = notificationSettingRepository.findByUserIdAndUserType(userId, userType)
        
        // 설정이 없는 경우 기본 설정 생성
        return if (settings.isEmpty()) {
            createDefaultSettings(userId, userType)
        } else {
            settings
        }
    }

    /**
     * 특정 채널이 활성화된 사용자 목록 조회 (마케팅용)
     */
    fun getUsersWithEnabledChannel(
        userType: RecipientType,
        channel: NotificationChannel
    ): List<Long> {
        // TODO: 모든 사용자를 대상으로 조회하는 메서드가 필요함
        // 현재는 특정 사용자만 조회 가능한 구조
        return emptyList()
    }

    /**
     * 마케팅 알림 수신 동의한 사용자 목록 조회
     */
    fun getUsersWithMarketingConsent(
        userType: RecipientType,
        channel: NotificationChannel
    ): List<Long> {
        val marketingTypes = NotificationType.values()
            .filter { it.isMarketingNotification() }
        
        return notificationSettingRepository.findUserIdsWithMarketingConsent(
            userType = userType,
            marketingTypes = marketingTypes,
            channel = channel.name
        )
    }

    private fun getOrCreateSetting(
        userId: Long,
        userType: RecipientType,
        notificationType: NotificationType
    ): NotificationSetting {
        return notificationSettingRepository
            .findByUserIdAndUserTypeAndNotificationType(userId, userType, notificationType)
            .orElseGet {
                NotificationSetting.createDefault(userId, userType, notificationType)
            }
    }
}