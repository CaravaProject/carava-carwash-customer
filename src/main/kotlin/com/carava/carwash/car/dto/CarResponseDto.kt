package com.carava.carwash.car.dto

import com.carava.carwash.car.entity.Car
import com.carava.carwash.car.entity.CarType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 차량 정보 응답 DTO
 */
@Schema(description = "차량 정보 응답")
data class CarResponseDto(
    @Schema(description = "차량 ID", example = "1")
    val id: Long,
    
    @Schema(description = "차량 브랜드", example = "현대")
    val brand: String,
    
    @Schema(description = "차량 모델", example = "아반떼")
    val model: String,
    
    @Schema(description = "차량 연식", example = "2023")
    val year: Int,
    
    @Schema(description = "차량 색상", example = "화이트")
    val color: String?,
    
    @Schema(description = "차량 번호판", example = "12가3456")
    val licensePlate: String,
    
    @Schema(description = "차량 타입", example = "SEDAN")
    val carType: CarType,
    
    @Schema(description = "기본 차량 여부", example = "true")
    val isDefault: Boolean,
    
    @Schema(description = "차량 표시명", example = "현대 아반떼 (2023년, 화이트)")
    val displayName: String,
    
    @Schema(description = "등록일시", example = "2024-01-01T10:00:00")
    val createdAt: LocalDateTime,
    
    @Schema(description = "수정일시", example = "2024-01-01T10:00:00")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(car: Car): CarResponseDto {
            return CarResponseDto(
                id = car.id,
                brand = car.brand,
                model = car.model,
                year = car.year,
                color = car.color,
                licensePlate = car.licensePlate,
                carType = car.carType,
                isDefault = car.isDefault,
                displayName = car.getDisplayName(),
                createdAt = car.createdAt,
                updatedAt = car.updatedAt
            )
        }
    }
}

/**
 * 차량 목록 응답용 간단한 DTO
 */
@Schema(description = "차량 목록 응답")
data class CarListResponseDto(
    @Schema(description = "차량 ID", example = "1")
    val id: Long,
    
    @Schema(description = "차량 표시명", example = "현대 아반떼 (2023년, 화이트)")
    val displayName: String,
    
    @Schema(description = "차량 번호판", example = "12가3456")
    val licensePlate: String,
    
    @Schema(description = "차량 타입", example = "SEDAN")
    val carType: CarType,
    
    @Schema(description = "기본 차량 여부", example = "true")
    val isDefault: Boolean
) {
    companion object {
        fun from(car: Car): CarListResponseDto {
            return CarListResponseDto(
                id = car.id,
                displayName = car.getDisplayName(),
                licensePlate = car.licensePlate,
                carType = car.carType,
                isDefault = car.isDefault
            )
        }
    }
}