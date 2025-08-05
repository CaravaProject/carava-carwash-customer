package com.carava.carwash.car.service

import com.carava.carwash.car.dto.CarRequestDto
import com.carava.carwash.car.entity.Car
import com.carava.carwash.car.entity.CarType
import com.carava.carwash.car.repository.CarRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.BeforeEach
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CarServiceTest {

    private val carRepository = mockk<CarRepository>()
    private val carService = CarService(carRepository)

    private val customerId = 1L
    private lateinit var sampleRequest: CarRequestDto
    private lateinit var sampleCar: Car

    @BeforeEach
    fun setUp() {
        sampleRequest = CarRequestDto(
            brand = "현대",
            model = "아반떼",
            year = 2023,
            color = "화이트",
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )

        sampleCar = Car(
            id = 1L,
            customerMemberId = customerId,
            brand = sampleRequest.brand,
            model = sampleRequest.model,
            year = sampleRequest.year,
            color = sampleRequest.color,
            licensePlate = sampleRequest.licensePlate,
            carType = sampleRequest.carType,
            isDefault = true
        )
    }

    @Test
    @DisplayName("차량 등록 - 첫 번째 차량 (자동으로 기본 차량 설정)")
    fun createCar_FirstCar_Success() {
        // Given
        every { carRepository.existsByCustomerMemberIdAndLicensePlate(customerId, sampleRequest.licensePlate) } returns false
        every { carRepository.findByCustomerMemberId(customerId) } returns emptyList()
        every { carRepository.save(any()) } returns sampleCar

        // When
        val result = carService.createCar(customerId, sampleRequest)

        // Then
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("차량이 성공적으로 등록되었습니다", result.message)
        assertEquals(sampleRequest.brand, result.data?.brand)
        assertEquals(sampleRequest.licensePlate, result.data?.licensePlate)
        assertTrue(result.data?.isDefault == true) // 첫 번째 차량은 기본 차량

        verify { carRepository.save(any()) }
    }

    @Test
    @DisplayName("차량 등록 - 두 번째 차량 (기본 차량 아님)")
    fun createCar_SecondCar_Success() {
        // Given
        val existingCar = sampleCar.copy(id = 2L)
        val newCar = sampleCar.copy(id = 3L, licensePlate = "34나5678", isDefault = false)

        every { carRepository.existsByCustomerMemberIdAndLicensePlate(customerId, "34나5678") } returns false
        every { carRepository.findByCustomerMemberId(customerId) } returns listOf(existingCar)
        every { carRepository.save(any()) } returns newCar

        val newRequest = sampleRequest.copy(licensePlate = "34나5678")

        // When
        val result = carService.createCar(customerId, newRequest)

        // Then
        assertTrue(result.success)
        assertNotNull(result.data)
        assertFalse(result.data?.isDefault == true) // 두 번째 차량은 기본 차량이 아님
    }

    @Test
    @DisplayName("차량 등록 - 번호판 중복 에러")
    fun createCar_DuplicateLicensePlate_Fail() {
        // Given
        every { carRepository.existsByCustomerMemberIdAndLicensePlate(customerId, sampleRequest.licensePlate) } returns true

        // When
        val result = carService.createCar(customerId, sampleRequest)

        // Then
        assertFalse(result.success)
        assertEquals("LICENSE_PLATE_DUPLICATE", result.errorCode)
        assertEquals("이미 등록된 번호판입니다", result.message)
    }

    @Test
    @DisplayName("내 차량 목록 조회 - 성공")
    fun getMyCars_Success() {
        // Given
        val car1 = sampleCar.copy(id = 1L, isDefault = true)
        val car2 = sampleCar.copy(id = 2L, licensePlate = "34나5678", isDefault = false)
        val cars = listOf(car1, car2)

        every { carRepository.findByCustomerMemberId(customerId) } returns cars

        // When
        val result = carService.getMyCars(customerId)

        // Then
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(2, result.data?.size)
        assertEquals("차량 목록 조회가 완료되었습니다", result.message)
        
        // 기본 차량이 먼저 와야 함
        assertTrue(result.data?.first()?.isDefault == true)
    }

    @Test
    @DisplayName("내 차량 목록 조회 - 차량 없음")
    fun getMyCars_EmptyList_Success() {
        // Given
        every { carRepository.findByCustomerMemberId(customerId) } returns emptyList()

        // When
        val result = carService.getMyCars(customerId)

        // Then
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(0, result.data?.size)
    }

    @Test
    @DisplayName("차량 상세 조회 - 성공")
    fun getCarDetail_Success() {
        // Given
        every { carRepository.findById(sampleCar.id) } returns Optional.of(sampleCar)

        // When
        val result = carService.getCarDetail(customerId, sampleCar.id)

        // Then
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("차량 정보 조회가 완료되었습니다", result.message)
        assertEquals(sampleCar.id, result.data?.id)
    }

    @Test
    @DisplayName("차량 상세 조회 - 차량 없음")
    fun getCarDetail_CarNotFound_Fail() {
        // Given
        every { carRepository.findById(999L) } returns Optional.empty()

        // When
        val result = carService.getCarDetail(customerId, 999L)

        // Then
        assertFalse(result.success)
        assertEquals("CAR_NOT_FOUND", result.errorCode)
        assertEquals("차량을 찾을 수 없습니다", result.message)
    }

    @Test
    @DisplayName("차량 상세 조회 - 접근 권한 없음")
    fun getCarDetail_AccessDenied_Fail() {
        // Given
        val otherUserCar = sampleCar.copy(customerMemberId = 999L)
        every { carRepository.findById(sampleCar.id) } returns Optional.of(otherUserCar)

        // When
        val result = carService.getCarDetail(customerId, sampleCar.id)

        // Then
        assertFalse(result.success)
        assertEquals("ACCESS_DENIED", result.errorCode)
        assertEquals("본인 소유의 차량만 조회할 수 있습니다", result.message)
    }

    @Test
    @DisplayName("차량 정보 수정 - 성공")
    fun updateCar_Success() {
        // Given
        val updateRequest = sampleRequest.copy(color = "블랙", licensePlate = "56다7890")
        val updatedCar = sampleCar.copy(color = "블랙", licensePlate = "56다7890")

        every { carRepository.findById(sampleCar.id) } returns Optional.of(sampleCar)
        every { carRepository.existsByCustomerMemberIdAndLicensePlate(customerId, "56다7890") } returns false
        every { carRepository.save(any()) } returns updatedCar

        // When
        val result = carService.updateCar(customerId, sampleCar.id, updateRequest)

        // Then
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("차량 정보가 성공적으로 수정되었습니다", result.message)
        assertEquals("블랙", result.data?.color)
        assertEquals("56다7890", result.data?.licensePlate)
    }

    @Test
    @DisplayName("차량 정보 수정 - 번호판 중복")
    fun updateCar_DuplicateLicensePlate_Fail() {
        // Given
        val updateRequest = sampleRequest.copy(licensePlate = "78라9012")

        every { carRepository.findById(sampleCar.id) } returns Optional.of(sampleCar)
        every { carRepository.existsByCustomerMemberIdAndLicensePlate(customerId, "78라9012") } returns true

        // When
        val result = carService.updateCar(customerId, sampleCar.id, updateRequest)

        // Then
        assertFalse(result.success)
        assertEquals("LICENSE_PLATE_DUPLICATE", result.errorCode)
        assertEquals("이미 등록된 번호판입니다", result.message)
    }

    @Test
    @DisplayName("차량 삭제 - 일반 차량 삭제")
    fun deleteCar_NonDefaultCar_Success() {
        // Given
        val nonDefaultCar = sampleCar.copy(isDefault = false)
        every { carRepository.findById(sampleCar.id) } returns Optional.of(nonDefaultCar)
        every { carRepository.delete(nonDefaultCar) } returns Unit

        // When
        val result = carService.deleteCar(customerId, sampleCar.id)

        // Then
        assertTrue(result.success)
        assertEquals("차량이 성공적으로 삭제되었습니다", result.message)
        verify { carRepository.delete(nonDefaultCar) }
    }

    @Test
    @DisplayName("차량 삭제 - 기본 차량 삭제 (다른 차량을 기본으로 설정)")
    fun deleteCar_DefaultCar_SetNewDefault_Success() {
        // Given
        val defaultCar = sampleCar.copy(id = 1L, isDefault = true)
        val otherCar = sampleCar.copy(id = 2L, isDefault = false, licensePlate = "34나5678")
        
        every { carRepository.findById(1L) } returns Optional.of(defaultCar)
        every { carRepository.findByCustomerMemberId(customerId) } returns listOf(defaultCar, otherCar)
        every { carRepository.save(any()) } returns otherCar.copy(isDefault = true)
        every { carRepository.delete(defaultCar) } returns Unit

        // When
        val result = carService.deleteCar(customerId, 1L)

        // Then
        assertTrue(result.success)
        assertEquals("차량이 성공적으로 삭제되었습니다", result.message)
        verify { carRepository.save(any()) } // 다른 차량을 기본으로 설정
        verify { carRepository.delete(defaultCar) }
    }

    @Test
    @DisplayName("기본 차량 설정 - 성공")
    fun setDefaultCar_Success() {
        // Given
        val nonDefaultCar = sampleCar.copy(isDefault = false)
        val updatedCar = nonDefaultCar.copy(isDefault = true)

        every { carRepository.findById(sampleCar.id) } returns Optional.of(nonDefaultCar)
        every { carRepository.unsetAllDefaultCars(customerId) } returns Unit
        every { carRepository.save(any()) } returns updatedCar

        // When
        val result = carService.setDefaultCar(customerId, sampleCar.id)

        // Then
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals("기본 차량이 성공적으로 변경되었습니다", result.message)
        assertTrue(result.data?.isDefault == true)

        verify { carRepository.unsetAllDefaultCars(customerId) }
        verify { carRepository.save(any()) }
    }

    @Test
    @DisplayName("기본 차량 설정 - 이미 기본 차량인 경우")
    fun setDefaultCar_AlreadyDefault_Success() {
        // Given
        val defaultCar = sampleCar.copy(isDefault = true)
        every { carRepository.findById(sampleCar.id) } returns Optional.of(defaultCar)

        // When
        val result = carService.setDefaultCar(customerId, sampleCar.id)

        // Then
        assertTrue(result.success)
        assertEquals("이미 기본 차량으로 설정되어 있습니다", result.message)
        
        // unsetAllDefaultCars가 호출되지 않아야 함
        verify(exactly = 0) { carRepository.unsetAllDefaultCars(any()) }
    }
}