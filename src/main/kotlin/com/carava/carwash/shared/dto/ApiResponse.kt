package com.carava.carwash.shared.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String,
    val code: String? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "성공"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }

        fun <T> success(message: String = "성공"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = null,
                message = message
            )
        }

        fun <T> failure(message: String, code: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                data = null,
                message = message,
                code = code
            )
        }
    }
} 