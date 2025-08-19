package com.carava.carwash.global.exception

/**
 * 예약 상태가 올바르지 않을 때 발생하는 예외
 */
class InvalidReservationStatusException(message: String) : RuntimeException(message)
