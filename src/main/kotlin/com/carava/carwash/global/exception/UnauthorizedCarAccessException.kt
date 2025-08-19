package com.carava.carwash.global.exception

/**
 * 다른 고객의 차량에 접근하려 할 때 발생하는 예외
 */
class UnauthorizedCarAccessException(message: String = "다른 고객의 차량으로는 예약할 수 없습니다") : RuntimeException(message)
