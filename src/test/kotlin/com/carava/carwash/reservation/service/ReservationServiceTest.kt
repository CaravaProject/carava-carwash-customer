package com.carava.carwash.reservation.service

import com.carava.carwash.car.entity.Car
import com.carava.carwash.car.entity.CarType
import com.carava.carwash.car.repository.CarRepository
import com.carava.carwash.menu.entity.Menu
import com.carava.carwash.menu.repository.MenuRepository
import com.carava.carwash.reservation.dto.CreateReservationRequestDto
import com.carava.carwash.reservation.entity.Reservation
import com.carava.carwash.reservation.entity.ReservationMenu
import com.carava.carwash.reservation.entity.ReservationStatus
import com.carava.carwash.reservation.repository.ReservationMenuRepository
import com.carava.carwash.reservation.repository.ReservationRepository
import com.carava.carwash.store.entity.Store
import com.carava.carwash.store.entity.StoreCategory
import com.carava.carwash.store.entity.StoreStatus
import com.carava.carwash.store.repository.StoreRepository
import com.carava.carwash.image.service.ImageUploadService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReservationServiceTest {

    private val reservationRepository = mockk<ReservationRepository>()
    private val carRepository = mockk<CarRepository>()
    private val storeRepository = mockk<StoreRepository>()
    private val menuRepository = mockk<MenuRepository>()
    private val reservationMenuRepository = mockk<ReservationMenuRepository>()

    private val imageUploadService = mockk<ImageUploadService>()

    private val reservationService = ReservationService(
        reservationRepository = reservationRepository,
        carRepository = carRepository,
        storeRepository = storeRepository,
        menuRepository = menuRepository,
        reservationMenuRepository = reservationMenuRepository,
        imageUploadService = imageUploadService
    )

    @Test
    fun `given_valid_reservation_request_when_create_reservation_then_success`() {
        // given
        val customerId = 1L
        val request = CreateReservationRequestDto(
            storeId = 1L,
            carId = 1L,
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            menuIds = listOf(1L, 2L),
            customerRequest = "깨끗하게 부탁드립니다"
        )

        val car = Car(
            id = 1L,
            customerMemberId = customerId,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )

        val store = Store(
            id = 1L,
            ownerMemberId = 2L,
            name = "깨끗한 세차장",
            addressId = 1L,
            category = StoreCategory.CAR_WASH,
            status = StoreStatus.ACTIVE
        )

        val menus = listOf(
            Menu(
                id = 1L,
                storeId = 1L,
                categoryName = "기본세차",
                name = "일반세차",
                price = BigDecimal("15000"),
                duration = 30
            ),
            Menu(
                id = 2L,
                storeId = 1L,
                categoryName = "프리미엄",
                name = "프리미엄세차",
                price = BigDecimal("25000"),
                duration = 60
            )
        )

        val savedReservation = Reservation(
            id = 1L,
            customerMemberId = customerId,
            storeId = request.storeId,
            carId = request.carId,
            reservationDate = request.reservationDate,
            reservationTime = request.reservationTime,
            status = ReservationStatus.PENDING,
            totalAmount = BigDecimal("40000"),
            finalAmount = BigDecimal("40000"),
            customerRequest = request.customerRequest,
            estimatedDuration = 90,
            // 차량 정보 스냅샷 (테스트용)
            carBrand = car.brand,
            carModel = car.model,
            carYear = car.year,
            carColor = car.color,
            carLicensePlate = car.licensePlate,
            carType = car.carType,
            carDisplayName = car.getDisplayName()
        )

        every { carRepository.findById(request.carId) } returns java.util.Optional.of(car)
        every { storeRepository.findByIdAndStatus(request.storeId, StoreStatus.ACTIVE) } returns store
        every { menuRepository.findByIdInAndIsActive(request.menuIds, true) } returns menus
        every { reservationRepository.existsByStoreIdAndReservationDateAndReservationTime(
            request.storeId, request.reservationDate, request.reservationTime) } returns false
        every { reservationRepository.save(any()) } returns savedReservation
        every { reservationMenuRepository.saveAll(any<List<ReservationMenu>>()) } returns listOf()

        // when
        val response = reservationService.createReservation(customerId, request)

        // then
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(savedReservation.id, response.data?.id)
        assertEquals("깨끗한 세차장", response.data?.storeName)
        assertEquals("현대 아반떼 (2023년)", response.data?.carDisplayName)

        verify { reservationRepository.save(any()) }
        verify { reservationMenuRepository.saveAll(any<List<ReservationMenu>>()) }
    }

    @Test
    fun `given_non_owned_car_when_create_reservation_then_throw_exception`() {
        // given
        val customerId = 1L
        val request = CreateReservationRequestDto(
            storeId = 1L,
            carId = 1L,
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            menuIds = listOf(1L)
        )

        val car = Car(
            id = 1L,
            customerMemberId = 999L, // 다른 고객의 차량
            brand = "현대",
            model = "아반떼",
            year = 2023,
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )

        every { carRepository.findById(request.carId) } returns java.util.Optional.of(car)

        // when & then
        assertThrows<IllegalArgumentException> {
            reservationService.createReservation(customerId, request)
        }
    }

    @Test
    fun `given_duplicate_reservation_time_when_create_reservation_then_throw_exception`() {
        // given
        val customerId = 1L
        val request = CreateReservationRequestDto(
            storeId = 1L,
            carId = 1L,
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            menuIds = listOf(1L)
        )

        val car = Car(
            id = 1L,
            customerMemberId = customerId,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )

        val store = Store(
            id = 1L,
            ownerMemberId = 2L,
            name = "깨끗한 세차장",
            addressId = 1L,
            category = StoreCategory.CAR_WASH,
            status = StoreStatus.ACTIVE
        )

        every { carRepository.findById(request.carId) } returns java.util.Optional.of(car)
        every { storeRepository.findByIdAndStatus(request.storeId, StoreStatus.ACTIVE) } returns store
        every { reservationRepository.existsByStoreIdAndReservationDateAndReservationTime(
            request.storeId, request.reservationDate, request.reservationTime) } returns true

        // when & then
        assertThrows<IllegalArgumentException> {
            reservationService.createReservation(customerId, request)
        }
    }

    @Test
    fun `given_customer_id_when_get_my_reservations_then_return_list`() {
        // given
        val customerId = 1L
        val pageable = PageRequest.of(0, 10)
        
        val reservations = listOf(
            Reservation(
                id = 1L,
                customerMemberId = customerId,
                storeId = 1L,
                carId = 1L,
                reservationDate = LocalDate.now().plusDays(1),
                reservationTime = LocalTime.of(10, 0),
                status = ReservationStatus.PENDING,
                totalAmount = BigDecimal("30000"),
                finalAmount = BigDecimal("30000"),
                // 차량 정보 스냅샷 (테스트용 더미 데이터)
                carBrand = "현대",
                carModel = "아반떼",
                carYear = 2023,
                carColor = "화이트",
                carLicensePlate = "12가3456",
                carType = CarType.SEDAN,
                carDisplayName = "현대 아반떼 (2023년, 화이트)"
            )
        )

        every { reservationRepository.findByCustomerMemberId(customerId, pageable) } returns PageImpl(reservations)

        // when
        val response = reservationService.getMyReservations(customerId, pageable)

        // then
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(1, response.data?.content?.size)
    }

    @Test
    fun `given_reservation_id_when_cancel_reservation_then_success`() {
        // given
        val customerId = 1L
        val reservationId = 1L
        val reason = "일정 변경"
        
        val reservation = Reservation(
            id = reservationId,
            customerMemberId = customerId,
            storeId = 1L,
            carId = 1L,
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            status = ReservationStatus.PENDING,
            totalAmount = BigDecimal("30000"),
            finalAmount = BigDecimal("30000"),
            // 차량 정보 스냅샷 (테스트용 더미 데이터)
            carBrand = "현대",
            carModel = "아반떼",
            carYear = 2023,
            carColor = "화이트",
            carLicensePlate = "12가3456",
            carType = CarType.SEDAN,
            carDisplayName = "현대 아반떼 (2023년, 화이트)"
        )

        val cancelledReservation = reservation.cancel(reason)

        every { reservationRepository.findById(reservationId) } returns java.util.Optional.of(reservation)
        every { reservationRepository.save(any()) } returns cancelledReservation

        // when
        val response = reservationService.cancelReservation(customerId, reservationId, reason)

        // then
        assertTrue(response.success)
        assertEquals("예약이 취소되었습니다", response.message)

        verify { reservationRepository.save(any()) }
    }
} 