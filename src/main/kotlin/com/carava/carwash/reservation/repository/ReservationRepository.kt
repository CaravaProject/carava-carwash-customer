package com.carava.carwash.reservation.repository

import com.carava.carwash.reservation.entity.Reservation
import com.carava.carwash.reservation.entity.ReservationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalTime

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {
    
    fun findByCustomerMemberId(customerMemberId: Long, pageable: Pageable): Page<Reservation>
    
    fun findByStoreId(storeId: Long, pageable: Pageable): Page<Reservation>
    
    fun findByStoreIdAndReservationDate(storeId: Long, reservationDate: LocalDate): List<Reservation>
    
    /**
     * 특정 매장, 날짜, 상태 목록으로 예약 조회
     */
    fun findByStoreIdAndReservationDateAndStatusIn(
        storeId: Long,
        reservationDate: LocalDate,
        statuses: List<ReservationStatus>
    ): List<Reservation>
    
    fun existsByStoreIdAndReservationDateAndReservationTime(
        storeId: Long,
        reservationDate: LocalDate,
        reservationTime: LocalTime
    ): Boolean
    
    @Query("""
        SELECT COUNT(r) FROM Reservation r 
        WHERE r.storeId = :storeId 
        AND r.reservationDate = :reservationDate 
        AND r.reservationTime BETWEEN :startTime AND :endTime
        AND r.status NOT IN ('CANCELLED', 'NO_SHOW')
    """)
    fun countActiveReservationsByStoreAndDateTimePeriod(
        storeId: Long,
        reservationDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime
    ): Long
    
    fun findByCustomerMemberIdAndStatus(
        customerMemberId: Long,
        status: ReservationStatus,
        pageable: Pageable
    ): Page<Reservation>
} 