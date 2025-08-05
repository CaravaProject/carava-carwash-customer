package com.carava.carwash.notification.entity

/**
 * 알림 유형 정의
 */
enum class NotificationType(
    val displayName: String,
    val description: String
) {
    // 예약 관련 알림
    RESERVATION_REQUESTED("예약 요청", "새로운 예약 요청이 접수되었습니다"),
    RESERVATION_CONFIRMED("예약 확정", "예약이 확정되었습니다"),
    RESERVATION_REJECTED("예약 거절", "예약이 거절되었습니다"),
    RESERVATION_CANCELLED("예약 취소", "예약이 취소되었습니다"),
    RESERVATION_REMINDER_2H("예약 알림 (2시간 전)", "예약 시간 2시간 전 알림입니다"),
    RESERVATION_REMINDER_30M("예약 알림 (30분 전)", "예약 시간 30분 전 알림입니다"),
    RESERVATION_STARTED("서비스 시작", "서비스가 시작되었습니다"),
    RESERVATION_COMPLETED("서비스 완료", "서비스가 완료되었습니다"),
    
    // 채팅 관련 알림
    CHAT_MESSAGE_RECEIVED("채팅 메시지", "새로운 메시지가 도착했습니다"),
    
    // 리뷰 관련 알림
    REVIEW_REQUEST("리뷰 작성 요청", "서비스 이용 후기를 작성해주세요"),
    REVIEW_RECEIVED("리뷰 등록", "새로운 리뷰가 등록되었습니다"),
    REVIEW_REPLY_RECEIVED("리뷰 답글", "작성한 리뷰에 답글이 달렸습니다"),
    
    // 시스템 관련 알림
    SYSTEM_MAINTENANCE("시스템 점검", "시스템 점검 안내입니다"),
    SYSTEM_UPDATE("앱 업데이트", "새로운 버전이 출시되었습니다"),
    
    // 마케팅 관련 알림
    PROMOTION("프로모션", "특별 할인 혜택을 확인하세요"),
    EVENT("이벤트", "새로운 이벤트에 참여하세요");
    
    /**
     * 중요도에 따른 알림 채널 결정
     */
    fun getDefaultChannels(): List<NotificationChannel> {
        return when (this) {
            // 고중요도: 푸시 + SMS
            RESERVATION_CONFIRMED, RESERVATION_REJECTED, RESERVATION_CANCELLED -> 
                listOf(NotificationChannel.PUSH, NotificationChannel.SMS)
            
            // 중간중요도: 푸시만
            RESERVATION_REQUESTED, RESERVATION_REMINDER_2H, RESERVATION_REMINDER_30M,
            RESERVATION_STARTED, RESERVATION_COMPLETED, REVIEW_REQUEST, REVIEW_RECEIVED,
            REVIEW_REPLY_RECEIVED, CHAT_MESSAGE_RECEIVED -> 
                listOf(NotificationChannel.PUSH)
            
            // 저중요도: 푸시 (사용자 설정에 따라)
            SYSTEM_MAINTENANCE, SYSTEM_UPDATE, PROMOTION, EVENT -> 
                listOf(NotificationChannel.PUSH)
        }
    }
    
    /**
     * 마케팅성 알림 여부 확인
     */
    fun isMarketingNotification(): Boolean {
        return this in listOf(PROMOTION, EVENT)
    }
}