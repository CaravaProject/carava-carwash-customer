package com.carava.carwash.notification.service

import com.carava.carwash.notification.entity.FcmToken
import com.carava.carwash.notification.repository.FcmTokenRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("FcmTokenService 테스트")
class FcmTokenServiceTest {

    private val fcmTokenRepository = mockk<FcmTokenRepository>()
    private val fcmTokenService = FcmTokenService(fcmTokenRepository)

    private val userId = 1L
    private val fcmToken = "test-fcm-token"
    private val deviceId = "device-123"
    private val deviceType = "android"
    private val appVersion = "1.0.0"

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 새로운 토큰")
    fun registerToken_NewToken_Success() {
        // Given
        every { fcmTokenRepository.findByUserIdAndToken(userId, fcmToken) } returns null
        every { fcmTokenRepository.save(any()) } returns mockk<FcmToken>()

        // When
        val result = fcmTokenService.registerToken(
            userId = userId,
            token = fcmToken,
            deviceId = deviceId,
            deviceType = deviceType,
            appVersion = appVersion
        )

        // Then
        assertTrue(result.success)
        assertEquals("토큰이 성공적으로 등록되었습니다", result.message)
        verify { fcmTokenRepository.save(any()) }
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 기존 토큰 업데이트")
    fun registerToken_ExistingToken_Success() {
        // Given
        val existingToken = FcmToken.create(userId, fcmToken)
        existingToken.isActive = false

        every { fcmTokenRepository.findByUserIdAndToken(userId, fcmToken) } returns existingToken
        every { fcmTokenRepository.save(any()) } returns existingToken

        // When
        val result = fcmTokenService.registerToken(
            userId = userId,
            token = fcmToken,
            deviceId = deviceId,
            deviceType = deviceType,
            appVersion = appVersion
        )

        // Then
        assertTrue(result.success)
        assertEquals("토큰이 성공적으로 업데이트되었습니다", result.message)
        assertTrue(existingToken.isActive)
        verify { fcmTokenRepository.save(existingToken) }
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 예외 발생")
    fun registerToken_Exception_Failure() {
        // Given
        every { fcmTokenRepository.findByUserIdAndToken(userId, fcmToken) } throws RuntimeException("Database error")

        // When
        val result = fcmTokenService.registerToken(
            userId = userId,
            token = fcmToken
        )

        // Then
        assertFalse(result.success)
        assertEquals("TOKEN_REGISTRATION_FAILED", result.errorCode)
        assertEquals("토큰 등록 중 오류가 발생했습니다", result.message)
    }

    @Test
    @DisplayName("FCM 토큰 삭제 - 성공")
    fun unregisterToken_Success() {
        // Given
        val existingToken = FcmToken.create(userId, fcmToken)
        every { fcmTokenRepository.findByUserIdAndToken(userId, fcmToken) } returns existingToken
        every { fcmTokenRepository.save(any()) } returns existingToken

        // When
        val result = fcmTokenService.unregisterToken(userId, fcmToken)

        // Then
        assertTrue(result.success)
        assertEquals("토큰이 성공적으로 삭제되었습니다", result.message)
        assertFalse(existingToken.isActive)
        verify { fcmTokenRepository.save(existingToken) }
    }

    @Test
    @DisplayName("FCM 토큰 삭제 - 토큰을 찾을 수 없음")
    fun unregisterToken_TokenNotFound() {
        // Given
        every { fcmTokenRepository.findByUserIdAndToken(userId, fcmToken) } returns null

        // When
        val result = fcmTokenService.unregisterToken(userId, fcmToken)

        // Then
        assertFalse(result.success)
        assertEquals("TOKEN_NOT_FOUND", result.errorCode)
        assertEquals("삭제할 토큰을 찾을 수 없습니다", result.message)
    }

    @Test
    @DisplayName("활성화된 토큰 목록 조회")
    fun getActiveTokens_Success() {
        // Given
        val activeTokens = listOf(
            FcmToken.create(userId, "token1"),
            FcmToken.create(userId, "token2")
        )
        every { fcmTokenRepository.findByUserIdAndIsActiveTrue(userId) } returns activeTokens

        // When
        val result = fcmTokenService.getActiveTokens(userId)

        // Then
        assertEquals(2, result.size)
        verify { fcmTokenRepository.findByUserIdAndIsActiveTrue(userId) }
    }

    @Test
    @DisplayName("모든 토큰 비활성화 - 성공")
    fun deactivateAllTokens_Success() {
        // Given
        every { fcmTokenRepository.deactivateAllByUserId(userId) } just Runs

        // When
        val result = fcmTokenService.deactivateAllTokens(userId)

        // Then
        assertTrue(result.success)
        assertEquals("모든 토큰이 비활성화되었습니다", result.message)
        verify { fcmTokenRepository.deactivateAllByUserId(userId) }
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰")
    fun validateToken_ValidToken() {
        // Given
        val validToken = FcmToken.create(userId, fcmToken)
        every { fcmTokenRepository.findByToken(fcmToken) } returns validToken

        // When
        val result = fcmTokenService.validateToken(fcmToken)

        // Then
        assertTrue(result)
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 무효한 토큰")
    fun validateToken_InvalidToken() {
        // Given
        every { fcmTokenRepository.findByToken(fcmToken) } returns null

        // When
        val result = fcmTokenService.validateToken(fcmToken)

        // Then
        assertFalse(result)
    }

    @Test
    @DisplayName("만료된 토큰 정리")
    fun cleanupExpiredTokens_Success() {
        // Given
        val beforeCount = 100L
        val afterCount = 80L
        
        every { fcmTokenRepository.count() } returnsMany listOf(beforeCount, afterCount)
        every { fcmTokenRepository.deactivateExpiredTokens(any()) } just Runs
        every { fcmTokenRepository.deleteInactiveTokensOlderThan(any()) } just Runs

        // When
        val result = fcmTokenService.cleanupExpiredTokens()

        // Then
        assertEquals(20, result)
        verify { fcmTokenRepository.deactivateExpiredTokens(any()) }
        verify { fcmTokenRepository.deleteInactiveTokensOlderThan(any()) }
    }
}