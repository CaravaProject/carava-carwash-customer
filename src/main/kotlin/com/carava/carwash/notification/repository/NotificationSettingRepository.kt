package com.carava.carwash.notification.repository

import com.carava.carwash.notification.entity.NotificationSetting
import com.carava.carwash.notification.entity.NotificationType
import com.carava.carwash.notification.entity.RecipientType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationSettingRepository : JpaRepository<NotificationSetting, Long> {
    
    /**
     * 사용자별 알림 설정 목록 조회
     */
    fun findByUserIdAndUserType(
        userId: Long,
        userType: RecipientType
    ): List<NotificationSetting>
    
    /**
     * 특정 알림 유형의 설정 조회
     */
    fun findByUserIdAndUserTypeAndNotificationType(
        userId: Long,
        userType: RecipientType,
        notificationType: NotificationType
    ): Optional<NotificationSetting>
    
    /**
     * 사용자의 모든 알림 설정 존재 여부 확인
     */
    fun existsByUserIdAndUserType(
        userId: Long,
        userType: RecipientType
    ): Boolean
    
    /**
     * 활성화된 알림 설정만 조회
     */
    fun findByUserIdAndUserTypeAndEnabledTrue(
        userId: Long,
        userType: RecipientType
    ): List<NotificationSetting>
    
    /**
     * 특정 채널이 활성화된 설정 조회
     */
    @Query("""
        SELECT ns FROM NotificationSetting ns 
        WHERE ns.userId = :userId 
        AND ns.userType = :userType 
        AND ns.enabled = true
        AND (
            (:channel = 'PUSH' AND ns.pushEnabled = true) OR
            (:channel = 'SMS' AND ns.smsEnabled = true) OR
            (:channel = 'EMAIL' AND ns.emailEnabled = true)
        )
    """)
    fun findByUserAndEnabledChannel(
        @Param("userId") userId: Long,
        @Param("userType") userType: RecipientType,
        @Param("channel") channel: String
    ): List<NotificationSetting>
    
    /**
     * 마케팅 알림 수신 동의한 사용자 목록 조회
     */
    @Query("""
        SELECT DISTINCT ns.userId FROM NotificationSetting ns 
        WHERE ns.userType = :userType 
        AND ns.enabled = true
        AND ns.notificationType IN :marketingTypes
        AND (
            (:channel = 'PUSH' AND ns.pushEnabled = true) OR
            (:channel = 'SMS' AND ns.smsEnabled = true) OR
            (:channel = 'EMAIL' AND ns.emailEnabled = true)
        )
    """)
    fun findUserIdsWithMarketingConsent(
        @Param("userType") userType: RecipientType,
        @Param("marketingTypes") marketingTypes: List<NotificationType>,
        @Param("channel") channel: String
    ): List<Long>
    
    /**
     * 방해금지 시간이 설정된 사용자 설정 조회
     */
    @Query("""
        SELECT ns FROM NotificationSetting ns 
        WHERE ns.userId = :userId 
        AND ns.userType = :userType 
        AND ns.quietStartHour IS NOT NULL 
        AND ns.quietEndHour IS NOT NULL
    """)
    fun findByUserWithQuietHours(
        @Param("userId") userId: Long,
        @Param("userType") userType: RecipientType
    ): List<NotificationSetting>
    
    /**
     * 사용자의 모든 알림 설정 삭제
     */
    @Modifying
    @Query("""
        DELETE FROM NotificationSetting ns 
        WHERE ns.userId = :userId 
        AND ns.userType = :userType
    """)
    fun deleteAllByUser(
        @Param("userId") userId: Long,
        @Param("userType") userType: RecipientType
    ): Int
    
    /**
     * 사용자의 모든 마케팅 알림 비활성화
     */
    @Modifying
    @Query("""
        UPDATE NotificationSetting ns 
        SET ns.enabled = false
        WHERE ns.userId = :userId 
        AND ns.userType = :userType
        AND ns.notificationType IN :marketingTypes
    """)
    fun disableMarketingNotifications(
        @Param("userId") userId: Long,
        @Param("userType") userType: RecipientType,
        @Param("marketingTypes") marketingTypes: List<NotificationType>
    ): Int
    
    /**
     * 특정 알림 유형을 전체 사용자에 대해 비활성화 (긴급 시)
     */
    @Modifying
    @Query("""
        UPDATE NotificationSetting ns 
        SET ns.enabled = false
        WHERE ns.notificationType = :notificationType
    """)
    fun disableNotificationTypeGlobally(
        @Param("notificationType") notificationType: NotificationType
    ): Int
    
    /**
     * 특정 채널을 전체 사용자에 대해 비활성화 (긴급 시)
     */
    @Modifying
    @Query("""
        UPDATE NotificationSetting ns 
        SET 
            ns.pushEnabled = CASE WHEN :channel = 'PUSH' THEN false ELSE ns.pushEnabled END,
            ns.smsEnabled = CASE WHEN :channel = 'SMS' THEN false ELSE ns.smsEnabled END,
            ns.emailEnabled = CASE WHEN :channel = 'EMAIL' THEN false ELSE ns.emailEnabled END
    """)
    fun disableChannelGlobally(
        @Param("channel") channel: String
    ): Int
}