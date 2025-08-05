package com.carava.carwash.reservation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate

/**
 * 예약 가능 시간 조회 요청 DTO
 */
@Schema(description = "예약 가능 시간 조회 요청")
data class AvailableTimeRequestDto(
    @Schema(description = "예약 날짜", example = "2024-08-05", required = true)
    @field:NotNull(message = "예약 날짜는 필수입니다")
    val reservationDate: LocalDate,
    
    @Schema(description = "선택된 메뉴 ID 목록", example = "[1, 2, 3]", required = true)
    @field:NotEmpty(message = "최소 하나의 메뉴를 선택해야 합니다")
    val menuIds: List<@Positive(message = "메뉴 ID는 양수여야 합니다") Long>
) 