package com.carava.carwash.reservation.service

import com.carava.carwash.car.entity.CarType
import com.carava.carwash.menu.entity.Menu
import com.carava.carwash.menu.repository.MenuRepository
import com.carava.carwash.reservation.dto.AvailableTimeRequestDto
import com.carava.carwash.reservation.entity.Reservation
import com.carava.carwash.reservation.entity.ReservationStatus
import com.carava.carwash.reservation.repository.ReservationRepository
import com.carava.carwash.store.entity.Store
import com.carava.carwash.store.entity.StoreCategory
import com.carava.carwash.store.entity.StoreStatus
import com.carava.carwash.store.repository.StoreRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AvailableTimeServiceTest {

    private val storeRepository = mockk<StoreRepository>()
    private val menuRepository = mockk<MenuRepository>()
    private val reservationRepository = mockk<ReservationRepository>()
    
    private val availableTimeService = AvailableTimeService(
        storeRepository = storeRepository,
        menuRepository = menuRepository,
        reservationRepository = reservationRepository
    )

    private val storeId = 1L
    private lateinit var mockStore: Store
    private lateinit var mockMenus: List<Menu>

    @BeforeEach
    fun setUp() {
        mockStore = Store(
            id = storeId,
            ownerMemberId = 1L,
            name = "테스트 세차장",
            category = StoreCategory.CAR_WASH,
            status = StoreStatus.ACTIVE,
            addressId = 1L,
            openTime = LocalTime.of(9, 0),
            closeTime = LocalTime.of(18, 0)
        )

        mockMenus = listOf(
            Menu(
                id = 1L,
                storeId = storeId,
                categoryName = "기본세차",
                name = "실내외 세차",
                price = BigDecimal("15000"),
                duration = 60,
                isActive = true
            ),
            Menu(
                id = 2L,
                storeId = storeId,
                categoryName = "프리미엄",
                name = "프리미엄 세차",
                price = BigDecimal("25000"),
                duration = 90,
                isActive = true
            )
        )
    }

    @Test
    @DisplayName("예약 가능 시간 조회 - 성공")
    fun getAvailableTimes_Success() {
        // Given
        val tomorrow = LocalDate.now().plusDays(1)
        val request = AvailableTimeRequestDto(
            reservationDate = tomorrow,
            menuIds = listOf(1L, 2L)
        )

        every { storeRepository.findById(storeId) } returns Optional.of(mockStore)
        every { menuRepository.findAllById(request.menuIds) } returns mockMenus
        every { reservationRepository.findByStoreIdAndReservationDateAndStatusIn(any(), any(), any()) } returns emptyList()

        // When
        val result = availableTimeService.getAvailableTimes(storeId, request)

        // Then
        assertTrue(result.success)
        assertEquals("예약 가능 시간 조회 완료", result.message)
        assertEquals(tomorrow, result.data?.date)
        assertEquals("테스트 세차장", result.data?.storeName)
        assertEquals(150, result.data?.totalDuration) // 60 + 90
        assertTrue(result.data?.availableSlots?.isNotEmpty() == true)
    }

    @Test
    @DisplayName("예약 가능 시간 조회 - 매장 없음")
    fun getAvailableTimes_StoreNotFound() {
        // Given
        val tomorrow = LocalDate.now().plusDays(1)
        val request = AvailableTimeRequestDto(
            reservationDate = tomorrow,
            menuIds = listOf(1L)
        )

        every { storeRepository.findById(storeId) } returns Optional.empty()

        // When
        val result = availableTimeService.getAvailableTimes(storeId, request)

        // Then
        assertFalse(result.success)
        assertEquals("STORE_NOT_FOUND", result.errorCode)
        assertEquals("매장을 찾을 수 없습니다", result.message)
    }

    @Test
    @DisplayName("예약 가능 시간 조회 - 비활성 매장")
    fun getAvailableTimes_InactiveStore() {
        // Given
        val tomorrow = LocalDate.now().plusDays(1)
        val request = AvailableTimeRequestDto(
            reservationDate = tomorrow,
            menuIds = listOf(1L)
        )
        val inactiveStore = mockStore.copy(status = StoreStatus.INACTIVE)

        every { storeRepository.findById(storeId) } returns Optional.of(inactiveStore)

        // When
        val result = availableTimeService.getAvailableTimes(storeId, request)

        // Then
        assertFalse(result.success)
        assertEquals("STORE_INACTIVE", result.errorCode)
        assertEquals("현재 이용할 수 없는 매장입니다", result.message)
    }

    @Test
    @DisplayName("예약 가능 시간 조회 - 과거 날짜")
    fun getAvailableTimes_PastDate() {
        // Given
        val yesterday = LocalDate.now().minusDays(1)
        val request = AvailableTimeRequestDto(
            reservationDate = yesterday,
            menuIds = listOf(1L)
        )

        every { storeRepository.findById(storeId) } returns Optional.of(mockStore)

        // When
        val result = availableTimeService.getAvailableTimes(storeId, request)

        // Then
        assertFalse(result.success)
        assertEquals("INVALID_DATE", result.errorCode)
        assertEquals("과거 날짜는 예약할 수 없습니다", result.message)
    }

    @Test
    @DisplayName("예약 가능 시간 조회 - 존재하지 않는 메뉴")
    fun getAvailableTimes_MenuNotFound() {
        // Given
        val tomorrow = LocalDate.now().plusDays(1)
        val request = AvailableTimeRequestDto(
            reservationDate = tomorrow,
            menuIds = listOf(1L, 999L) // 999L은 존재하지 않는 메뉴
        )

        every { storeRepository.findById(storeId) } returns Optional.of(mockStore)
        every { menuRepository.findAllById(request.menuIds) } returns listOf(mockMenus[0]) // 하나만 반환

        // When
        val result = availableTimeService.getAvailableTimes(storeId, request)

        // Then
        assertFalse(result.success)
        assertEquals("MENU_NOT_FOUND", result.errorCode)
        assertEquals("존재하지 않는 메뉴가 포함되어 있습니다", result.message)
    }

    @Test
    @DisplayName("예약 가능 시간 조회 - 다른 매장의 메뉴")
    fun getAvailableTimes_InvalidMenu() {
        // Given
        val tomorrow = LocalDate.now().plusDays(1)
        val request = AvailableTimeRequestDto(
            reservationDate = tomorrow,
            menuIds = listOf(1L)
        )
        val otherStoreMenu = mockMenus[0].copy(storeId = 999L)

        every { storeRepository.findById(storeId) } returns Optional.of(mockStore)
        every { menuRepository.findAllById(request.menuIds) } returns listOf(otherStoreMenu)

        // When
        val result = availableTimeService.getAvailableTimes(storeId, request)

        // Then
        assertFalse(result.success)
        assertEquals("INVALID_MENU", result.errorCode)
        assertEquals("해당 매장의 메뉴가 아닙니다", result.message)
    }

    @Test
    @DisplayName("예약 가능 시간 조회 - 비활성 메뉴")
    fun getAvailableTimes_InactiveMenu() {
        // Given
        val tomorrow = LocalDate.now().plusDays(1)
        val request = AvailableTimeRequestDto(
            reservationDate = tomorrow,
            menuIds = listOf(1L)
        )
        val inactiveMenu = mockMenus[0].copy(isActive = false)

        every { storeRepository.findById(storeId) } returns Optional.of(mockStore)
        every { menuRepository.findAllById(request.menuIds) } returns listOf(inactiveMenu)

        // When
        val result = availableTimeService.getAvailableTimes(storeId, request)

        // Then
        assertFalse(result.success)
        assertEquals("INACTIVE_MENU", result.errorCode)
        assertEquals("현재 이용할 수 없는 메뉴가 포함되어 있습니다", result.message)
    }

    @Test
    @DisplayName("예약 가능 시간 조회 - 기존 예약과 충돌")
    fun getAvailableTimes_WithExistingReservations() {
        // Given
        val tomorrow = LocalDate.now().plusDays(1)
        val request = AvailableTimeRequestDto(
            reservationDate = tomorrow,
            menuIds = listOf(1L) // 60분 소요
        )
        
        // 10:00-11:00에 기존 예약이 있다고 가정
        val existingReservation = Reservation(
            id = 1L,
            customerMemberId = 1L,
            storeId = storeId,
            carId = 1L,
            reservationDate = tomorrow,
            reservationTime = LocalTime.of(10, 0),
            status = ReservationStatus.CONFIRMED,
            totalAmount = BigDecimal("15000"),
            finalAmount = BigDecimal("15000"),
            estimatedDuration = 60,
            // 차량 정보 스냅샷 (테스트용 더미 데이터)
            carBrand = "현대",
            carModel = "아반떠",
            carYear = 2023,
            carColor = "화이트",
            carLicensePlate = "12가3456",
            carType = CarType.SEDAN,
            carDisplayName = "현대 아반떠 (2023년, 화이트)"
        )

        every { storeRepository.findById(storeId) } returns Optional.of(mockStore)
        every { menuRepository.findAllById(request.menuIds) } returns listOf(mockMenus[0])
        every { reservationRepository.findByStoreIdAndReservationDateAndStatusIn(any(), any(), any()) } returns listOf(existingReservation)

        // When
        val result = availableTimeService.getAvailableTimes(storeId, request)

        // Then
        assertTrue(result.success)
        assertEquals(60, result.data?.totalDuration)
        
        // 10:00-11:00 시간대는 예약 불가능해야 함
        val conflictSlot = result.data?.availableSlots?.find { 
            it.startTime == LocalTime.of(10, 0) 
        }
        assertEquals(false, conflictSlot?.isAvailable)
        assertEquals("이미 예약된 시간입니다", conflictSlot?.unavailableReason)
    }

    @Test
    @DisplayName("매장 메뉴 목록 조회 - 성공")
    fun getStoreMenus_Success() {
        // Given
        every { storeRepository.findById(storeId) } returns Optional.of(mockStore)
        every { menuRepository.findByStoreIdAndIsActiveTrue(storeId) } returns mockMenus

        // When
        val result = availableTimeService.getStoreMenus(storeId)

        // Then
        assertTrue(result.success)
        assertEquals("매장 메뉴 목록 조회 완료", result.message)
        assertEquals(2, result.data?.size)
        assertEquals("실내외 세차", result.data?.get(0)?.menuName)
        assertEquals(60, result.data?.get(0)?.duration)
        assertEquals(15000, result.data?.get(0)?.price)
    }

    @Test
    @DisplayName("매장 메뉴 목록 조회 - 매장 없음")
    fun getStoreMenus_StoreNotFound() {
        // Given
        every { storeRepository.findById(storeId) } returns Optional.empty()

        // When
        val result = availableTimeService.getStoreMenus(storeId)

        // Then
        assertFalse(result.success)
        assertEquals("STORE_NOT_FOUND", result.errorCode)
        assertEquals("매장을 찾을 수 없습니다", result.message)
    }
} 