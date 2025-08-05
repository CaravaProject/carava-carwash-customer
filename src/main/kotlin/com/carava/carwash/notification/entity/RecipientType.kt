package com.carava.carwash.notification.entity

/**
 * 알림 수신자 유형 정의
 */
enum class RecipientType(
    val displayName: String,
    val description: String
) {
    CUSTOMER("고객", "고객 회원"),
    OWNER("업체", "업체 회원"),
    ADMIN("관리자", "시스템 관리자");
    
    /**
     * 고객 여부 확인
     */
    fun isCustomer(): Boolean {
        return this == CUSTOMER
    }
    
    /**
     * 업체 여부 확인
     */
    fun isOwner(): Boolean {
        return this == OWNER
    }
}