package com.carava.carwash.reservation.service

import com.carava.carwash.car.repository.CarRepository
import com.carava.carwash.menu.repository.MenuRepository
import com.carava.carwash.reservation.dto.*
import com.carava.carwash.reservation.entity.Reservation
import com.carava.carwash.reservation.entity.ReservationMenu
import com.carava.carwash.reservation.repository.ReservationMenuRepository
import com.carava.carwash.reservation.repository.ReservationRepository
import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.store.entity.StoreStatus
import com.carava.carwash.store.repository.StoreRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val carRepository: CarRepository,
    private val storeRepository: StoreRepository,
    private val menuRepository: MenuRepository,
    private val reservationMenuRepository: ReservationMenuRepository
) {

    fun createReservation(
        customerId: Long,
        request: CreateReservationRequestDto
    ): ApiResponse<ReservationResponseDto> {
        // 1. 차량 소유권 확인
        val car = carRepository.findById(request.carId)
            .orElseThrow { IllegalArgumentException("존재하지 않는 차량입니다") }
        
        if (car.customerMemberId != customerId) {
            throw IllegalArgumentException("다른 고객의 차량으로는 예약할 수 없습니다")
        }

        // 2. 매장 상태 확인
        val store = storeRepository.findByIdAndStatus(request.storeId, StoreStatus.ACTIVE)
            ?: throw IllegalArgumentException("운영 중이지 않은 매장입니다")

        // 3. 메뉴 유효성 확인
        val menus = menuRepository.findByIdInAndIsActive(request.menuIds, true)
        if (menus.size != request.menuIds.size) {
            throw IllegalArgumentException("일부 메뉴가 존재하지 않거나 비활성화되었습니다")
        }
        if (menus.any { it.storeId != request.storeId }) {
            throw IllegalArgumentException("다른 매장의 메뉴는 선택할 수 없습니다")
        }

        // 4. 중복 예약 시간 확인
        if (reservationRepository.existsByStoreIdAndReservationDateAndReservationTime(
                request.storeId, request.reservationDate, request.reservationTime)) {
            throw IllegalArgumentException("해당 시간에 이미 예약이 있습니다")
        }

        // 5. 금액 계산
        val totalAmount = menus.sumOf { it.price }
        val estimatedDuration = menus.sumOf { it.duration }

        // 6. 예약 생성 (차량 정보 스냅샷과 함께)
        val reservation = Reservation.create(
            customerMemberId = customerId,
            storeId = request.storeId,
            car = car, // 차량 정보 스냅샷 자동 저장
            reservationDate = request.reservationDate,
            reservationTime = request.reservationTime,
            totalAmount = totalAmount,
            finalAmount = totalAmount,
            customerRequest = request.customerRequest,
            estimatedDuration = estimatedDuration
        )

        val savedReservation = reservationRepository.save(reservation)

        // 7. 예약 메뉴 생성
        val reservationMenus = menus.map { menu ->
            ReservationMenu(
                reservationId = savedReservation.id,
                menuId = menu.id,
                quantity = 1,
                unitPrice = menu.price,
                totalPrice = menu.price
            )
        }
        reservationMenuRepository.saveAll(reservationMenus)

        // 8. 응답 DTO 생성
        val menuDtos = menus.map { menu ->
            ReservationMenuDto(
                menuId = menu.id,
                menuName = menu.name,
                quantity = 1,
                unitPrice = menu.price,
                totalPrice = menu.price
            )
        }

        val responseDto = ReservationResponseDto.from(
            reservation = savedReservation,
            storeName = store.name,
            carDisplayName = savedReservation.carDisplayName, // ✅ 스냅샷된 차량 정보 사용
            menus = menuDtos
        )

        return ApiResponse.success(responseDto, "예약이 완료되었습니다")
    }

    @Transactional(readOnly = true)
    fun getMyReservations(
        customerId: Long,
        pageable: Pageable
    ): ApiResponse<Page<ReservationListResponseDto>> {
        val reservations = reservationRepository.findByCustomerMemberId(customerId, pageable)
        
        val responseDto = reservations.map { reservation ->
            // 스냅샷된 차량 정보 사용
            ReservationListResponseDto(
                id = reservation.id,
                storeId = reservation.storeId,
                storeName = "매장명", // TODO: 실제 매장명 조회
                carDisplayName = reservation.carDisplayName, // ✅ 스냅샷된 차량 정보 사용
                reservationDate = reservation.reservationDate,
                reservationTime = reservation.reservationTime,
                status = reservation.status,
                finalAmount = reservation.finalAmount,
                menuCount = 1 // TODO: 실제 메뉴 개수 조회
            )
        }

        return ApiResponse.success(responseDto, "예약 목록 조회가 완료되었습니다")
    }

    @Transactional(readOnly = true)
    fun getReservationDetail(
        customerId: Long,
        reservationId: Long
    ): ApiResponse<ReservationResponseDto> {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { IllegalArgumentException("존재하지 않는 예약입니다") }

        if (reservation.customerMemberId != customerId) {
            throw IllegalArgumentException("다른 고객의 예약입니다")
        }

        // 관련 매장 정보 조회 (차량 정보는 스냅샷 사용)
        val store = storeRepository.findById(reservation.storeId)
            .orElseThrow { IllegalArgumentException("매장 정보를 찾을 수 없습니다") }

        val reservationMenus = reservationMenuRepository.findByReservationId(reservationId)
        val menuIds = reservationMenus.map { it.menuId }
        val menus = if (menuIds.isNotEmpty()) {
            menuRepository.findAllById(menuIds)
        } else {
            emptyList()
        }

        val menuDtos = reservationMenus.map { reservationMenu ->
            val menu = menus.find { it.id == reservationMenu.menuId }
            ReservationMenuDto(
                menuId = reservationMenu.menuId,
                menuName = menu?.name ?: "메뉴명",
                quantity = reservationMenu.quantity,
                unitPrice = reservationMenu.unitPrice,
                totalPrice = reservationMenu.totalPrice
            )
        }

        val responseDto = ReservationResponseDto.from(
            reservation = reservation,
            storeName = store.name,
            carDisplayName = reservation.carDisplayName, // ✅ 스냅샷된 차량 정보 사용
            menus = menuDtos
        )

        return ApiResponse.success(responseDto, "예약 상세 조회가 완료되었습니다")
    }

    fun cancelReservation(
        customerId: Long,
        reservationId: Long,
        reason: String?
    ): ApiResponse<Nothing> {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { IllegalArgumentException("존재하지 않는 예약입니다") }

        if (reservation.customerMemberId != customerId) {
            throw IllegalArgumentException("다른 고객의 예약입니다")
        }

        if (!reservation.canBeCancelled()) {
            throw IllegalArgumentException("취소할 수 없는 예약입니다")
        }

        val cancelledReservation = reservation.cancel(reason)
        reservationRepository.save(cancelledReservation)

        return ApiResponse.success<Nothing>(data = null, message = "예약이 취소되었습니다")
    }
} 