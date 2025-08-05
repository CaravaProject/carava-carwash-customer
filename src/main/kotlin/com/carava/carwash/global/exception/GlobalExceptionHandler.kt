package com.carava.carwash.global.exception

import com.carava.carwash.global.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException) =
        ResponseEntity.badRequest().body(ApiResponse.error<Nothing>("VALIDATION_ERROR", "입력값이 올바르지 않습니다"))

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException) =
        ResponseEntity.badRequest().body(ApiResponse.error<Nothing>("EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다"))

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException) =
        ResponseEntity.badRequest().body(ApiResponse.error<Nothing>("INVALID_CREDENTIALS", "잘못된 인증 정보입니다"))

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFoundException(ex: UsernameNotFoundException) =
        ResponseEntity.badRequest().body(ApiResponse.error<Nothing>("USER_NOT_FOUND", "사용자를 찾을 수 없습니다"))

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error occurred", ex)
        logger.error("Error class: ${ex.javaClass.name}")
        logger.error("Error message: ${ex.message}")
        ex.printStackTrace()
        return ResponseEntity.internalServerError().body(ApiResponse.error<Nothing>("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다"))
    }
}