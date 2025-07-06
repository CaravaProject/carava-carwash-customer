package com.carava.carwash.global.exception

import com.carava.carwash.global.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException) =
        ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_ERROR"))

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException) =
        ResponseEntity.badRequest().body(ApiResponse.error("EMAIL_ALREADY_EXISTS"))

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(ex: BadCredentialsException) =
        ResponseEntity.badRequest().body(ApiResponse.error("INVALID_CREDENTIALS"))

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFoundException(ex: UsernameNotFoundException) =
        ResponseEntity.badRequest().body(ApiResponse.error("USER_NOT_FOUND"))

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception) =
        ResponseEntity.internalServerError().body(ApiResponse.error("INTERNAL_SERVER_ERROR"))
}