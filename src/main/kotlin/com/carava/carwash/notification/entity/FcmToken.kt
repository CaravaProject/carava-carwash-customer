package com.carava.carwash.notification.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * FCM 토큰 관리 엔티티
 * 사용자별 FCM 토큰을 관리하고 토큰의 유효성을 추적
 */
@Entity
@Table(
    name = "fcm_tokens",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "token"])
    ],
    indexes = [
        Index(name = "idx_fcm_token_user_id", columnList = "user_id"),
        Index(name = "idx_fcm_token_is_active", columnList = "is_active"),
        Index(name = "idx_fcm_token_last_used", columnList = "last_used_at")
    ]
)
data class FcmToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * 사용자 ID (Customer Member ID)
     */
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    /**
     * FCM 토큰 값
     */
    @Column(name = "token", nullable = false, length = 255)
    val token: String,

    /**
     * 디바이스 정보
     */
    @Column(name = "device_id", length = 100)
    val deviceId: String? = null,

    /**
     * 디바이스 타입 (android, ios, web)
     */
    @Column(name = "device_type", length = 20)
    val deviceType: String? = null,

    /**
     * 앱 버전
     */
    @Column(name = "app_version", length = 20)
    val appVersion: String? = null,

    /**
     * 토큰 활성화 상태
     */
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    /**
     * 마지막 사용 시간
     */
    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,

    /**
     * 토큰 만료 시간 (선택적)
     */
    @Column(name = "expires_at")
    val expiresAt: LocalDateTime? = null

) : BaseEntity() {

    /**
     * 토큰을 비활성화
     */
    fun deactivate() {
        this.isActive = false
    }

    /**
     * 마지막 사용 시간 업데이트
     */
    fun updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now()
    }

    /**
     * 토큰이 유효한지 확인
     */
    fun isValid(): Boolean {
        if (!isActive) return false
        
        return expiresAt?.let { expiry ->
            LocalDateTime.now().isBefore(expiry)
        } ?: true
    }

    companion object {
        /**
         * 새로운 FCM 토큰 생성
         */
        fun create(
            userId: Long,
            token: String,
            deviceId: String? = null,
            deviceType: String? = null,
            appVersion: String? = null,
            expiresAt: LocalDateTime? = null
        ): FcmToken {
            return FcmToken(
                userId = userId,
                token = token,
                deviceId = deviceId,
                deviceType = deviceType,
                appVersion = appVersion,
                lastUsedAt = LocalDateTime.now(),
                expiresAt = expiresAt
            )
        }
    }
}