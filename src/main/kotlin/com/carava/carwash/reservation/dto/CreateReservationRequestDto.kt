package com.carava.carwash.reservation.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.time.LocalTime

data class CreateReservationRequestDto(
    @field:NotNull(message = "매장 ID는 필수입니다")
    @field:Positive(message = "매장 ID는 양수여야 합니다")
    val storeId: Long,

    @field:NotNull(message = "차량 ID는 필수입니다")
    @field:Positive(message = "차량 ID는 양수여야 합니다")
    val carId: Long,

    @field:NotNull(message = "예약 날짜는 필수입니다")
    @field:Future(message = "예약 날짜는 미래 날짜여야 합니다")
    val reservationDate: LocalDate,

    @field:NotNull(message = "예약 시간은 필수입니다")
    val reservationTime: LocalTime,

    @field:NotEmpty(message = "메뉴는 최소 하나 이상 선택해야 합니다")
    val menuIds: List<Long>,

    val customerRequest: String? = null,

    val beforeImageIds: List<Long>? = null
) 