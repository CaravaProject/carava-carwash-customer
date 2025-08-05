package com.carava.carwash.notification.entity

/**
 * 알림 채널 정의
 */
enum class NotificationChannel(
    val displayName: String,
    val description: String
) {
    PUSH("푸시 알림", "앱 푸시 알림으로 전송"),
    SMS("SMS", "문자 메시지로 전송"),
    EMAIL("이메일", "이메일로 전송");
    
    /**
     * 실시간 알림 여부 확인
     */
    fun isRealtime(): Boolean {
        return this in listOf(PUSH, SMS)
    }
}