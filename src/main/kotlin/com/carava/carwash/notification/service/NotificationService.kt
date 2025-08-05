package com.carava.carwash.notification.service

import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.notification.dto.*
import com.carava.carwash.notification.entity.*
import com.carava.carwash.notification.repository.NotificationRepository
import com.carava.carwash.notification.repository.NotificationSettingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationSettingRepository: NotificationSettingRepository,
    private val notificationSettingService: NotificationSettingService,
    private val pushNotificationService: PushNotificationService,
    private val smsNotificationService: SmsNotificationService
) {

    /**
     * 즉시 알림 생성 및 발송
     */
    @Transactional
    fun createAndSendNotification(
        recipientId: Long,
        recipientType: RecipientType,
        notificationType: NotificationType,
        title: String,
        message: String,
        dataPayload: String? = null,
        relatedEntityType: String? = null,
        relatedEntityId: Long? = null
    ): ApiResponse<List<NotificationResponseDto>> {
        return try {
            // 1. 사용자 알림 설정 확인
            val settings = notificationSettingService.getUserSettings(recipientId, recipientType)
            val typeSettings = settings.find { it.notificationType == notificationType }
            
            if (typeSettings?.enabled != true) {
                return ApiResponse.success(emptyList(), "알림 설정이 비활성화되어 있습니다")
            }
            
            // 2. 활성화된 채널 확인
            val enabledChannels = getEnabledChannels(typeSettings)
            if (enabledChannels.isEmpty()) {
                return ApiResponse.success(emptyList(), "활성화된 알림 채널이 없습니다")
            }
            
            val notifications = mutableListOf<Notification>()
            
            // 3. 채널별 알림 생성 및 발송
            for (channel in enabledChannels) {
                val notification = Notification.createImmediate(
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
                
                val savedNotification = notificationRepository.save(notification)
                
                // 4. 채널별 발송 처리
                when (channel) {
                    NotificationChannel.PUSH -> {
                        pushNotificationService.sendPushNotification(savedNotification)
                    }
                    NotificationChannel.SMS -> {
                        smsNotificationService.sendSmsNotification(savedNotification)
                    }
                    NotificationChannel.EMAIL -> {
                        // 이메일 발송 (향후 구현)
                    }
                }
                
                notifications.add(savedNotification)
            }
            
            val responseDtos = notifications.map { NotificationResponseDto.from(it) }
            ApiResponse.success(responseDtos, "알림이 성공적으로 발송되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("NOTIFICATION_SEND_FAILED", "알림 발송 중 오류가 발생했습니다: ${e.message}")
        }
    }

    /**
     * 예약 발송 알림 생성
     */
    @Transactional
    fun scheduleNotification(
        recipientId: Long,
        recipientType: RecipientType,
        notificationType: NotificationType,
        title: String,
        message: String,
        scheduledAt: LocalDateTime,
        dataPayload: String? = null,
        relatedEntityType: String? = null,
        relatedEntityId: Long? = null
    ): ApiResponse<List<NotificationResponseDto>> {
        return try {
            // 1. 사용자 알림 설정 확인
            val settings = notificationSettingService.getUserSettings(recipientId, recipientType)
            val typeSettings = settings.find { it.notificationType == notificationType }
            
            if (typeSettings?.enabled != true) {
                return ApiResponse.success(emptyList(), "알림 설정이 비활성화되어 있습니다")
            }
            
            // 2. 활성화된 채널 확인
            val enabledChannels = getEnabledChannels(typeSettings)
            if (enabledChannels.isEmpty()) {
                return ApiResponse.success(emptyList(), "활성화된 알림 채널이 없습니다")
            }
            
            val notifications = mutableListOf<Notification>()
            
            // 3. 채널별 예약 알림 생성
            for (channel in enabledChannels) {
                val notification = Notification.createScheduled(
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
                
                val savedNotification = notificationRepository.save(notification)
                notifications.add(savedNotification)
            }
            
            val responseDtos = notifications.map { NotificationResponseDto.from(it) }
            ApiResponse.success(responseDtos, "예약 알림이 성공적으로 등록되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("NOTIFICATION_SCHEDULE_FAILED", "예약 알림 등록 중 오류가 발생했습니다: ${e.message}")
        }
    }

    /**
     * 사용자 알림 목록 조회
     */
    fun getUserNotifications(
        recipientId: Long,
        recipientType: RecipientType,
        page: Int = 0,
        size: Int = 20
    ): ApiResponse<Page<NotificationListResponseDto>> {
        return try {
            val pageable: Pageable = PageRequest.of(page, size)
            val notifications = notificationRepository.findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
                recipientId, recipientType, pageable
            )
            
            val responsePage = notifications.map { NotificationListResponseDto.from(it) }
            ApiResponse.success(responsePage, "알림 목록 조회가 완료되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("NOTIFICATION_LIST_FAILED", "알림 목록 조회 중 오류가 발생했습니다")
        }
    }

    /**
     * 알림 상세 조회
     */
    fun getNotificationDetail(
        notificationId: Long,
        recipientId: Long,
        recipientType: RecipientType
    ): ApiResponse<NotificationResponseDto> {
        return try {
            val notification = notificationRepository.findById(notificationId)
                .orElse(null) ?: return ApiResponse.error("NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다")
            
            // 소유권 검증
            if (notification.recipientId != recipientId || notification.recipientType != recipientType) {
                return ApiResponse.error("ACCESS_DENIED", "해당 알림에 접근할 권한이 없습니다")
            }
            
            val responseDto = NotificationResponseDto.from(notification)
            ApiResponse.success(responseDto, "알림 상세 조회가 완료되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("NOTIFICATION_DETAIL_FAILED", "알림 상세 조회 중 오류가 발생했습니다")
        }
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    fun markAsRead(
        notificationId: Long,
        recipientId: Long,
        recipientType: RecipientType
    ): ApiResponse<NotificationResponseDto> {
        return try {
            val notification = notificationRepository.findById(notificationId)
                .orElse(null) ?: return ApiResponse.error("NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다")
            
            // 소유권 검증
            if (notification.recipientId != recipientId || notification.recipientType != recipientType) {
                return ApiResponse.error("ACCESS_DENIED", "해당 알림에 접근할 권한이 없습니다")
            }
            
            // 이미 읽음 상태인 경우
            if (notification.status == NotificationStatus.READ) {
                val responseDto = NotificationResponseDto.from(notification)
                return ApiResponse.success(responseDto, "이미 읽음 처리된 알림입니다")
            }
            
            notification.markAsRead()
            val savedNotification = notificationRepository.save(notification)
            
            val responseDto = NotificationResponseDto.from(savedNotification)
            ApiResponse.success(responseDto, "알림이 읽음 처리되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("NOTIFICATION_READ_FAILED", "알림 읽음 처리 중 오류가 발생했습니다")
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    fun markAllAsRead(
        recipientId: Long,
        recipientType: RecipientType
    ): ApiResponse<Nothing> {
        return try {
            val updatedCount = notificationRepository.markAllAsReadByRecipient(
                recipientId = recipientId,
                recipientType = recipientType,
                readStatus = NotificationStatus.READ,
                readAt = LocalDateTime.now(),
                unreadStatuses = listOf(NotificationStatus.SENT, NotificationStatus.DELIVERED)
            )
            
            ApiResponse.success(null, "${updatedCount}개의 알림이 읽음 처리되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("NOTIFICATION_READ_ALL_FAILED", "전체 알림 읽음 처리 중 오류가 발생했습니다")
        }
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    fun getUnreadNotificationCount(
        recipientId: Long,
        recipientType: RecipientType
    ): ApiResponse<UnreadNotificationCountDto> {
        return try {
            val unreadStatuses = listOf(NotificationStatus.SENT, NotificationStatus.DELIVERED)
            val totalCount = notificationRepository.countByRecipientIdAndRecipientTypeAndStatusIn(
                recipientId, recipientType, unreadStatuses
            )
            
            val countDto = UnreadNotificationCountDto.create(totalCount)
            ApiResponse.success(countDto, "읽지 않은 알림 개수 조회가 완료되었습니다")
            
        } catch (e: Exception) {
            ApiResponse.error("UNREAD_COUNT_FAILED", "읽지 않은 알림 개수 조회 중 오류가 발생했습니다")
        }
    }

    /**
     * 예약 발송 알림 처리 (스케줄러용)
     */
    @Transactional
    fun processScheduledNotifications(): Int {
        val now = LocalDateTime.now()
        val scheduledNotifications = notificationRepository.findPendingNotifications(
            NotificationStatus.PENDING, now
        )
        
        var processedCount = 0
        
        for (notification in scheduledNotifications) {
            try {
                when (notification.channel) {
                    NotificationChannel.PUSH -> {
                        pushNotificationService.sendPushNotification(notification)
                    }
                    NotificationChannel.SMS -> {
                        smsNotificationService.sendSmsNotification(notification)
                    }
                    NotificationChannel.EMAIL -> {
                        // 이메일 발송 (향후 구현)
                    }
                }
                processedCount++
            } catch (e: Exception) {
                notification.markAsFailed("스케줄 발송 실패: ${e.message}")
                notificationRepository.save(notification)
            }
        }
        
        return processedCount
    }

    private fun getEnabledChannels(setting: NotificationSetting): List<NotificationChannel> {
        val channels = mutableListOf<NotificationChannel>()
        
        if (setting.pushEnabled) {
            channels.add(NotificationChannel.PUSH)
        }
        if (setting.smsEnabled) {
            channels.add(NotificationChannel.SMS)
        }
        if (setting.emailEnabled) {
            channels.add(NotificationChannel.EMAIL)
        }
        
        return channels
    }
}