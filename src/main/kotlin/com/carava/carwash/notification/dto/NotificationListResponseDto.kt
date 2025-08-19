package com.carava.carwash.notification.dto

import com.carava.carwash.notification.entity.Notification
import com.carava.carwash.notification.entity.NotificationChannel
import com.carava.carwash.notification.entity.NotificationStatus
import com.carava.carwash.notification.entity.NotificationType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 알림 목록 응답 DTO
 */
@Schema(description = "알림 목록 응답")
data class NotificationListResponseDto(
    @Schema(description = "알림 목록")
    val notifications: List<NotificationSummaryDto>,
    
    @Schema(description = "전체 개수")
    val totalCount: Long,
    
    @Schema(description = "읽지 않은 개수")
    val unreadCount: Long
)

/**
 * 알림 요약 DTO (목록용)
 */
@Schema(description = "알림 요약")
data class NotificationSummaryDto(
    @Schema(description = "알림 ID", example = "1")
    val id: Long,
    
    @Schema(description = "알림 제목", example = "예약 확인")
    val title: String,
    
    @Schema(description = "알림 내용", example = "예약이 확정되었습니다")
    val content: String,
    
    @Schema(description = "알림 타입", example = "RESERVATION_CONFIRMED")
    val type: NotificationType,
    
    @Schema(description = "알림 채널", example = "PUSH")
    val channel: NotificationChannel,
    
    @Schema(description = "알림 상태", example = "SENT")
    val status: NotificationStatus,
    
    @Schema(description = "읽음 여부", example = "false")
    val isRead: Boolean,
    
    @Schema(description = "생성 시간", example = "2024-01-15T10:30:00")
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notification: Notification): NotificationSummaryDto {
            return NotificationSummaryDto(
                id = notification.id,
                title = notification.title,
                content = notification.message,
                type = notification.notificationType,
                channel = notification.channel,
                status = notification.status,
                isRead = notification.readAt != null,
                createdAt = notification.createdAt
            )
        }
    }
}