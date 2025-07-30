package com.carava.carwash.reservation.dto

import com.carava.carwash.reservation.entity.ReservationStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

data class ReservationListResponseDto(
    val id: Long,
    val storeId: Long,
    val storeName: String,
    val carDisplayName: String,
    val reservationDate: LocalDate,
    val reservationTime: LocalTime,
    val status: ReservationStatus,
    val finalAmount: BigDecimal,
    val menuCount: Int
) 