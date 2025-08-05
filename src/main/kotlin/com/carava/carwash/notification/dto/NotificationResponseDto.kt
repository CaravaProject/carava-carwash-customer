package com.carava.carwash.notification.dto

import com.carava.carwash.notification.entity.*
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 알림 응답 DTO
 */
@Schema(description = "알림 정보 응답")
data class NotificationResponseDto(
    @Schema(description = "알림 ID", example = "1")
    val id: Long,
    
    @Schema(description = "알림 유형", example = "RESERVATION_CONFIRMED")
    val notificationType: NotificationType,
    
    @Schema(description = "제목", example = "예약이 확정되었습니다")
    val title: String,
    
    @Schema(description = "메시지", example = "2024년 1월 15일 14:00 예약이 확정되었습니다")
    val message: String,
    
    @Schema(description = "추가 데이터")
    val dataPayload: Map<String, Any>?,
    
    @Schema(description = "알림 채널", example = "PUSH")
    val channel: NotificationChannel,
    
    @Schema(description = "알림 상태", example = "DELIVERED")
    val status: NotificationStatus,
    
    @Schema(description = "발송 예정 시간")
    val scheduledAt: LocalDateTime?,
    
    @Schema(description = "발송 시간")
    val sentAt: LocalDateTime?,
    
    @Schema(description = "전달 시간")
    val deliveredAt: LocalDateTime?,
    
    @Schema(description = "읽음 시간")
    val readAt: LocalDateTime?,
    
    @Schema(description = "관련 엔티티 타입", example = "RESERVATION")
    val relatedEntityType: String?,
    
    @Schema(description = "관련 엔티티 ID", example = "123")
    val relatedEntityId: Long?,
    
    @Schema(description = "생성 시간")
    val createdAt: LocalDateTime,
    
    @Schema(description = "읽지 않음 여부", example = "true")
    val isUnread: Boolean
) {
    companion object {
        fun from(notification: Notification): NotificationResponseDto {
            val dataPayload = notification.dataPayload?.let { 
                try {
                    // JSON 파싱 (실제로는 Jackson ObjectMapper 사용)
                    mapOf<String, Any>() // 임시로 빈 맵 반환
                } catch (e: Exception) {
                    null
                }
            }
            
            return NotificationResponseDto(
                id = notification.id,
                notificationType = notification.notificationType,
                title = notification.title,
                message = notification.message,
                dataPayload = dataPayload,
                channel = notification.channel,
                status = notification.status,
                scheduledAt = notification.scheduledAt,
                sentAt = notification.sentAt,
                deliveredAt = notification.deliveredAt,
                readAt = notification.readAt,
                relatedEntityType = notification.relatedEntityType,
                relatedEntityId = notification.relatedEntityId,
                createdAt = notification.createdAt,
                isUnread = notification.status in listOf(
                    NotificationStatus.SENT, 
                    NotificationStatus.DELIVERED
                )
            )
        }
    }
}

/**
 * 알림 목록 응답용 간단한 DTO
 */
@Schema(description = "알림 목록 응답")
data class NotificationListResponseDto(
    @Schema(description = "알림 ID", example = "1")
    val id: Long,
    
    @Schema(description = "알림 유형", example = "RESERVATION_CONFIRMED")
    val notificationType: NotificationType,
    
    @Schema(description = "제목", example = "예약이 확정되었습니다")
    val title: String,
    
    @Schema(description = "메시지", example = "2024년 1월 15일 14:00 예약이 확정되었습니다")
    val message: String,
    
    @Schema(description = "알림 상태", example = "DELIVERED")
    val status: NotificationStatus,
    
    @Schema(description = "생성 시간")
    val createdAt: LocalDateTime,
    
    @Schema(description = "읽지 않음 여부", example = "true")
    val isUnread: Boolean
) {
    companion object {
        fun from(notification: Notification): NotificationListResponseDto {
            return NotificationListResponseDto(
                id = notification.id,
                notificationType = notification.notificationType,
                title = notification.title,
                message = notification.message,
                status = notification.status,
                createdAt = notification.createdAt,
                isUnread = notification.status in listOf(
                    NotificationStatus.SENT, 
                    NotificationStatus.DELIVERED
                )
            )
        }
    }
}

/**
 * 읽지 않은 알림 개수 응답 DTO
 */
@Schema(description = "읽지 않은 알림 개수 응답")
data class UnreadNotificationCountDto(
    @Schema(description = "전체 읽지 않은 알림 개수", example = "5")
    val totalCount: Long,
    
    @Schema(description = "알림 유형별 개수")
    val countByType: Map<NotificationType, Long>
) {
    companion object {
        fun create(
            totalCount: Long,
            countByType: Map<NotificationType, Long> = emptyMap()
        ): UnreadNotificationCountDto {
            return UnreadNotificationCountDto(
                totalCount = totalCount,
                countByType = countByType
            )
        }
    }
}