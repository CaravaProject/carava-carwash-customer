package com.carava.carwash.global.exception

/**
 * 메뉴를 찾을 수 없을 때 발생하는 예외
 */
class MenuNotFoundException(message: String = "존재하지 않는 메뉴입니다") : RuntimeException(message)
