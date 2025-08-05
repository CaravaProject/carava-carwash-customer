package com.carava.carwash.notification.service

import com.carava.carwash.notification.entity.Notification
import com.carava.carwash.notification.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * SMS 알림 서비스
 * 
 * 실제 프로덕션에서는 외부 SMS 서비스 (NAVER SENS, AWS SNS 등)를 사용하여 구현
 * 현재는 모의(Mock) 구현으로 제공
 */
@Service
@Transactional
class SmsNotificationService(
    private val notificationRepository: NotificationRepository
) {
    
    private val logger = LoggerFactory.getLogger(SmsNotificationService::class.java)
    
    /**
     * SMS 알림 발송
     */
    fun sendSmsNotification(notification: Notification): Boolean {
        return try {
            logger.info("SMS 알림 발송 시작 - ID: ${notification.id}, 수신자: ${notification.recipientId}")
            
            // TODO: 실제 SMS 발송 로직 구현
            // 1. 사용자 휴대폰 번호 조회
            // 2. SMS 메시지 생성
            // 3. 외부 SMS 서비스를 통한 발송
            
            // 현재는 모의 발송으로 처리
            val mockResponse = simulateSmsSend(notification)
            
            if (mockResponse.success) {
                notification.markAsSent(
                    externalId = mockResponse.messageId,
                    externalResponse = mockResponse.response
                )
                notificationRepository.save(notification)
                
                logger.info("SMS 알림 발송 성공 - ID: ${notification.id}, SMS ID: ${mockResponse.messageId}")
                true
            } else {
                notification.markAsFailed(mockResponse.errorMessage ?: "알 수 없는 오류")
                notificationRepository.save(notification)
                
                logger.error("SMS 알림 발송 실패 - ID: ${notification.id}, 오류: ${mockResponse.errorMessage}")
                false
            }
            
        } catch (e: Exception) {
            logger.error("SMS 알림 발송 중 예외 발생 - ID: ${notification.id}", e)
            
            notification.markAsFailed("발송 중 예외 발생: ${e.message}")
            notificationRepository.save(notification)
            
            false
        }
    }
    
    /**
     * 배치 SMS 알림 발송
     */
    fun sendBatchSmsNotifications(notifications: List<Notification>): BatchSendResult {
        var successCount = 0
        var failureCount = 0
        
        for (notification in notifications) {
            if (sendSmsNotification(notification)) {
                successCount++
            } else {
                failureCount++
            }
        }
        
        return BatchSendResult(
            totalCount = notifications.size,
            successCount = successCount,
            failureCount = failureCount
        )
    }
    
    /**
     * 휴대폰 번호 유효성 검증
     */
    fun validatePhoneNumber(phoneNumber: String): Boolean {
        // 한국 휴대폰 번호 형식 검증 (010-xxxx-xxxx)
        val phoneRegex = Regex("^01[0-9]-?[0-9]{4}-?[0-9]{4}$")
        return phoneRegex.matches(phoneNumber.replace("-", ""))
    }
    
    /**
     * SMS 메시지 길이 검증 (한국 SMS 표준: 90자)
     */
    fun validateSmsLength(message: String): Boolean {
        return message.length <= 90
    }
    
    /**
     * LMS 메시지 길이 검증 (한국 LMS 표준: 2000자)
     */
    fun validateLmsLength(message: String): Boolean {
        return message.length <= 2000
    }
    
    /**
     * 메시지 타입 결정 (SMS/LMS)
     */
    fun determineMessageType(message: String): SmsMessageType {
        return if (message.length <= 90) {
            SmsMessageType.SMS
        } else if (message.length <= 2000) {
            SmsMessageType.LMS
        } else {
            SmsMessageType.INVALID
        }
    }
    
    /**
     * 모의 SMS 발송 (개발/테스트용)
     */
    private fun simulateSmsSend(notification: Notification): MockSmsResponse {
        // 실제 환경에서는 이 부분을 실제 SMS 발송 로직으로 교체
        return try {
            // 메시지 길이 검증
            val messageType = determineMessageType(notification.message)
            if (messageType == SmsMessageType.INVALID) {
                return MockSmsResponse(
                    success = false,
                    errorMessage = "메시지가 너무 깁니다 (최대 2000자)"
                )
            }
            
            // 모의 발송 성공 (95% 확률로 성공)
            val success = (1..20).random() <= 19
            
            if (success) {
                MockSmsResponse(
                    success = true,
                    messageId = "sms_${System.currentTimeMillis()}_${(1000..9999).random()}",
                    response = """{"messageId": "sms_${System.currentTimeMillis()}", "status": "SENT"}""",
                    messageType = messageType
                )
            } else {
                MockSmsResponse(
                    success = false,
                    errorMessage = "휴대폰 번호가 유효하지 않습니다"
                )
            }
            
        } catch (e: Exception) {
            MockSmsResponse(
                success = false,
                errorMessage = e.message ?: "알 수 없는 오류"
            )
        }
    }
    
    /**
     * 실제 SMS 발송 로직 (향후 구현)
     */
    private fun sendActualSmsMessage(notification: Notification): SmsResponse {
        // TODO: 외부 SMS 서비스 사용
        /*
        예시 - NAVER SENS 사용 시:
        
        val request = SmsRequest(
            type = determineMessageType(notification.message).toString(),
            from = "01012345678", // 발신번호
            content = notification.message,
            messages = listOf(
                SmsMessage(
                    to = getUserPhoneNumber(notification.recipientId)
                )
            )
        )
        
        val response = naverSensClient.sendSms(request)
        return SmsResponse(
            success = response.statusCode == "202",
            messageId = response.requestId,
            response = response.toString()
        )
        */
        
        throw NotImplementedError("실제 SMS 발송은 외부 SMS 서비스 설정 후 구현 예정")
    }
    
    /**
     * 사용자 휴대폰 번호 조회 (향후 구현)
     */
    private fun getUserPhoneNumber(userId: Long): String? {
        // TODO: 사용자 휴대폰 번호 조회 로직 구현
        // 개인정보이므로 암호화되어 저장된 번호를 복호화하여 반환
        return null
    }
    
    enum class SmsMessageType {
        SMS,    // 90자 이하 단문
        LMS,    // 2000자 이하 장문
        INVALID // 2000자 초과
    }
    
    data class MockSmsResponse(
        val success: Boolean,
        val messageId: String? = null,
        val response: String? = null,
        val errorMessage: String? = null,
        val messageType: SmsMessageType? = null
    )
    
    data class SmsResponse(
        val success: Boolean,
        val messageId: String? = null,
        val response: String? = null,
        val errorMessage: String? = null
    )
    
    data class BatchSendResult(
        val totalCount: Int,
        val successCount: Int,
        val failureCount: Int
    ) {
        val successRate: Double get() = if (totalCount > 0) successCount.toDouble() / totalCount else 0.0
    }
}