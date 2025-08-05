package com.carava.carwash.global.dto

data class ApiResponse<T> (
    val success: Boolean,
    val data: T? = null,
    val message: String,
    val code: String? = null,
    val errorCode: String? = null
) {
    companion object {
        fun <T> success(data: T? = null, message: String = "요청이 성공적으로 처리되었습니다"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }

        fun <T> error(errorCode: String, message: String): ApiResponse<T> {
            return ApiResponse(
                success = false,
                data = null,
                message = message,
                errorCode = errorCode
            )
        }
    }
}