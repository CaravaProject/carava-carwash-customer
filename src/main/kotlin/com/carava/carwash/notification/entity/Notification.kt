package com.carava.carwash.notification.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 알림 엔티티
 */
@Entity
@Table(
    name = "notification",
    indexes = [
        Index(name = "idx_notification_recipient", columnList = "recipient_id, recipient_type"),
        Index(name = "idx_notification_status", columnList = "status"),
        Index(name = "idx_notification_type", columnList = "notification_type"),
        Index(name = "idx_notification_scheduled", columnList = "scheduled_at"),
        Index(name = "idx_notification_created", columnList = "created_at")
    ]
)
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    // 수신자 정보
    @Column(name = "recipient_id", nullable = false)
    val recipientId: Long,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 20)
    val recipientType: RecipientType,
    
    // 알림 내용
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    val notificationType: NotificationType,
    
    @Column(name = "title", nullable = false, length = 100)
    val title: String,
    
    @Column(name = "message", nullable = false, length = 500)
    val message: String,
    
    @Column(name = "data_payload", columnDefinition = "TEXT")
    val dataPayload: String? = null, // JSON 형태로 추가 데이터 저장
    
    // 발송 채널 및 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    val channel: NotificationChannel,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: NotificationStatus = NotificationStatus.PENDING,
    
    // 발송 관련 정보
    @Column(name = "scheduled_at")
    val scheduledAt: LocalDateTime? = null, // 예약 발송 시간
    
    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,
    
    @Column(name = "delivered_at")
    var deliveredAt: LocalDateTime? = null,
    
    @Column(name = "read_at")
    var readAt: LocalDateTime? = null,
    
    // 실패 관련 정보
    @Column(name = "failure_reason", length = 200)
    var failureReason: String? = null,
    
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,
    
    // 관련 엔티티 정보 (선택적)
    @Column(name = "related_entity_type", length = 50)
    val relatedEntityType: String? = null, // "RESERVATION", "CHAT", "REVIEW" 등
    
    @Column(name = "related_entity_id")
    val relatedEntityId: Long? = null,
    
    // 외부 서비스 응답 정보
    @Column(name = "external_id", length = 100)
    var externalId: String? = null, // FCM message ID, SMS ID 등
    
    @Column(name = "external_response", columnDefinition = "TEXT")
    var externalResponse: String? = null // 외부 서비스 응답 전체
    
) : BaseEntity() {
    
    companion object {
        /**
         * 즉시 발송 알림 생성
         */
        fun createImmediate(
            recipientId: Long,
            recipientType: RecipientType,
            notificationType: NotificationType,
            title: String,
            message: String,
            channel: NotificationChannel,
            dataPayload: String? = null,
            relatedEntityType: String? = null,
            relatedEntityId: Long? = null
        ): Notification {
            return Notification(
                recipientId = recipientId,
                recipientType = recipientType,
                notificationType = notificationType,
                title = title,
                message = message,
                channel = channel,
                dataPayload = dataPayload,
                relatedEntityType = relatedEntityType,
                relatedEntityId = relatedEntityId
            )
        }
        
        /**
         * 예약 발송 알림 생성
         */
        fun createScheduled(
            recipientId: Long,
            recipientType: RecipientType,
            notificationType: NotificationType,
            title: String,
            message: String,
            channel: NotificationChannel,
            scheduledAt: LocalDateTime,
            dataPayload: String? = null,
            relatedEntityType: String? = null,
            relatedEntityId: Long? = null
        ): Notification {
            return Notification(
                recipientId = recipientId,
                recipientType = recipientType,
                notificationType = notificationType,
                title = title,
                message = message,
                channel = channel,
                scheduledAt = scheduledAt,
                dataPayload = dataPayload,
                relatedEntityType = relatedEntityType,
                relatedEntityId = relatedEntityId
            )
        }
    }
    
    /**
     * 알림 발송 완료 처리
     */
    fun markAsSent(externalId: String? = null, externalResponse: String? = null) {
        this.status = NotificationStatus.SENT
        this.sentAt = LocalDateTime.now()
        this.externalId = externalId
        this.externalResponse = externalResponse
    }
    
    /**
     * 알림 전달 완료 처리
     */
    fun markAsDelivered() {
        this.status = NotificationStatus.DELIVERED
        this.deliveredAt = LocalDateTime.now()
    }
    
    /**
     * 알림 읽음 처리
     */
    fun markAsRead() {
        this.status = NotificationStatus.READ
        this.readAt = LocalDateTime.now()
    }
    
    /**
     * 알림 실패 처리
     */
    fun markAsFailed(reason: String) {
        this.status = NotificationStatus.FAILED
        this.failureReason = reason
        this.retryCount++
    }
    
    /**
     * 재시도 가능 여부 확인
     */
    fun canRetry(maxRetryCount: Int = 3): Boolean {
        return status == NotificationStatus.FAILED && retryCount < maxRetryCount
    }
    
    /**
     * 예약 발송 시간 확인
     */
    fun isScheduledForFuture(): Boolean {
        return scheduledAt != null && scheduledAt > LocalDateTime.now()
    }
}