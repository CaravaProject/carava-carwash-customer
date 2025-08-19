package com.carava.carwash.notification.repository

import com.carava.carwash.notification.entity.FcmToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * FCM 토큰 Repository
 */
@Repository
interface FcmTokenRepository : JpaRepository<FcmToken, Long> {

    /**
     * 사용자의 활성화된 FCM 토큰 목록 조회
     */
    fun findByUserIdAndIsActiveTrue(userId: Long): List<FcmToken>

    /**
     * 특정 토큰이 존재하는지 확인
     */
    fun existsByToken(token: String): Boolean

    /**
     * 사용자와 토큰으로 FCM 토큰 조회
     */
    fun findByUserIdAndToken(userId: Long, token: String): FcmToken?

    /**
     * 특정 토큰 조회
     */
    fun findByToken(token: String): FcmToken?

    /**
     * 사용자의 모든 토큰을 비활성화
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.userId = :userId")
    fun deactivateAllByUserId(@Param("userId") userId: Long)

    /**
     * 특정 토큰을 비활성화
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.token = :token")
    fun deactivateByToken(@Param("token") token: String)

    /**
     * 만료된 토큰들을 비활성화
     */
    @Modifying
    @Query("UPDATE FcmToken f SET f.isActive = false WHERE f.expiresAt < :now AND f.isActive = true")
    fun deactivateExpiredTokens(@Param("now") now: LocalDateTime)

    /**
     * 오래된 비활성화 토큰들 삭제 (정리용)
     */
    @Modifying
    @Query("DELETE FROM FcmToken f WHERE f.isActive = false AND f.updatedAt < :cutoffDate")
    fun deleteInactiveTokensOlderThan(@Param("cutoffDate") cutoffDate: LocalDateTime)

    /**
     * 사용자별 활성화된 토큰 개수 조회
     */
    fun countByUserIdAndIsActiveTrue(userId: Long): Long

    /**
     * 디바이스 ID로 토큰 조회
     */
    fun findByDeviceIdAndIsActiveTrue(deviceId: String): List<FcmToken>
}