package com.carava.carwash.notification.service

import com.carava.carwash.notification.entity.*
import com.carava.carwash.notification.repository.NotificationRepository
import com.carava.carwash.notification.repository.NotificationSettingRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationServiceTest {

    private val notificationRepository = mockk<NotificationRepository>()
    private val notificationSettingRepository = mockk<NotificationSettingRepository>()
    private val notificationSettingService = mockk<NotificationSettingService>()
    private val pushNotificationService = mockk<PushNotificationService>()
    private val smsNotificationService = mockk<SmsNotificationService>()
    
    private val notificationService = NotificationService(
        notificationRepository,
        notificationSettingRepository,
        notificationSettingService,
        pushNotificationService,
        smsNotificationService
    )

    private val recipientId = 1L
    private val recipientType = RecipientType.CUSTOMER
    private val notificationType = NotificationType.RESERVATION_CONFIRMED

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    @DisplayName("즉시 알림 생성 및 발송 - 성공")
    fun createAndSendNotification_Success() {
        // Given
        val title = "예약 확정"
        val message = "예약이 확정되었습니다"
        
        val setting = NotificationSetting.createDefault(recipientId, recipientType, notificationType)
        setting.pushEnabled = true
        setting.smsEnabled = false
        
        val notification = Notification.createImmediate(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = notificationType,
            title = title,
            message = message,
            channel = NotificationChannel.PUSH
        )

        every { notificationSettingService.getUserSettings(recipientId, recipientType) } returns listOf(setting)
        every { notificationRepository.save(any()) } returns notification
        every { pushNotificationService.sendPushNotification(any()) } returns true

        // When
        val result = notificationService.createAndSendNotification(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = notificationType,
            title = title,
            message = message
        )

        // Then
        assertTrue(result.success)
        assertEquals("알림이 성공적으로 발송되었습니다", result.message)
        assertEquals(1, result.data?.size)

        verify(exactly = 1) { notificationRepository.save(any()) }
        verify(exactly = 1) { pushNotificationService.sendPushNotification(any()) }
        verify(exactly = 0) { smsNotificationService.sendSmsNotification(any()) }
    }

    @Test
    @DisplayName("즉시 알림 생성 및 발송 - 설정 비활성화")
    fun createAndSendNotification_SettingDisabled() {
        // Given
        val title = "예약 확정"
        val message = "예약이 확정되었습니다"
        
        val setting = NotificationSetting.createDefault(recipientId, recipientType, notificationType)
        setting.enabled = false

        every { notificationSettingService.getUserSettings(recipientId, recipientType) } returns listOf(setting)

        // When
        val result = notificationService.createAndSendNotification(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = notificationType,
            title = title,
            message = message
        )

        // Then
        assertTrue(result.success)
        assertEquals("알림 설정이 비활성화되어 있습니다", result.message)
        assertEquals(0, result.data?.size)

        verify(exactly = 0) { notificationRepository.save(any()) }
        verify(exactly = 0) { pushNotificationService.sendPushNotification(any()) }
    }

    @Test
    @DisplayName("즉시 알림 생성 및 발송 - 모든 채널 비활성화")
    fun createAndSendNotification_AllChannelsDisabled() {
        // Given
        val title = "예약 확정"
        val message = "예약이 확정되었습니다"
        
        val setting = NotificationSetting.createDefault(recipientId, recipientType, notificationType)
        setting.pushEnabled = false
        setting.smsEnabled = false
        setting.emailEnabled = false

        every { notificationSettingService.getUserSettings(recipientId, recipientType) } returns listOf(setting)

        // When
        val result = notificationService.createAndSendNotification(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = notificationType,
            title = title,
            message = message
        )

        // Then
        assertTrue(result.success)
        assertEquals("활성화된 알림 채널이 없습니다", result.message)
        assertEquals(0, result.data?.size)

        verify(exactly = 0) { notificationRepository.save(any()) }
    }

    @Test
    @DisplayName("예약 발송 알림 생성 - 성공")
    fun scheduleNotification_Success() {
        // Given
        val title = "예약 알림"
        val message = "예약 시간 2시간 전입니다"
        val scheduledAt = LocalDateTime.now().plusHours(2)
        
        val setting = NotificationSetting.createDefault(recipientId, recipientType, NotificationType.RESERVATION_REMINDER_2H)
        setting.pushEnabled = true
        
        val notification = Notification.createScheduled(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = NotificationType.RESERVATION_REMINDER_2H,
            title = title,
            message = message,
            channel = NotificationChannel.PUSH,
            scheduledAt = scheduledAt
        )

        every { notificationSettingService.getUserSettings(recipientId, recipientType) } returns listOf(setting)
        every { notificationRepository.save(any()) } returns notification

        // When
        val result = notificationService.scheduleNotification(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = NotificationType.RESERVATION_REMINDER_2H,
            title = title,
            message = message,
            scheduledAt = scheduledAt
        )

        // Then
        assertTrue(result.success)
        assertEquals("예약 알림이 성공적으로 등록되었습니다", result.message)
        assertEquals(1, result.data?.size)

        verify(exactly = 1) { notificationRepository.save(any()) }
        verify(exactly = 0) { pushNotificationService.sendPushNotification(any()) } // 예약 발송이므로 즉시 발송하지 않음
    }

    @Test
    @DisplayName("사용자 알림 목록 조회 - 성공")
    fun getUserNotifications_Success() {
        // Given
        val notification1 = Notification.createImmediate(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = notificationType,
            title = "알림 1",
            message = "메시지 1",
            channel = NotificationChannel.PUSH
        )
        
        val notification2 = Notification.createImmediate(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = NotificationType.RESERVATION_STARTED,
            title = "알림 2",
            message = "메시지 2",
            channel = NotificationChannel.PUSH
        )

        val notifications = listOf(notification1, notification2)
        val page = PageImpl(notifications, PageRequest.of(0, 20), 2)

        every { 
            notificationRepository.findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(
                recipientId, recipientType, any()
            ) 
        } returns page

        // When
        val result = notificationService.getUserNotifications(recipientId, recipientType)

        // Then
        assertTrue(result.success)
        assertEquals("알림 목록 조회가 완료되었습니다", result.message)
        assertEquals(2, result.data?.content?.size)
        assertEquals("알림 1", result.data?.content?.get(0)?.title)
        assertEquals("알림 2", result.data?.content?.get(1)?.title)
    }

    @Test
    @DisplayName("알림 상세 조회 - 성공")
    fun getNotificationDetail_Success() {
        // Given
        val notificationId = 1L
        val notification = Notification.createImmediate(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = notificationType,
            title = "알림 제목",
            message = "알림 메시지",
            channel = NotificationChannel.PUSH
        )

        every { notificationRepository.findById(notificationId) } returns Optional.of(notification)

        // When
        val result = notificationService.getNotificationDetail(notificationId, recipientId, recipientType)

        // Then
        assertTrue(result.success)
        assertEquals("알림 상세 조회가 완료되었습니다", result.message)
        assertEquals("알림 제목", result.data?.title)
        assertEquals("알림 메시지", result.data?.message)
    }

    @Test
    @DisplayName("알림 상세 조회 - 알림 없음")
    fun getNotificationDetail_NotFound() {
        // Given
        val notificationId = 999L

        every { notificationRepository.findById(notificationId) } returns Optional.empty()

        // When
        val result = notificationService.getNotificationDetail(notificationId, recipientId, recipientType)

        // Then
        assertFalse(result.success)
        assertEquals("NOTIFICATION_NOT_FOUND", result.errorCode)
        assertEquals("알림을 찾을 수 없습니다", result.message)
    }

    @Test
    @DisplayName("알림 상세 조회 - 접근 권한 없음")
    fun getNotificationDetail_AccessDenied() {
        // Given
        val notificationId = 1L
        val notification = Notification.createImmediate(
            recipientId = 999L, // 다른 사용자
            recipientType = recipientType,
            notificationType = notificationType,
            title = "알림 제목",
            message = "알림 메시지",
            channel = NotificationChannel.PUSH
        )

        every { notificationRepository.findById(notificationId) } returns Optional.of(notification)

        // When
        val result = notificationService.getNotificationDetail(notificationId, recipientId, recipientType)

        // Then
        assertFalse(result.success)
        assertEquals("ACCESS_DENIED", result.errorCode)
        assertEquals("해당 알림에 접근할 권한이 없습니다", result.message)
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    fun markAsRead_Success() {
        // Given
        val notificationId = 1L
        val notification = Notification.createImmediate(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = notificationType,
            title = "알림 제목",
            message = "알림 메시지",
            channel = NotificationChannel.PUSH
        )
        notification.status = NotificationStatus.DELIVERED

        every { notificationRepository.findById(notificationId) } returns Optional.of(notification)
        every { notificationRepository.save(any()) } returns notification

        // When
        val result = notificationService.markAsRead(notificationId, recipientId, recipientType)

        // Then
        assertTrue(result.success)
        assertEquals("알림이 읽음 처리되었습니다", result.message)
        
        verify { notificationRepository.save(any()) }
    }

    @Test
    @DisplayName("알림 읽음 처리 - 이미 읽음 상태")
    fun markAsRead_AlreadyRead() {
        // Given
        val notificationId = 1L
        val notification = Notification.createImmediate(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = notificationType,
            title = "알림 제목",
            message = "알림 메시지",
            channel = NotificationChannel.PUSH
        )
        notification.status = NotificationStatus.READ

        every { notificationRepository.findById(notificationId) } returns Optional.of(notification)

        // When
        val result = notificationService.markAsRead(notificationId, recipientId, recipientType)

        // Then
        assertTrue(result.success)
        assertEquals("이미 읽음 처리된 알림입니다", result.message)
        
        verify(exactly = 0) { notificationRepository.save(any()) }
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 - 성공")
    fun markAllAsRead_Success() {
        // Given
        val updatedCount = 5

        every { 
            notificationRepository.markAllAsReadByRecipient(
                recipientId = recipientId,
                recipientType = recipientType,
                readStatus = NotificationStatus.READ,
                readAt = any(),
                unreadStatuses = listOf(NotificationStatus.SENT, NotificationStatus.DELIVERED)
            ) 
        } returns updatedCount

        // When
        val result = notificationService.markAllAsRead(recipientId, recipientType)

        // Then
        assertTrue(result.success)
        assertEquals("${updatedCount}개의 알림이 읽음 처리되었습니다", result.message)
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 - 성공")
    fun getUnreadNotificationCount_Success() {
        // Given
        val unreadCount = 3L

        every { 
            notificationRepository.countByRecipientIdAndRecipientTypeAndStatusIn(
                recipientId, recipientType, listOf(NotificationStatus.SENT, NotificationStatus.DELIVERED)
            ) 
        } returns unreadCount

        // When
        val result = notificationService.getUnreadNotificationCount(recipientId, recipientType)

        // Then
        assertTrue(result.success)
        assertEquals("읽지 않은 알림 개수 조회가 완료되었습니다", result.message)
        assertEquals(unreadCount, result.data?.totalCount)
    }

    @Test
    @DisplayName("예약 발송 알림 처리 - 성공")
    fun processScheduledNotifications_Success() {
        // Given
        val notification1 = Notification.createScheduled(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = NotificationType.RESERVATION_REMINDER_2H,
            title = "알림 1",
            message = "메시지 1",
            channel = NotificationChannel.PUSH,
            scheduledAt = LocalDateTime.now().minusMinutes(10)
        )
        
        val notification2 = Notification.createScheduled(
            recipientId = recipientId,
            recipientType = recipientType,
            notificationType = NotificationType.RESERVATION_REMINDER_30M,
            title = "알림 2",
            message = "메시지 2",
            channel = NotificationChannel.SMS,
            scheduledAt = LocalDateTime.now().minusMinutes(5)
        )

        every { notificationRepository.findPendingNotifications(NotificationStatus.PENDING, any()) } returns listOf(notification1, notification2)
        every { pushNotificationService.sendPushNotification(notification1) } returns true
        every { smsNotificationService.sendSmsNotification(notification2) } returns true

        // When
        val processedCount = notificationService.processScheduledNotifications()

        // Then
        assertEquals(2, processedCount)
        
        verify(exactly = 1) { pushNotificationService.sendPushNotification(notification1) }
        verify(exactly = 1) { smsNotificationService.sendSmsNotification(notification2) }
    }
}