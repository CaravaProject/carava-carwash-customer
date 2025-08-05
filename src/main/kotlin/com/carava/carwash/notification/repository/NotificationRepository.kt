package com.carava.carwash.notification.repository

import com.carava.carwash.notification.entity.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    
    /**
     * 수신자별 알림 목록 조회 (페이징)
     */
    fun findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
        recipientId: Long,
        recipientType: RecipientType,
        pageable: Pageable
    ): Page<Notification>
    
    /**
     * 수신자별 읽지 않은 알림 개수 조회
     */
    fun countByRecipientIdAndRecipientTypeAndStatusIn(
        recipientId: Long,
        recipientType: RecipientType,
        statuses: List<NotificationStatus>
    ): Long
    
    /**
     * 수신자별 읽지 않은 알림 목록 조회
     */
    fun findByRecipientIdAndRecipientTypeAndStatusInOrderByCreatedAtDesc(
        recipientId: Long,
        recipientType: RecipientType,
        statuses: List<NotificationStatus>
    ): List<Notification>
    
    /**
     * 발송 대기중인 알림 목록 조회 (예약 발송 포함)
     */
    @Query("""
        SELECT n FROM Notification n 
        WHERE n.status = :status 
        AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)
        ORDER BY n.createdAt ASC
    """)
    fun findPendingNotifications(
        @Param("status") status: NotificationStatus,
        @Param("now") now: LocalDateTime
    ): List<Notification>
    
    /**
     * 특정 채널의 발송 대기중인 알림 목록 조회
     */
    @Query("""
        SELECT n FROM Notification n 
        WHERE n.status = :status 
        AND n.channel = :channel
        AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now)
        ORDER BY n.createdAt ASC
    """)
    fun findPendingNotificationsByChannel(
        @Param("status") status: NotificationStatus,
        @Param("channel") channel: NotificationChannel,
        @Param("now") now: LocalDateTime
    ): List<Notification>
    
    /**
     * 재시도 가능한 실패 알림 목록 조회
     */
    @Query("""
        SELECT n FROM Notification n 
        WHERE n.status = :status 
        AND n.retryCount < :maxRetryCount
        ORDER BY n.createdAt ASC
    """)
    fun findRetryableFailedNotifications(
        @Param("status") status: NotificationStatus,
        @Param("maxRetryCount") maxRetryCount: Int
    ): List<Notification>
    
    /**
     * 특정 엔티티와 관련된 알림 목록 조회
     */
    fun findByRelatedEntityTypeAndRelatedEntityId(
        relatedEntityType: String,
        relatedEntityId: Long
    ): List<Notification>
    
    /**
     * 특정 기간 내 발송된 알림 개수 조회 (통계용)
     */
    @Query("""
        SELECT COUNT(n) FROM Notification n 
        WHERE n.status IN :successStatuses
        AND n.sentAt BETWEEN :startDate AND :endDate
    """)
    fun countSentNotificationsBetween(
        @Param("successStatuses") successStatuses: List<NotificationStatus>,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long
    
    /**
     * 특정 유형의 알림 개수 조회 (통계용)
     */
    fun countByNotificationTypeAndStatusIn(
        notificationType: NotificationType,
        statuses: List<NotificationStatus>
    ): Long
    
    /**
     * 오래된 알림 삭제 (배치 작업용)
     */
    @Modifying
    @Query("""
        DELETE FROM Notification n 
        WHERE n.createdAt < :cutoffDate
        AND n.status IN :completedStatuses
    """)
    fun deleteOldNotifications(
        @Param("cutoffDate") cutoffDate: LocalDateTime,
        @Param("completedStatuses") completedStatuses: List<NotificationStatus>
    ): Int
    
    /**
     * 수신자의 모든 알림을 읽음 처리
     */
    @Modifying
    @Query("""
        UPDATE Notification n 
        SET n.status = :readStatus, n.readAt = :readAt
        WHERE n.recipientId = :recipientId 
        AND n.recipientType = :recipientType
        AND n.status IN :unreadStatuses
    """)
    fun markAllAsReadByRecipient(
        @Param("recipientId") recipientId: Long,
        @Param("recipientType") recipientType: RecipientType,
        @Param("readStatus") readStatus: NotificationStatus,
        @Param("readAt") readAt: LocalDateTime,
        @Param("unreadStatuses") unreadStatuses: List<NotificationStatus>
    ): Int
    
    /**
     * 예약 발송 알림 목록 조회 (스케줄러용)
     */
    @Query("""
        SELECT n FROM Notification n 
        WHERE n.status = :status 
        AND n.scheduledAt IS NOT NULL
        AND n.scheduledAt BETWEEN :startTime AND :endTime
        ORDER BY n.scheduledAt ASC
    """)
    fun findScheduledNotificationsBetween(
        @Param("status") status: NotificationStatus,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime
    ): List<Notification>
}