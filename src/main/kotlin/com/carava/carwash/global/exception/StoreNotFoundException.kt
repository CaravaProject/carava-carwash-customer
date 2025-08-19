package com.carava.carwash.global.exception

/**
 * 매장을 찾을 수 없을 때 발생하는 예외
 */
class StoreNotFoundException(message: String = "존재하지 않는 매장입니다") : RuntimeException(message)
