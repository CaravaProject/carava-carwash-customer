package com.carava.carwash.car.controller

import com.carava.carwash.car.dto.CarListResponseDto
import com.carava.carwash.car.dto.CarRequestDto
import com.carava.carwash.car.dto.CarResponseDto
import com.carava.carwash.car.entity.CarType
import com.carava.carwash.car.service.CarService
import com.carava.carwash.global.dto.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(CarController::class)
class CarControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var carService: CarService

    private val customerId = 1L

    @Test
    @DisplayName("GET /cars - 차량 목록 조회 성공")
    @WithMockUser(username = "1")
    fun getMyCars_Success() {
        // Given
        val carList = listOf(
            CarListResponseDto(
                id = 1L,
                displayName = "현대 아반떼 (2023년, 화이트)",
                licensePlate = "12가3456",
                carType = CarType.SEDAN,
                isDefault = true
            ),
            CarListResponseDto(
                id = 2L,
                displayName = "기아 스포티지 (2022년)",
                licensePlate = "34나5678",
                carType = CarType.SUV,
                isDefault = false
            )
        )
        val response = ApiResponse.success(carList, "차량 목록 조회가 완료되었습니다")

        every { carService.getMyCars(customerId) } returns response

        // When & Then
        mockMvc.perform(get("/cars"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("차량 목록 조회가 완료되었습니다"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].id").value(1))
            .andExpect(jsonPath("$.data[0].isDefault").value(true))
            .andExpect(jsonPath("$.data[1].id").value(2))
            .andExpect(jsonPath("$.data[1].isDefault").value(false))
    }

    @Test
    @DisplayName("POST /cars - 차량 등록 성공")
    @WithMockUser(username = "1")
    fun createCar_Success() {
        // Given
        val request = CarRequestDto(
            brand = "현대",
            model = "아반떼",
            year = 2023,
            color = "화이트",
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )
        
        val responseData = CarResponseDto(
            id = 1L,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            color = "화이트",
            licensePlate = "12가3456",
            carType = CarType.SEDAN,
            isDefault = true,
            displayName = "현대 아반떼 (2023년, 화이트)",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val response = ApiResponse.success(responseData, "차량이 성공적으로 등록되었습니다")

        every { carService.createCar(customerId, request) } returns response

        // When & Then
        mockMvc.perform(
            post("/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("차량이 성공적으로 등록되었습니다"))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.brand").value("현대"))
            .andExpect(jsonPath("$.data.licensePlate").value("12가3456"))
            .andExpect(jsonPath("$.data.isDefault").value(true))
    }

    @Test
    @DisplayName("POST /cars - 번호판 중복으로 차량 등록 실패")
    @WithMockUser(username = "1")
    fun createCar_DuplicateLicensePlate_Fail() {
        // Given
        val request = CarRequestDto(
            brand = "현대",
            model = "아반떼",
            year = 2023,
            color = "화이트",
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )
        
        val response = ApiResponse.error<CarResponseDto>("LICENSE_PLATE_DUPLICATE", "이미 등록된 번호판입니다")

        every { carService.createCar(customerId, request) } returns response

        // When & Then
        mockMvc.perform(
            post("/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
        )
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("LICENSE_PLATE_DUPLICATE"))
            .andExpect(jsonPath("$.message").value("이미 등록된 번호판입니다"))
    }

    @Test
    @DisplayName("GET /cars/{id} - 차량 상세 조회 성공")
    @WithMockUser(username = "1")
    fun getCarDetail_Success() {
        // Given
        val carId = 1L
        val responseData = CarResponseDto(
            id = carId,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            color = "화이트",
            licensePlate = "12가3456",
            carType = CarType.SEDAN,
            isDefault = true,
            displayName = "현대 아반떼 (2023년, 화이트)",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val response = ApiResponse.success(responseData, "차량 정보 조회가 완료되었습니다")

        every { carService.getCarDetail(customerId, carId) } returns response

        // When & Then
        mockMvc.perform(get("/cars/{id}", carId))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(carId))
            .andExpect(jsonPath("$.data.brand").value("현대"))
    }

    @Test
    @DisplayName("GET /cars/{id} - 차량 없음으로 조회 실패")
    @WithMockUser(username = "1")
    fun getCarDetail_CarNotFound_Fail() {
        // Given
        val carId = 999L
        val response = ApiResponse.error<CarResponseDto>("CAR_NOT_FOUND", "차량을 찾을 수 없습니다")

        every { carService.getCarDetail(customerId, carId) } returns response

        // When & Then
        mockMvc.perform(get("/cars/{id}", carId))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("CAR_NOT_FOUND"))
    }

    @Test
    @DisplayName("PUT /cars/{id} - 차량 정보 수정 성공")
    @WithMockUser(username = "1")
    fun updateCar_Success() {
        // Given
        val carId = 1L
        val request = CarRequestDto(
            brand = "현대",
            model = "아반떼",
            year = 2023,
            color = "블랙", // 색상 변경
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )
        
        val responseData = CarResponseDto(
            id = carId,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            color = "블랙",
            licensePlate = "12가3456",
            carType = CarType.SEDAN,
            isDefault = true,
            displayName = "현대 아반떼 (2023년, 블랙)",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val response = ApiResponse.success(responseData, "차량 정보가 성공적으로 수정되었습니다")

        every { carService.updateCar(customerId, carId, request) } returns response

        // When & Then
        mockMvc.perform(
            put("/cars/{id}", carId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.color").value("블랙"))
            .andExpect(jsonPath("$.message").value("차량 정보가 성공적으로 수정되었습니다"))
    }

    @Test
    @DisplayName("DELETE /cars/{id} - 차량 삭제 성공")
    @WithMockUser(username = "1")
    fun deleteCar_Success() {
        // Given
        val carId = 1L
        val response = ApiResponse.success<Nothing>(null, "차량이 성공적으로 삭제되었습니다")

        every { carService.deleteCar(customerId, carId) } returns response

        // When & Then
        mockMvc.perform(delete("/cars/{id}", carId).with(csrf()))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("차량이 성공적으로 삭제되었습니다"))
    }

    @Test
    @DisplayName("PUT /cars/{id}/default - 기본 차량 설정 성공")
    @WithMockUser(username = "1")
    fun setDefaultCar_Success() {
        // Given
        val carId = 2L
        val responseData = CarResponseDto(
            id = carId,
            brand = "기아",
            model = "스포티지",
            year = 2022,
            color = null,
            licensePlate = "34나5678",
            carType = CarType.SUV,
            isDefault = true, // 기본 차량으로 변경됨
            displayName = "기아 스포티지 (2022년)",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val response = ApiResponse.success(responseData, "기본 차량이 성공적으로 변경되었습니다")

        every { carService.setDefaultCar(customerId, carId) } returns response

        // When & Then
        mockMvc.perform(put("/cars/{id}/default", carId).with(csrf()))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.isDefault").value(true))
            .andExpect(jsonPath("$.message").value("기본 차량이 성공적으로 변경되었습니다"))
    }

    @Test
    @DisplayName("POST /cars - 유효성 검증 실패 (필수 필드 누락)")
    @WithMockUser(username = "1")
    fun createCar_ValidationError_Fail() {
        // Given - brand 필드가 누락된 요청
        val invalidRequest = """
            {
                "model": "아반떼",
                "year": 2023,
                "licensePlate": "12가3456",
                "carType": "SEDAN"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("GET /cars/{id} - 접근 권한 없음")
    @WithMockUser(username = "1")
    fun getCarDetail_AccessDenied_Fail() {
        // Given
        val carId = 1L
        val response = ApiResponse.error<CarResponseDto>("ACCESS_DENIED", "본인 소유의 차량만 조회할 수 있습니다")

        every { carService.getCarDetail(customerId, carId) } returns response

        // When & Then
        mockMvc.perform(get("/cars/{id}", carId))
            .andExpect(status().isForbidden)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
    }
}