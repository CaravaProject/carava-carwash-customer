package com.carava.carwash.notification.entity

/**
 * 알림 상태 정의
 */
enum class NotificationStatus(
    val displayName: String,
    val description: String
) {
    PENDING("대기중", "발송 대기 중인 상태"),
    SENT("발송됨", "알림이 성공적으로 발송됨"),
    DELIVERED("전달됨", "수신자에게 전달됨"),
    READ("읽음", "사용자가 알림을 확인함"),
    FAILED("실패", "알림 발송에 실패함"),
    CANCELLED("취소됨", "알림 발송이 취소됨");
    
    /**
     * 완료 상태 여부 확인
     */
    fun isCompleted(): Boolean {
        return this in listOf(DELIVERED, READ, FAILED, CANCELLED)
    }
    
    /**
     * 성공 상태 여부 확인
     */
    fun isSuccess(): Boolean {
        return this in listOf(SENT, DELIVERED, READ)
    }
}