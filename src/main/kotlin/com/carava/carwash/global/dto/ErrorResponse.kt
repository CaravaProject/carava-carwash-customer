package com.carava.carwash.global.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "오류 응답")
data class ErrorResponse(
    @Schema(description = "성공 여부", example = "false")
    val success: Boolean = false,
    
    @Schema(description = "데이터", example = "null")
    val data: Any? = null,
    
    @Schema(description = "오류 메시지", example = "잘못된 요청입니다")
    val message: String,
    
    @Schema(description = "응답 코드", example = "null")
    val code: String? = null,
    
    @Schema(description = "오류 코드", example = "VALIDATION_ERROR")
    val errorCode: String
) {
    companion object {
        fun validationError() = ErrorResponse(
            message = "입력값이 올바르지 않습니다",
            errorCode = "VALIDATION_ERROR"
        )
        
        fun authenticationError() = ErrorResponse(
            message = "인증에 실패했습니다",
            errorCode = "AUTHENTICATION_ERROR"
        )
        
        fun authorizationError() = ErrorResponse(
            message = "권한이 없습니다",
            errorCode = "AUTHORIZATION_ERROR"
        )
        
        fun notFoundError() = ErrorResponse(
            message = "요청한 리소스를 찾을 수 없습니다",
            errorCode = "NOT_FOUND"
        )
        
        fun conflictError() = ErrorResponse(
            message = "이미 존재하는 데이터입니다",
            errorCode = "CONFLICT"
        )
        
        fun internalServerError() = ErrorResponse(
            message = "서버 내부 오류가 발생했습니다",
            errorCode = "INTERNAL_SERVER_ERROR"
        )
    }
} 