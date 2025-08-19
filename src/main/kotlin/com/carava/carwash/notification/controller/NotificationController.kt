package com.carava.carwash.notification.controller

import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.global.config.security.JwtUtil
import com.carava.carwash.notification.dto.*
import com.carava.carwash.notification.entity.RecipientType
import com.carava.carwash.notification.service.NotificationService
import com.carava.carwash.notification.service.NotificationSettingService
import com.carava.carwash.notification.service.FcmTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 알림 관리 API Controller
 */
@Tag(name = "알림", description = "알림 관리 API")
@RestController
@RequestMapping("/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val notificationSettingService: NotificationSettingService,
    private val fcmTokenService: FcmTokenService,
    private val jwtUtil: JwtUtil
) {

    /**
     * 내 알림 목록 조회
     */
    @Operation(summary = "내 알림 목록 조회", description = "사용자의 알림 목록을 페이징으로 조회합니다")
    @GetMapping
    fun getMyNotifications(
        @RequestHeader("Authorization") token: String,
        @Parameter(description = "페이징 정보") @PageableDefault(size = 20) pageable: Pageable,
        @Parameter(description = "읽지 않은 알림만 조회") @RequestParam(defaultValue = "false") unreadOnly: Boolean
    ): ResponseEntity<ApiResponse<NotificationListResponseDto>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = notificationService.getMyNotifications(userId, pageable, unreadOnly)
        return ResponseEntity.ok(result)
    }

    /**
     * 알림 상세 조회 (읽음 처리)
     */
    @Operation(summary = "알림 상세 조회", description = "특정 알림을 조회하고 읽음 처리합니다")
    @GetMapping("/{notificationId}")
    fun getNotificationDetail(
        @RequestHeader("Authorization") token: String,
        @Parameter(description = "알림 ID") @PathVariable notificationId: Long
    ): ResponseEntity<ApiResponse<NotificationResponseDto>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = notificationService.getNotificationDetail(userId, notificationId)
        return ResponseEntity.ok(result)
    }

    /**
     * 알림 읽음 처리
     */
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다")
    @PutMapping("/{notificationId}/read")
    fun markAsRead(
        @RequestHeader("Authorization") token: String,
        @Parameter(description = "알림 ID") @PathVariable notificationId: Long
    ): ResponseEntity<ApiResponse<String>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = notificationService.markAsRead(userId, notificationId)
        return ResponseEntity.ok(result)
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 처리합니다")
    @PutMapping("/read-all")
    fun markAllAsRead(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ApiResponse<String>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = notificationService.markAllAsRead(userId)
        return ResponseEntity.ok(result)
    }

    /**
     * 알림 삭제
     */
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다")
    @DeleteMapping("/{notificationId}")
    fun deleteNotification(
        @RequestHeader("Authorization") token: String,
        @Parameter(description = "알림 ID") @PathVariable notificationId: Long
    ): ResponseEntity<ApiResponse<String>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = notificationService.deleteNotification(userId, notificationId)
        return ResponseEntity.ok(result)
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "사용자의 읽지 않은 알림 개수를 조회합니다")
    @GetMapping("/unread-count")
    fun getUnreadCount(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ApiResponse<Long>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = notificationService.getUnreadCount(userId)
        return ResponseEntity.ok(result)
    }

    /**
     * 알림 설정 조회
     */
    @Operation(summary = "알림 설정 조회", description = "사용자의 알림 설정을 조회합니다")
    @GetMapping("/settings")
    fun getNotificationSettings(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ApiResponse<NotificationSettingsResponseDto>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = notificationSettingService.getUserNotificationSettings(userId, RecipientType.CUSTOMER)
        return ResponseEntity.ok(result)
    }

    /**
     * 알림 설정 업데이트
     */
    @Operation(summary = "알림 설정 업데이트", description = "사용자의 알림 설정을 업데이트합니다")
    @PutMapping("/settings")
    fun updateNotificationSettings(
        @RequestHeader("Authorization") token: String,
        @Valid @RequestBody request: NotificationSettingUpdateRequestDto
    ): ResponseEntity<ApiResponse<NotificationSettingResponseDto>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        // TODO: 특정 알림 타입을 지정해야 함
        val result = notificationSettingService.updateNotificationTypeSetting(
            userId, RecipientType.CUSTOMER, 
            com.carava.carwash.notification.entity.NotificationType.RESERVATION_CONFIRMED, 
            request
        )
        return ResponseEntity.ok(result)
    }

    /**
     * FCM 토큰 등록
     */
    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다")
    @PostMapping("/fcm-token")
    fun registerFcmToken(
        @RequestHeader("Authorization") token: String,
        @Valid @RequestBody request: FcmTokenRequestDto
    ): ResponseEntity<ApiResponse<String>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = fcmTokenService.registerToken(
            userId = userId,
            token = request.token,
            deviceId = request.deviceId,
            deviceType = request.deviceType,
            appVersion = request.appVersion
        )
        return ResponseEntity.ok(result)
    }

    /**
     * FCM 토큰 삭제
     */
    @Operation(summary = "FCM 토큰 삭제", description = "사용자의 FCM 토큰을 삭제합니다")
    @DeleteMapping("/fcm-token")
    fun unregisterFcmToken(
        @RequestHeader("Authorization") token: String,
        @Parameter(description = "삭제할 FCM 토큰") @RequestParam fcmToken: String
    ): ResponseEntity<ApiResponse<String>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = fcmTokenService.unregisterToken(userId, fcmToken)
        return ResponseEntity.ok(result)
    }

    /**
     * 로그아웃 시 모든 FCM 토큰 비활성화
     */
    @Operation(summary = "모든 FCM 토큰 비활성화", description = "로그아웃 시 사용자의 모든 FCM 토큰을 비활성화합니다")
    @PostMapping("/fcm-token/deactivate-all")
    fun deactivateAllFcmTokens(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<ApiResponse<String>> {
        val userEmail = jwtUtil.getEmailFromToken(token.removePrefix("Bearer "))
        // TODO: 이메일로 실제 사용자 ID 조회하는 로직 필요
        val userId = 1L // 임시 하드코딩
        val result = fcmTokenService.deactivateAllTokens(userId)
        return ResponseEntity.ok(result)
    }
}