package com.carava.carwash.reservation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

/**
 * 예약 가능 시간 조회 응답 DTO
 */
@Schema(description = "예약 가능 시간 조회 응답")
data class AvailableTimeResponseDto(
    @Schema(description = "조회 날짜", example = "2024-08-05")
    val date: LocalDate,
    
    @Schema(description = "매장명", example = "카라바 세차장")
    val storeName: String,
    
    @Schema(description = "영업 시작 시간", example = "09:00")
    val openTime: LocalTime,
    
    @Schema(description = "영업 종료 시간", example = "18:00")
    val closeTime: LocalTime,
    
    @Schema(description = "선택한 메뉴들의 총 소요시간(분)", example = "90")
    val totalDuration: Int,
    
    @Schema(description = "예약 가능한 시간대 목록")
    val availableSlots: List<TimeSlotDto>
)

/**
 * 시간대 정보 DTO
 */
@Schema(description = "시간대 정보")
data class TimeSlotDto(
    @Schema(description = "시작 시간", example = "10:00")
    val startTime: LocalTime,
    
    @Schema(description = "종료 시간", example = "11:30")
    val endTime: LocalTime,
    
    @Schema(description = "예약 가능 여부", example = "true")
    val isAvailable: Boolean,
    
    @Schema(description = "불가능한 이유 (가능한 경우 null)", example = "이미 예약된 시간입니다")
    val unavailableReason: String? = null
)

/**
 * 메뉴 정보 요약 DTO
 */
@Schema(description = "선택된 메뉴 요약")
data class SelectedMenuSummaryDto(
    @Schema(description = "메뉴 ID", example = "1")
    val menuId: Long,
    
    @Schema(description = "메뉴명", example = "기본 세차")
    val menuName: String,
    
    @Schema(description = "소요시간(분)", example = "60")
    val duration: Int,
    
    @Schema(description = "가격", example = "15000")
    val price: Int
) 