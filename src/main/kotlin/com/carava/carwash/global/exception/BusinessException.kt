package com.carava.carwash.global.exception

/**
 * 비즈니스 로직 예외 - 에러 코드와 메시지를 함께 관리
 */
class BusinessException(
    val errorCode: ErrorCode,
    message: String = errorCode.defaultMessage
) : RuntimeException(message) {
    
    constructor(errorCode: ErrorCode) : this(errorCode, errorCode.defaultMessage)
}

/**
 * 에러 코드 정의
 */
enum class ErrorCode(val code: String, val defaultMessage: String) {
    // 인증/권한
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "접근 권한이 없습니다"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "잘못된 인증 정보입니다"),
    
    // 리소스 조회
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다"),
    CAR_NOT_FOUND("CAR_NOT_FOUND", "존재하지 않는 차량입니다"),
    STORE_NOT_FOUND("STORE_NOT_FOUND", "존재하지 않는 매장입니다"),
    MENU_NOT_FOUND("MENU_NOT_FOUND", "존재하지 않는 메뉴입니다"),
    RESERVATION_NOT_FOUND("RESERVATION_NOT_FOUND", "존재하지 않는 예약입니다"),
    
    // 비즈니스 규칙 위반
    INVALID_BUSINESS_RULE("INVALID_BUSINESS_RULE", "비즈니스 규칙에 위배됩니다"),
    INVALID_RESERVATION_TIME("INVALID_RESERVATION_TIME", "예약 불가능한 시간입니다"),
    INVALID_RESERVATION_STATUS("INVALID_RESERVATION_STATUS", "잘못된 예약 상태입니다"),
    
    // 중복/충돌
    RESOURCE_CONFLICT("RESOURCE_CONFLICT", "리소스 충돌이 발생했습니다"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다")
}
