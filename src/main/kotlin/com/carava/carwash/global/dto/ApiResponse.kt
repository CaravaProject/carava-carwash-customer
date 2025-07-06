package com.carava.carwash.global.dto

data class ApiResponse<T> (
    val isSuccess: Boolean,
    val data: T? = null,
    val errorCode: String? = null
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse(
                isSuccess = true,
                data = data
            )
        }

        fun error(errorCode: String? = null): ApiResponse<Nothing> {
            return ApiResponse(
                isSuccess = false,
                errorCode = errorCode,
                data = null
            )
        }
    }
}