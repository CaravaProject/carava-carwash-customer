package com.carava.carwash.car.dto

import com.carava.carwash.car.entity.CarType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

/**
 * 차량 등록/수정 요청 DTO
 */
@Schema(description = "차량 등록/수정 요청")
data class CarRequestDto(
    @Schema(description = "차량 브랜드", example = "현대", required = true)
    @field:NotBlank(message = "브랜드는 필수입니다")
    val brand: String,
    
    @Schema(description = "차량 모델", example = "아반떼", required = true)
    @field:NotBlank(message = "모델은 필수입니다")
    val model: String,
    
    @Schema(description = "차량 연식", example = "2023", required = true)
    @field:NotNull(message = "연식은 필수입니다")
    @field:Positive(message = "연식은 양수여야 합니다")
    val year: Int,
    
    @Schema(description = "차량 색상", example = "화이트")
    val color: String? = null,
    
    @Schema(description = "차량 번호판", example = "12가3456", required = true)
    @field:NotBlank(message = "번호판은 필수입니다")
    val licensePlate: String,
    
    @Schema(description = "차량 타입", example = "SEDAN", required = true)
    @field:NotNull(message = "차량 타입은 필수입니다")
    val carType: CarType
)