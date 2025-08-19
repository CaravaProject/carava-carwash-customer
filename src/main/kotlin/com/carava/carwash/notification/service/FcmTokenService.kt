package com.carava.carwash.notification.service

import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.notification.entity.FcmToken
import com.carava.carwash.notification.repository.FcmTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * FCM 토큰 관리 서비스
 */
@Service
@Transactional(readOnly = true)
class FcmTokenService(
    private val fcmTokenRepository: FcmTokenRepository
) {
    
    private val logger = LoggerFactory.getLogger(FcmTokenService::class.java)
    
    /**
     * FCM 토큰 등록/업데이트
     */
    @Transactional
    fun registerToken(
        userId: Long,
        token: String,
        deviceId: String? = null,
        deviceType: String? = null,
        appVersion: String? = null
    ): ApiResponse<String> {
        return try {
            logger.info("FCM 토큰 등록 시작 - 사용자 ID: $userId, 디바이스: $deviceType")
            
            // 기존 토큰이 있는지 확인
            val existingToken = fcmTokenRepository.findByUserIdAndToken(userId, token)
            
            if (existingToken != null) {
                // 기존 토큰이 있으면 활성화 및 마지막 사용 시간 업데이트
                if (!existingToken.isActive) {
                    existingToken.isActive = true
                }
                existingToken.updateLastUsed()
                fcmTokenRepository.save(existingToken)
                
                logger.info("기존 FCM 토큰 업데이트 완료 - ID: ${existingToken.id}")
                ApiResponse.success("토큰이 성공적으로 업데이트되었습니다")
            } else {
                // 새로운 토큰 생성
                val newToken = FcmToken.create(
                    userId = userId,
                    token = token,
                    deviceId = deviceId,
                    deviceType = deviceType,
                    appVersion = appVersion
                )
                
                fcmTokenRepository.save(newToken)
                
                logger.info("새로운 FCM 토큰 등록 완료 - ID: ${newToken.id}")
                ApiResponse.success("토큰이 성공적으로 등록되었습니다")
            }
        } catch (e: Exception) {
            logger.error("FCM 토큰 등록 실패 - 사용자 ID: $userId", e)
            ApiResponse.error("TOKEN_REGISTRATION_FAILED", "토큰 등록 중 오류가 발생했습니다")
        }
    }
    
    /**
     * FCM 토큰 삭제/비활성화
     */
    @Transactional
    fun unregisterToken(userId: Long, token: String): ApiResponse<String> {
        return try {
            logger.info("FCM 토큰 삭제 시작 - 사용자 ID: $userId")
            
            val fcmToken = fcmTokenRepository.findByUserIdAndToken(userId, token)
            
            if (fcmToken != null) {
                fcmToken.deactivate()
                fcmTokenRepository.save(fcmToken)
                
                logger.info("FCM 토큰 비활성화 완료 - ID: ${fcmToken.id}")
                ApiResponse.success("토큰이 성공적으로 삭제되었습니다")
            } else {
                logger.warn("삭제할 FCM 토큰을 찾을 수 없음 - 사용자 ID: $userId")
                ApiResponse.error("TOKEN_NOT_FOUND", "삭제할 토큰을 찾을 수 없습니다")
            }
        } catch (e: Exception) {
            logger.error("FCM 토큰 삭제 실패 - 사용자 ID: $userId", e)
            ApiResponse.error("TOKEN_DELETION_FAILED", "토큰 삭제 중 오류가 발생했습니다")
        }
    }
    
    /**
     * 사용자의 활성화된 FCM 토큰 목록 조회
     */
    fun getActiveTokens(userId: Long): List<FcmToken> {
        return fcmTokenRepository.findByUserIdAndIsActiveTrue(userId)
    }
    
    /**
     * 사용자의 모든 토큰 비활성화 (로그아웃 시 사용)
     */
    @Transactional
    fun deactivateAllTokens(userId: Long): ApiResponse<String> {
        return try {
            logger.info("사용자의 모든 FCM 토큰 비활성화 - 사용자 ID: $userId")
            
            fcmTokenRepository.deactivateAllByUserId(userId)
            
            logger.info("모든 FCM 토큰 비활성화 완료 - 사용자 ID: $userId")
            ApiResponse.success("모든 토큰이 비활성화되었습니다")
        } catch (e: Exception) {
            logger.error("모든 토큰 비활성화 실패 - 사용자 ID: $userId", e)
            ApiResponse.error("TOKEN_DEACTIVATION_FAILED", "토큰 비활성화 중 오류가 발생했습니다")
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        val fcmToken = fcmTokenRepository.findByToken(token)
        return fcmToken?.isValid() ?: false
    }
    
    /**
     * 만료된 토큰 정리 (스케줄러에서 호출)
     */
    @Transactional
    fun cleanupExpiredTokens(): Int {
        logger.info("만료된 FCM 토큰 정리 시작")
        
        val now = LocalDateTime.now()
        val beforeCount = fcmTokenRepository.count()
        
        // 만료된 토큰들을 비활성화
        fcmTokenRepository.deactivateExpiredTokens(now)
        
        // 30일 이상 된 비활성화 토큰들 삭제
        val cutoffDate = now.minusDays(30)
        fcmTokenRepository.deleteInactiveTokensOlderThan(cutoffDate)
        
        val afterCount = fcmTokenRepository.count()
        val cleanedCount = (beforeCount - afterCount).toInt()
        
        logger.info("FCM 토큰 정리 완료 - 정리된 토큰 수: $cleanedCount")
        return cleanedCount
    }
}