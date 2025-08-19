package com.carava.carwash.notification.service

import com.carava.carwash.notification.entity.Notification
import com.carava.carwash.notification.entity.NotificationStatus
import com.carava.carwash.notification.repository.NotificationRepository
import com.google.firebase.messaging.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Firebase FCM 푸시 알림 서비스
 * 실제 Firebase Admin SDK를 사용하여 푸시 알림 발송
 */
@Service
@Transactional
class PushNotificationService(
    private val notificationRepository: NotificationRepository,
    private val fcmTokenService: FcmTokenService,
    private val firebaseMessaging: FirebaseMessaging
) {
    
    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java)
    
    /**
     * 푸시 알림 발송 (실제 Firebase FCM 사용)
     */
    fun sendPushNotification(notification: Notification): Boolean {
        return try {
            logger.info("FCM 푸시 알림 발송 시작 - ID: ${notification.id}, 수신자: ${notification.recipientId}")
            
            // 1. 사용자의 활성화된 FCM 토큰들 조회
            val fcmTokens = fcmTokenService.getActiveTokens(notification.recipientId)
            
            if (fcmTokens.isEmpty()) {
                logger.warn("FCM 토큰이 없는 사용자 - ID: ${notification.recipientId}")
                notification.markAsFailed("FCM 토큰이 등록되지 않음")
                notificationRepository.save(notification)
                return false
            }
            
            // 2. FCM 메시지 생성 (첫 번째 활성 토큰 사용)
            val message = buildFcmMessage(notification, fcmTokens.first().token)
            
            // 3. Firebase Admin SDK를 통한 발송
            val response = firebaseMessaging.send(message)
            
            // 4. 발송 결과 처리
            notification.markAsSent(
                externalId = response,
                externalResponse = "FCM 발송 성공"
            )
            notificationRepository.save(notification)
            
            // 5. 토큰 사용 시간 업데이트
            fcmTokens.first().updateLastUsed()
            
            logger.info("FCM 푸시 알림 발송 성공 - ID: ${notification.id}, FCM ID: $response")
            true
            
        } catch (e: FirebaseMessagingException) {
            logger.error("Firebase 메시징 오류 - ID: ${notification.id}, 오류 코드: ${e.messagingErrorCode}", e)
            
            // Firebase 특정 오류 처리
            val errorMessage = when (e.messagingErrorCode) {
                MessagingErrorCode.INVALID_ARGUMENT -> "잘못된 메시지 형식"
                MessagingErrorCode.UNREGISTERED -> "유효하지 않은 FCM 토큰"
                MessagingErrorCode.SENDER_ID_MISMATCH -> "발신자 ID 불일치"
                MessagingErrorCode.QUOTA_EXCEEDED -> "할당량 초과"
                MessagingErrorCode.UNAVAILABLE -> "FCM 서비스 일시적 사용 불가"
                else -> "Firebase 메시징 오류: ${e.message}"
            }
            
            notification.markAsFailed(errorMessage)
            notificationRepository.save(notification)
            false
            
        } catch (e: Exception) {
            logger.error("푸시 알림 발송 중 예외 발생 - ID: ${notification.id}", e)
            
            notification.markAsFailed("시스템 오류: ${e.message}")
            notificationRepository.save(notification)
            false
        }
    }
    
    /**
     * 배치 푸시 알림 발송 (여러 사용자에게 동일한 메시지)
     */
    fun sendBatchPushNotifications(notifications: List<Notification>): Map<Long, Boolean> {
        val results = mutableMapOf<Long, Boolean>()
        
        logger.info("배치 FCM 푸시 알림 발송 시작 - 대상 수: ${notifications.size}")
        
        notifications.forEach { notification ->
            results[notification.id] = sendPushNotification(notification)
        }
        
        val successCount = results.values.count { it }
        logger.info("배치 FCM 푸시 알림 발송 완료 - 성공: $successCount/${notifications.size}")
        
        return results
    }
    
    /**
     * FCM 메시지 빌드
     */
    private fun buildFcmMessage(notification: Notification, fcmToken: String): Message {
        // Android 설정
        val androidConfig = AndroidConfig.builder()
            .setNotification(
                AndroidNotification.builder()
                    .setTitle(notification.title)
                    .setBody(notification.message)
                    .setIcon("ic_notification")
                    .setColor("#FF6B35") // 카라바 브랜드 컬러
                    .setPriority(AndroidNotification.Priority.HIGH)
                    .build()
            )
            .putData("notificationId", notification.id.toString())
            .putData("type", notification.notificationType.name)
            .putData("recipientId", notification.recipientId.toString())
            .build()
        
        // iOS 설정
        val apnsConfig = ApnsConfig.builder()
            .setAps(
                Aps.builder()
                    .setAlert(
                        ApsAlert.builder()
                            .setTitle(notification.title)
                            .setBody(notification.message)
                            .build()
                    )
                    .setBadge(1)
                    .setSound("default")
                    .build()
            )
            .putCustomData("notificationId", notification.id.toString())
            .putCustomData("type", notification.notificationType.name)
            .putCustomData("recipientId", notification.recipientId.toString())
            .build()
        
        // 웹 푸시 설정
        val webpushConfig = WebpushConfig.builder()
            .setNotification(
                WebpushNotification.builder()
                    .setTitle(notification.title)
                    .setBody(notification.message)
                    .setIcon("/icons/icon-192x192.png")
                    .setBadge("/icons/badge-72x72.png")
                    .build()
            )
            .putData("notificationId", notification.id.toString())
            .putData("type", notification.notificationType.name)
            .putData("recipientId", notification.recipientId.toString())
            .build()
        
        return Message.builder()
            .setToken(fcmToken)
            .setNotification(
                com.google.firebase.messaging.Notification.builder()
                    .setTitle(notification.title)
                    .setBody(notification.message)
                    .build()
            )
            .setAndroidConfig(androidConfig)
            .setApnsConfig(apnsConfig)
            .setWebpushConfig(webpushConfig)
            .putData("notificationId", notification.id.toString())
            .putData("type", notification.notificationType.name)
            .putData("recipientId", notification.recipientId.toString())
            .putData("timestamp", System.currentTimeMillis().toString())
            .build()
    }
    
    /**
     * 토픽 구독 (특정 주제의 알림을 받고 싶은 사용자)
     */
    fun subscribeToTopic(fcmToken: String, topic: String): Boolean {
        return try {
            firebaseMessaging.subscribeToTopic(listOf(fcmToken), topic)
            logger.info("토픽 구독 성공 - 토픽: $topic")
            true
        } catch (e: FirebaseMessagingException) {
            logger.error("토픽 구독 실패 - 토픽: $topic", e)
            false
        }
    }
    
    /**
     * 토픽 구독 해제
     */
    fun unsubscribeFromTopic(fcmToken: String, topic: String): Boolean {
        return try {
            firebaseMessaging.unsubscribeFromTopic(listOf(fcmToken), topic)
            logger.info("토픽 구독 해제 성공 - 토픽: $topic")
            true
        } catch (e: FirebaseMessagingException) {
            logger.error("토픽 구독 해제 실패 - 토픽: $topic", e)
            false
        }
    }
    
    /**
     * FCM 토큰 유효성 검증
     */
    fun validateFcmToken(fcmToken: String): Boolean {
        return try {
            // 테스트 메시지를 dry-run으로 발송하여 토큰 유효성 검증
            val testMessage = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle("Test")
                        .setBody("Token validation")
                        .build()
                )
                .build()
            
            firebaseMessaging.send(testMessage, true) // dry-run = true
            logger.debug("FCM 토큰 유효성 검증 성공")
            true
        } catch (e: FirebaseMessagingException) {
            logger.warn("FCM 토큰 유효성 검증 실패: ${e.message}")
            false
        }
    }
}