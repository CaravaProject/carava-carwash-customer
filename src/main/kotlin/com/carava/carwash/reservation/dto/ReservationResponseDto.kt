package com.carava.carwash.reservation.dto

import com.carava.carwash.reservation.entity.Reservation
import com.carava.carwash.reservation.entity.ReservationStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ReservationResponseDto(
    val id: Long,
    val storeId: Long,
    val storeName: String,
    val carId: Long,
    val carDisplayName: String,
    val reservationDate: LocalDate,
    val reservationTime: LocalTime,
    val status: ReservationStatus,
    val totalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val customerRequest: String?,
    val rejectionReason: String?,
    val estimatedDuration: Int?,
    val menus: List<ReservationMenuDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(
            reservation: Reservation,
            storeName: String,
            carDisplayName: String,
            menus: List<ReservationMenuDto>
        ): ReservationResponseDto {
            return ReservationResponseDto(
                id = reservation.id,
                storeId = reservation.storeId,
                storeName = storeName,
                carId = reservation.carId,
                carDisplayName = carDisplayName,
                reservationDate = reservation.reservationDate,
                reservationTime = reservation.reservationTime,
                status = reservation.status,
                totalAmount = reservation.totalAmount,
                discountAmount = reservation.discountAmount,
                finalAmount = reservation.finalAmount,
                customerRequest = reservation.customerRequest,
                rejectionReason = reservation.rejectionReason,
                estimatedDuration = reservation.estimatedDuration,
                menus = menus,
                createdAt = reservation.createdAt,
                updatedAt = reservation.updatedAt
            )
        }
    }
}

data class ReservationMenuDto(
    val menuId: Long,
    val menuName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
) 