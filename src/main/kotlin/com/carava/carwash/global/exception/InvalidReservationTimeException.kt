package com.carava.carwash.global.exception

/**
 * 예약 불가능한 시간에 예약하려 할 때 발생하는 예외
 */
class InvalidReservationTimeException(message: String = "예약 불가능한 시간입니다") : RuntimeException(message)
