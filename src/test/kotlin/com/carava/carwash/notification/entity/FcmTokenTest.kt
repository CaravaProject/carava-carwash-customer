package com.carava.carwash.notification.entity

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("FcmToken 엔티티 테스트")
class FcmTokenTest {

    @Test
    @DisplayName("FCM 토큰 생성 - 성공")
    fun createFcmToken_Success() {
        // Given
        val userId = 1L
        val token = "test-fcm-token"
        val deviceId = "device-123"
        val deviceType = "android"
        val appVersion = "1.0.0"

        // When
        val fcmToken = FcmToken.create(
            userId = userId,
            token = token,
            deviceId = deviceId,
            deviceType = deviceType,
            appVersion = appVersion
        )

        // Then
        assertEquals(userId, fcmToken.userId)
        assertEquals(token, fcmToken.token)
        assertEquals(deviceId, fcmToken.deviceId)
        assertEquals(deviceType, fcmToken.deviceType)
        assertEquals(appVersion, fcmToken.appVersion)
        assertTrue(fcmToken.isActive)
        assertNotNull(fcmToken.lastUsedAt)
    }

    @Test
    @DisplayName("FCM 토큰 비활성화")
    fun deactivateToken() {
        // Given
        val fcmToken = FcmToken.create(
            userId = 1L,
            token = "test-token"
        )

        // When
        fcmToken.deactivate()

        // Then
        assertFalse(fcmToken.isActive)
    }

    @Test
    @DisplayName("마지막 사용 시간 업데이트")
    fun updateLastUsed() {
        // Given
        val fcmToken = FcmToken.create(
            userId = 1L,
            token = "test-token"
        )
        val originalLastUsed = fcmToken.lastUsedAt

        // When
        Thread.sleep(10) // 시간 차이를 위한 대기
        fcmToken.updateLastUsed()

        // Then
        assertNotNull(fcmToken.lastUsedAt)
        assertTrue(fcmToken.lastUsedAt!! > originalLastUsed!!)
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 활성화된 토큰")
    fun isValid_ActiveToken() {
        // Given
        val fcmToken = FcmToken.create(
            userId = 1L,
            token = "test-token"
        )

        // When & Then
        assertTrue(fcmToken.isValid())
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 비활성화된 토큰")
    fun isValid_InactiveToken() {
        // Given
        val fcmToken = FcmToken.create(
            userId = 1L,
            token = "test-token"
        )
        fcmToken.deactivate()

        // When & Then
        assertFalse(fcmToken.isValid())
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 만료된 토큰")
    fun isValid_ExpiredToken() {
        // Given
        val expiredTime = LocalDateTime.now().minusHours(1)
        val fcmToken = FcmToken.create(
            userId = 1L,
            token = "test-token",
            expiresAt = expiredTime
        )

        // When & Then
        assertFalse(fcmToken.isValid())
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 만료되지 않은 토큰")
    fun isValid_NotExpiredToken() {
        // Given
        val futureTime = LocalDateTime.now().plusHours(1)
        val fcmToken = FcmToken.create(
            userId = 1L,
            token = "test-token",
            expiresAt = futureTime
        )

        // When & Then
        assertTrue(fcmToken.isValid())
    }
}