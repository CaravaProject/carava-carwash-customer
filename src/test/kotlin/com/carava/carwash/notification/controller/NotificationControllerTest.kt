package com.carava.carwash.notification.controller

import com.carava.carwash.global.config.security.JwtUtil
import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.notification.dto.*
import com.carava.carwash.notification.entity.RecipientType
import com.carava.carwash.notification.service.FcmTokenService
import com.carava.carwash.notification.service.NotificationService
import com.carava.carwash.notification.service.NotificationSettingService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(NotificationController::class)
@ContextConfiguration(classes = [NotificationController::class])
@DisplayName("NotificationController 테스트")
class NotificationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var notificationService: NotificationService

    @MockBean
    private lateinit var notificationSettingService: NotificationSettingService

    @MockBean
    private lateinit var fcmTokenService: FcmTokenService

    @MockBean
    private lateinit var jwtUtil: JwtUtil

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val authToken = "Bearer valid-jwt-token"
    private val userEmail = "test@example.com"

    @Test
    @DisplayName("내 알림 목록 조회 - 성공")
    fun getMyNotifications_Success() {
        // Given
        val mockResponse = ApiResponse.success(
            NotificationListResponseDto(
                notifications = emptyList(),
                totalCount = 0,
                unreadCount = 0
            )
        )

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { notificationService.getMyNotifications(any(), any<Pageable>(), any()) } returns mockResponse

        // When & Then
        mockMvc.perform(
            get("/notifications")
                .header("Authorization", authToken)
                .param("page", "0")
                .param("size", "20")
                .param("unreadOnly", "false")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalCount").value(0))
            .andExpect(jsonPath("$.data.unreadCount").value(0))

        verify { notificationService.getMyNotifications(any(), any<Pageable>(), eq(false)) }
    }

    @Test
    @DisplayName("알림 상세 조회 - 성공")
    fun getNotificationDetail_Success() {
        // Given
        val notificationId = 1L
        val mockResponse = ApiResponse.success(
            NotificationResponseDto(
                id = notificationId,
                notificationType = com.carava.carwash.notification.entity.NotificationType.RESERVATION_CONFIRMED,
                title = "테스트 알림",
                message = "테스트 메시지",
                dataPayload = null,
                channel = com.carava.carwash.notification.entity.NotificationChannel.PUSH,
                status = com.carava.carwash.notification.entity.NotificationStatus.SENT,
                scheduledAt = null,
                sentAt = java.time.LocalDateTime.now(),
                deliveredAt = null,
                readAt = null,
                relatedEntityType = null,
                relatedEntityId = null,
                createdAt = java.time.LocalDateTime.now(),
                isUnread = false
            )
        )

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { notificationService.getNotificationDetail(any(), eq(notificationId)) } returns mockResponse

        // When & Then
        mockMvc.perform(
            get("/notifications/{notificationId}", notificationId)
                .header("Authorization", authToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(notificationId))
            .andExpect(jsonPath("$.data.title").value("테스트 알림"))

        verify { notificationService.getNotificationDetail(any(), eq(notificationId)) }
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    fun markAsRead_Success() {
        // Given
        val notificationId = 1L
        val mockResponse = ApiResponse.success("알림이 읽음 처리되었습니다")

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { notificationService.markAsRead(any(), eq(notificationId)) } returns mockResponse

        // When & Then
        mockMvc.perform(
            put("/notifications/{notificationId}/read", notificationId)
                .header("Authorization", authToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("알림이 읽음 처리되었습니다"))

        verify { notificationService.markAsRead(any(), eq(notificationId)) }
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 - 성공")
    fun markAllAsRead_Success() {
        // Given
        val mockResponse = ApiResponse.success("모든 알림이 읽음 처리되었습니다")

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { notificationService.markAllAsRead(any()) } returns mockResponse

        // When & Then
        mockMvc.perform(
            put("/notifications/read-all")
                .header("Authorization", authToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("모든 알림이 읽음 처리되었습니다"))

        verify { notificationService.markAllAsRead(any()) }
    }

    @Test
    @DisplayName("알림 삭제 - 성공")
    fun deleteNotification_Success() {
        // Given
        val notificationId = 1L
        val mockResponse = ApiResponse.success("알림이 삭제되었습니다")

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { notificationService.deleteNotification(any(), eq(notificationId)) } returns mockResponse

        // When & Then
        mockMvc.perform(
            delete("/notifications/{notificationId}", notificationId)
                .header("Authorization", authToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("알림이 삭제되었습니다"))

        verify { notificationService.deleteNotification(any(), eq(notificationId)) }
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 - 성공")
    fun getUnreadCount_Success() {
        // Given
        val unreadCount = 5L
        val mockResponse = ApiResponse.success(unreadCount)

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { notificationService.getUnreadCount(any()) } returns mockResponse

        // When & Then
        mockMvc.perform(
            get("/notifications/unread-count")
                .header("Authorization", authToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(unreadCount))

        verify { notificationService.getUnreadCount(any()) }
    }

    @Test
    @DisplayName("알림 설정 조회 - 성공")
    fun getNotificationSettings_Success() {
        // Given
        val mockResponse = ApiResponse.success(
            NotificationSettingsResponseDto(
                settings = emptyList(),
                globalPushEnabled = true,
                globalSmsEnabled = false,
                globalEmailEnabled = true
            )
        )

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { notificationSettingService.getUserNotificationSettings(any(), eq(RecipientType.CUSTOMER)) } returns mockResponse

        // When & Then
        mockMvc.perform(
            get("/notifications/settings")
                .header("Authorization", authToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.globalPushEnabled").value(true))

        verify { notificationSettingService.getUserNotificationSettings(any(), eq(RecipientType.CUSTOMER)) }
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 성공")
    fun registerFcmToken_Success() {
        // Given
        val request = FcmTokenRequestDto(
            token = "test-fcm-token",
            deviceId = "device-123",
            deviceType = "android",
            appVersion = "1.0.0"
        )
        val mockResponse = ApiResponse.success("토큰이 성공적으로 등록되었습니다")

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { fcmTokenService.registerToken(any(), any(), any(), any(), any()) } returns mockResponse

        // When & Then
        mockMvc.perform(
            post("/notifications/fcm-token")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("토큰이 성공적으로 등록되었습니다"))

        verify { fcmTokenService.registerToken(any(), any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("FCM 토큰 삭제 - 성공")
    fun unregisterFcmToken_Success() {
        // Given
        val fcmToken = "test-fcm-token"
        val mockResponse = ApiResponse.success("토큰이 성공적으로 삭제되었습니다")

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { fcmTokenService.unregisterToken(any(), eq(fcmToken)) } returns mockResponse

        // When & Then
        mockMvc.perform(
            delete("/notifications/fcm-token")
                .header("Authorization", authToken)
                .param("fcmToken", fcmToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("토큰이 성공적으로 삭제되었습니다"))

        verify { fcmTokenService.unregisterToken(any(), eq(fcmToken)) }
    }

    @Test
    @DisplayName("모든 FCM 토큰 비활성화 - 성공")
    fun deactivateAllFcmTokens_Success() {
        // Given
        val mockResponse = ApiResponse.success("모든 토큰이 비활성화되었습니다")

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail
        every { fcmTokenService.deactivateAllTokens(any()) } returns mockResponse

        // When & Then
        mockMvc.perform(
            post("/notifications/fcm-token/deactivate-all")
                .header("Authorization", authToken)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("모든 토큰이 비활성화되었습니다"))

        verify { fcmTokenService.deactivateAllTokens(any()) }
    }

    @Test
    @DisplayName("인증 헤더 없음 - 실패")
    fun noAuthorizationHeader_Failure() {
        // When & Then
        mockMvc.perform(
            get("/notifications")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("잘못된 JWT 토큰 - 실패")
    fun invalidJwtToken_Failure() {
        // Given
        every { jwtUtil.getEmailFromToken("invalid-token") } throws RuntimeException("Invalid token")

        // When & Then
        mockMvc.perform(
            get("/notifications")
                .header("Authorization", "Bearer invalid-token")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 유효성 검증 실패")
    fun registerFcmToken_ValidationFailure() {
        // Given
        val invalidRequest = FcmTokenRequestDto(
            token = "", // 빈 토큰
            deviceType = "android"
        )

        every { jwtUtil.getEmailFromToken("valid-jwt-token") } returns userEmail

        // When & Then
        mockMvc.perform(
            post("/notifications/fcm-token")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
    }
}