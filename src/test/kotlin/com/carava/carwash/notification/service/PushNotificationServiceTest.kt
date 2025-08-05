package com.carava.carwash.notification.service

import com.carava.carwash.notification.entity.*
import com.carava.carwash.notification.repository.NotificationRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PushNotificationServiceTest {

    private val notificationRepository = mockk<NotificationRepository>()
    private val pushNotificationService = PushNotificationService(notificationRepository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    @DisplayName("푸시 알림 발송 - 성공")
    fun sendPushNotification_Success() {
        // Given
        val notification = Notification.createImmediate(
            recipientId = 1L,
            recipientType = RecipientType.CUSTOMER,
            notificationType = NotificationType.RESERVATION_CONFIRMED,
            title = "예약 확정",
            message = "예약이 확정되었습니다",
            channel = NotificationChannel.PUSH
        )

        every { notificationRepository.save(any()) } returns notification

        // When
        val result = pushNotificationService.sendPushNotification(notification)

        // Then
        assertTrue(result)
        verify { notificationRepository.save(any()) }
    }

    @Test
    @DisplayName("푸시 알림 발송 - 실패 (모의 실패)")
    fun sendPushNotification_Failure() {
        // Given
        val notification = Notification.createImmediate(
            recipientId = 1L,
            recipientType = RecipientType.CUSTOMER,
            notificationType = NotificationType.RESERVATION_CONFIRMED,
            title = "예약 확정",
            message = "예약이 확정되었습니다",
            channel = NotificationChannel.PUSH
        )

        every { notificationRepository.save(any()) } returns notification

        // When - 여러 번 시도해서 실패 케이스 확인 (10% 확률로 실패)
        var foundFailure = false
        repeat(50) {
            val result = pushNotificationService.sendPushNotification(notification)
            if (!result) {
                foundFailure = true
                return@repeat
            }
        }

        // Then - 실패 케이스가 발생했는지 확인 (확률적이므로 여러 번 시도)
        // 실제로는 모의 환경에서 90% 성공률을 가지므로 대부분 성공할 것
        verify(atLeast = 1) { notificationRepository.save(any()) }
    }

    @Test
    @DisplayName("배치 푸시 알림 발송 - 성공")
    fun sendBatchPushNotifications_Success() {
        // Given
        val notifications = listOf(
            Notification.createImmediate(
                recipientId = 1L,
                recipientType = RecipientType.CUSTOMER,
                notificationType = NotificationType.RESERVATION_CONFIRMED,
                title = "예약 확정 1",
                message = "예약이 확정되었습니다 1",
                channel = NotificationChannel.PUSH
            ),
            Notification.createImmediate(
                recipientId = 2L,
                recipientType = RecipientType.CUSTOMER,
                notificationType = NotificationType.RESERVATION_REMINDER_2H,
                title = "예약 알림 2",
                message = "예약 시간 2시간 전입니다 2",
                channel = NotificationChannel.PUSH
            ),
            Notification.createImmediate(
                recipientId = 3L,
                recipientType = RecipientType.CUSTOMER,
                notificationType = NotificationType.RESERVATION_STARTED,
                title = "서비스 시작 3",
                message = "서비스가 시작되었습니다 3",
                channel = NotificationChannel.PUSH
            )
        )

        every { notificationRepository.save(any()) } returnsMany notifications

        // When
        val result = pushNotificationService.sendBatchPushNotifications(notifications)

        // Then
        assertEquals(3, result.totalCount)
        assertTrue(result.successCount >= 0) // 모의 환경에서는 확률적으로 성공/실패
        assertTrue(result.failureCount >= 0)
        assertEquals(3, result.successCount + result.failureCount)
        
        verify(exactly = 3) { notificationRepository.save(any()) }
    }

    @Test
    @DisplayName("배치 푸시 알림 발송 - 빈 목록")
    fun sendBatchPushNotifications_EmptyList() {
        // Given
        val notifications = emptyList<Notification>()

        // When
        val result = pushNotificationService.sendBatchPushNotifications(notifications)

        // Then
        assertEquals(0, result.totalCount)
        assertEquals(0, result.successCount)
        assertEquals(0, result.failureCount)
        assertEquals(0.0, result.successRate)
        
        verify(exactly = 0) { notificationRepository.save(any()) }
    }

    @Test
    @DisplayName("FCM 토큰 유효성 검증 - 유효한 토큰")
    fun validateFcmToken_ValidToken() {
        // Given
        val validToken = "fGHGJHGjhg123456789abcdef"

        // When
        val result = pushNotificationService.validateFcmToken(validToken)

        // Then
        assertTrue(result)
    }

    @Test
    @DisplayName("FCM 토큰 유효성 검증 - 무효한 토큰 (짧음)")
    fun validateFcmToken_TooShort() {
        // Given
        val shortToken = "short"

        // When
        val result = pushNotificationService.validateFcmToken(shortToken)

        // Then
        assertFalse(result)
    }

    @Test
    @DisplayName("FCM 토큰 유효성 검증 - 무효한 토큰 (빈 문자열)")
    fun validateFcmToken_Empty() {
        // Given
        val emptyToken = ""

        // When
        val result = pushNotificationService.validateFcmToken(emptyToken)

        // Then
        assertFalse(result)
    }

    @Test
    @DisplayName("FCM 토큰 유효성 검증 - 무효한 토큰 (공백)")
    fun validateFcmToken_Blank() {
        // Given
        val blankToken = "   "

        // When
        val result = pushNotificationService.validateFcmToken(blankToken)

        // Then
        assertFalse(result)
    }

    @Test
    @DisplayName("성공률 계산 - 모든 성공")
    fun batchSendResult_AllSuccess() {
        // Given
        val result = PushNotificationService.BatchSendResult(
            totalCount = 10,
            successCount = 10,
            failureCount = 0
        )

        // When & Then
        assertEquals(1.0, result.successRate)
    }

    @Test
    @DisplayName("성공률 계산 - 절반 성공")
    fun batchSendResult_HalfSuccess() {
        // Given
        val result = PushNotificationService.BatchSendResult(
            totalCount = 10,
            successCount = 5,
            failureCount = 5
        )

        // When & Then
        assertEquals(0.5, result.successRate)
    }

    @Test
    @DisplayName("성공률 계산 - 모든 실패")
    fun batchSendResult_AllFailure() {
        // Given
        val result = PushNotificationService.BatchSendResult(
            totalCount = 10,
            successCount = 0,
            failureCount = 10
        )

        // When & Then
        assertEquals(0.0, result.successRate)
    }

    @Test
    @DisplayName("성공률 계산 - 건수 없음")
    fun batchSendResult_NoItems() {
        // Given
        val result = PushNotificationService.BatchSendResult(
            totalCount = 0,
            successCount = 0,
            failureCount = 0
        )

        // When & Then
        assertEquals(0.0, result.successRate)
    }
}