package com.carava.carwash.notification.service

import com.carava.carwash.notification.entity.FcmToken
import com.carava.carwash.notification.repository.FcmTokenRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("FcmTokenService 통합 테스트")
class FcmTokenServiceIntegrationTest {

    private val fcmTokenRepository = mockk<FcmTokenRepository>()
    private val fcmTokenService = FcmTokenService(fcmTokenRepository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 성공")
    fun registerToken_Success() {
        // Given
        val userId = 1L
        val token = "test-token"
        
        every { fcmTokenRepository.findByUserIdAndToken(userId, token) } returns null
        every { fcmTokenRepository.save(any()) } returns mockk<FcmToken>()

        // When
        val result = fcmTokenService.registerToken(userId, token)

        // Then
        assertTrue(result.success)
        verify { fcmTokenRepository.save(any()) }
    }

    @Test
    @DisplayName("FCM 토큰 삭제 - 성공")
    fun unregisterToken_Success() {
        // Given
        val userId = 1L
        val token = "test-token"
        val existingToken = FcmToken.create(userId, token)
        
        every { fcmTokenRepository.findByUserIdAndToken(userId, token) } returns existingToken
        every { fcmTokenRepository.save(any()) } returns existingToken

        // When
        val result = fcmTokenService.unregisterToken(userId, token)

        // Then
        assertTrue(result.success)
        assertFalse(existingToken.isActive)
    }

    @Test
    @DisplayName("활성화된 토큰 조회")
    fun getActiveTokens_Success() {
        // Given
        val userId = 1L
        val tokens = listOf(FcmToken.create(userId, "token1"))
        
        every { fcmTokenRepository.findByUserIdAndIsActiveTrue(userId) } returns tokens

        // When
        val result = fcmTokenService.getActiveTokens(userId)

        // Then
        assertEquals(1, result.size)
    }
}