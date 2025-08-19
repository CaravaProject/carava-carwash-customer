package com.carava.carwash.global.exception

/**
 * 예약을 찾을 수 없을 때 발생하는 예외
 */
class ReservationNotFoundException(message: String = "존재하지 않는 예약입니다") : RuntimeException(message)
