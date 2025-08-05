package com.carava.carwash.notification.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*

/**
 * 사용자별 알림 설정 엔티티
 */
@Entity
@Table(
    name = "notification_setting",
    indexes = [
        Index(name = "idx_notification_setting_user", columnList = "user_id, user_type"),
        Index(name = "idx_notification_setting_type", columnList = "notification_type")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_notification_setting_user_type",
            columnNames = ["user_id", "user_type", "notification_type"]
        )
    ]
)
data class NotificationSetting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    // 사용자 정보
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    val userType: RecipientType,
    
    // 알림 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    val notificationType: NotificationType,
    
    // 채널별 설정
    @Column(name = "push_enabled", nullable = false)
    var pushEnabled: Boolean = true,
    
    @Column(name = "sms_enabled", nullable = false)
    var smsEnabled: Boolean = true,
    
    @Column(name = "email_enabled", nullable = false)
    var emailEnabled: Boolean = false,
    
    // 수신 시간 설정 (24시간 형식)
    @Column(name = "quiet_start_hour")
    var quietStartHour: Int? = null, // 방해금지 시작 시간 (예: 22)
    
    @Column(name = "quiet_end_hour")
    var quietEndHour: Int? = null,   // 방해금지 종료 시간 (예: 8)
    
    // 활성화 여부
    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true
    
) : BaseEntity() {
    
    companion object {
        /**
         * 기본 알림 설정 생성
         */
        fun createDefault(
            userId: Long,
            userType: RecipientType,
            notificationType: NotificationType
        ): NotificationSetting {
            val defaultChannels = notificationType.getDefaultChannels()
            
            return NotificationSetting(
                userId = userId,
                userType = userType,
                notificationType = notificationType,
                pushEnabled = defaultChannels.contains(NotificationChannel.PUSH),
                smsEnabled = defaultChannels.contains(NotificationChannel.SMS),
                emailEnabled = defaultChannels.contains(NotificationChannel.EMAIL)
            )
        }
    }
    
    /**
     * 특정 채널의 알림이 활성화되어 있는지 확인
     */
    fun isChannelEnabled(channel: NotificationChannel): Boolean {
        if (!enabled) return false
        
        return when (channel) {
            NotificationChannel.PUSH -> pushEnabled
            NotificationChannel.SMS -> smsEnabled
            NotificationChannel.EMAIL -> emailEnabled
        }
    }
    
    /**
     * 현재 시간이 방해금지 시간인지 확인
     */
    fun isQuietTime(currentHour: Int): Boolean {
        if (quietStartHour == null || quietEndHour == null) return false
        
        return if (quietStartHour!! <= quietEndHour!!) {
            // 같은 날 (예: 22시-8시)
            currentHour >= quietStartHour!! && currentHour < quietEndHour!!
        } else {
            // 다음 날로 넘어가는 경우 (예: 22시-08시)
            currentHour >= quietStartHour!! || currentHour < quietEndHour!!
        }
    }
    
    /**
     * 마케팅 알림 수신 거부 시 비활성화
     */
    fun disableIfMarketing() {
        if (notificationType.isMarketingNotification()) {
            this.enabled = false
        }
    }
    
    /**
     * 채널별 설정 업데이트
     */
    fun updateChannelSettings(
        pushEnabled: Boolean? = null,
        smsEnabled: Boolean? = null,
        emailEnabled: Boolean? = null
    ) {
        pushEnabled?.let { this.pushEnabled = it }
        smsEnabled?.let { this.smsEnabled = it }
        emailEnabled?.let { this.emailEnabled = it }
    }
    
    /**
     * 방해금지 시간 설정
     */
    fun setQuietHours(startHour: Int?, endHour: Int?) {
        this.quietStartHour = startHour
        this.quietEndHour = endHour
    }
}