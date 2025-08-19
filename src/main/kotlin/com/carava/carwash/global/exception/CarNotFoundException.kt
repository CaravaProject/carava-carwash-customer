package com.carava.carwash.global.exception

/**
 * 차량을 찾을 수 없을 때 발생하는 예외
 */
class CarNotFoundException(message: String = "존재하지 않는 차량입니다") : RuntimeException(message)
