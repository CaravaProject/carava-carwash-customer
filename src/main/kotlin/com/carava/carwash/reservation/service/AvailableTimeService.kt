package com.carava.carwash.reservation.service

import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.menu.entity.Menu
import com.carava.carwash.menu.repository.MenuRepository
import com.carava.carwash.reservation.dto.*
import com.carava.carwash.reservation.entity.Reservation
import com.carava.carwash.reservation.entity.ReservationStatus
import com.carava.carwash.reservation.repository.ReservationRepository
import com.carava.carwash.store.entity.Store
import com.carava.carwash.store.repository.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional(readOnly = true)
class AvailableTimeService(
    private val storeRepository: StoreRepository,
    private val menuRepository: MenuRepository,
    private val reservationRepository: ReservationRepository
) {

    /**
     * 예약 가능한 시간대 조회
     */
    fun getAvailableTimes(
        storeId: Long,
        request: AvailableTimeRequestDto
    ): ApiResponse<AvailableTimeResponseDto> {
        return try {
            // 매장 정보 조회
            val store = storeRepository.findById(storeId).orElse(null)
                ?: return ApiResponse.error("STORE_NOT_FOUND", "매장을 찾을 수 없습니다")

            if (!store.isActive()) {
                return ApiResponse.error("STORE_INACTIVE", "현재 이용할 수 없는 매장입니다")
            }

            // 과거 날짜 예약 방지
            if (request.reservationDate.isBefore(LocalDate.now())) {
                return ApiResponse.error("INVALID_DATE", "과거 날짜는 예약할 수 없습니다")
            }

            // 선택한 메뉴들 조회 및 검증
            val selectedMenus = menuRepository.findAllById(request.menuIds)
            if (selectedMenus.size != request.menuIds.size) {
                return ApiResponse.error("MENU_NOT_FOUND", "존재하지 않는 메뉴가 포함되어 있습니다")
            }

            // 매장에 속하지 않은 메뉴 체크
            val invalidMenus = selectedMenus.filter { it.storeId != storeId }
            if (invalidMenus.isNotEmpty()) {
                return ApiResponse.error("INVALID_MENU", "해당 매장의 메뉴가 아닙니다")
            }

            // 비활성 메뉴 체크
            val inactiveMenus = selectedMenus.filter { !it.isActive }
            if (inactiveMenus.isNotEmpty()) {
                return ApiResponse.error("INACTIVE_MENU", "현재 이용할 수 없는 메뉴가 포함되어 있습니다")
            }

            // 총 소요시간 계산
            val totalDuration = selectedMenus.sumOf { it.duration }

            // 해당 날짜의 기존 예약 조회
            val existingReservations = reservationRepository.findByStoreIdAndReservationDateAndStatusIn(
                storeId = storeId,
                reservationDate = request.reservationDate,
                statuses = listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.IN_PROGRESS)
            )

            // 시간대별 예약 가능 여부 계산
            val availableSlots = calculateAvailableTimeSlots(
                store = store,
                date = request.reservationDate,
                totalDuration = totalDuration,
                existingReservations = existingReservations
            )

            val response = AvailableTimeResponseDto(
                date = request.reservationDate,
                storeName = store.name,
                openTime = store.openTime,
                closeTime = store.closeTime,
                totalDuration = totalDuration,
                availableSlots = availableSlots
            )

            ApiResponse.success(response, "예약 가능 시간 조회 완료")
        } catch (e: Exception) {
            ApiResponse.error("INTERNAL_ERROR", "예약 가능 시간 조회 중 오류가 발생했습니다")
        }
    }

    /**
     * 30분 단위 시간대별 예약 가능 여부 계산
     */
    private fun calculateAvailableTimeSlots(
        store: Store,
        date: LocalDate,
        totalDuration: Int,
        existingReservations: List<Reservation>
    ): List<TimeSlotDto> {
        val slots = mutableListOf<TimeSlotDto>()
        val slotInterval = 30 // 30분 단위

        // 오늘 날짜인 경우 현재 시간 이후만 예약 가능
        val now = LocalDateTime.now()
        val earliestTime = if (date == LocalDate.now()) {
            // 현재 시간에서 30분 단위로 올림
            val currentTime = now.toLocalTime()
            val minutes = currentTime.minute
            val roundedMinutes = if (minutes == 0) 0 else ((minutes / 30) + 1) * 30
            currentTime.withMinute(roundedMinutes % 60).withSecond(0).withNano(0)
                .let { if (roundedMinutes >= 60) it.plusHours(1) else it }
        } else {
            store.openTime
        }

        // 영업 시작 시간부터 종료 시간까지 30분 단위로 체크
        var currentTime = maxOf(earliestTime, store.openTime)
        
        while (currentTime.plusMinutes(totalDuration.toLong()) <= store.closeTime) {
            val endTime = currentTime.plusMinutes(totalDuration.toLong())
            
            val (isAvailable, reason) = checkTimeSlotAvailability(
                store = store,
                startTime = currentTime,
                endTime = endTime,
                existingReservations = existingReservations
            )
            
            slots.add(
                TimeSlotDto(
                    startTime = currentTime,
                    endTime = endTime,
                    isAvailable = isAvailable,
                    unavailableReason = reason
                )
            )
            
            currentTime = currentTime.plusMinutes(slotInterval.toLong())
        }

        return slots
    }

    /**
     * 특정 시간대의 예약 가능 여부 체크
     */
    private fun checkTimeSlotAvailability(
        store: Store,
        startTime: LocalTime,
        endTime: LocalTime,
        existingReservations: List<Reservation>
    ): Pair<Boolean, String?> {
        // 영업시간 체크
        if (!store.canMakeReservationAt(startTime, endTime)) {
            return Pair(false, "영업시간 외입니다")
        }

        // 휴게시간 체크
        if (store.breakStartTime != null && store.breakEndTime != null) {
            val breakStart = store.breakStartTime!!
            val breakEnd = store.breakEndTime!!
            
            // 예약 시간이 휴게시간과 겹치는지 체크
            if (!(endTime <= breakStart || startTime >= breakEnd)) {
                return Pair(false, "휴게시간입니다")
            }
        }

        // 기존 예약과의 충돌 체크
        for (reservation in existingReservations) {
            val reservationStart = reservation.reservationTime
            val reservationEnd = reservation.reservationTime.plusMinutes(reservation.estimatedDuration?.toLong() ?: 60L)
            
            // 시간 겹침 체크
            if (!(endTime <= reservationStart || startTime >= reservationEnd)) {
                return Pair(false, "이미 예약된 시간입니다")
            }
        }

        return Pair(true, null)
    }

    /**
     * 매장의 메뉴 목록 조회
     */
    fun getStoreMenus(storeId: Long): ApiResponse<List<SelectedMenuSummaryDto>> {
        return try {
            val store = storeRepository.findById(storeId).orElse(null)
                ?: return ApiResponse.error("STORE_NOT_FOUND", "매장을 찾을 수 없습니다")

            val menus = menuRepository.findByStoreIdAndIsActiveTrue(storeId)
            
            val menuSummaries = menus.map { menu ->
                SelectedMenuSummaryDto(
                    menuId = menu.id,
                    menuName = menu.name,
                    duration = menu.duration,
                    price = menu.price.toInt()
                )
            }

            ApiResponse.success(menuSummaries, "매장 메뉴 목록 조회 완료")
        } catch (e: Exception) {
            ApiResponse.error("INTERNAL_ERROR", "메뉴 조회 중 오류가 발생했습니다")
        }
    }
} 