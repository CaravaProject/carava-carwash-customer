package com.carava.carwash.reservation.entity

enum class ReservationStatus {
    PENDING,        // 예약 대기
    CONFIRMED,      // 예약 확인
    IN_PROGRESS,    // 서비스 진행중
    COMPLETED,      // 완료 (현장 결제 완료)
    CANCELLED,      // 취소
    NO_SHOW         // 노쇼
} 