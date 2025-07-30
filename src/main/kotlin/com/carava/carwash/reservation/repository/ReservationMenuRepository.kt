package com.carava.carwash.reservation.repository

import com.carava.carwash.reservation.entity.ReservationMenu
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReservationMenuRepository : JpaRepository<ReservationMenu, Long> {
    
    fun findByReservationId(reservationId: Long): List<ReservationMenu>
    
    fun deleteByReservationId(reservationId: Long)
} 