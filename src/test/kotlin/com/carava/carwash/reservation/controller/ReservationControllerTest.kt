package com.carava.carwash.reservation.controller

import com.carava.carwash.reservation.dto.CreateReservationRequestDto
import com.carava.carwash.reservation.dto.ReservationMenuDto
import com.carava.carwash.reservation.dto.ReservationResponseDto
import com.carava.carwash.reservation.entity.ReservationStatus
import com.carava.carwash.reservation.service.ReservationService
import com.carava.carwash.shared.dto.ApiResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@WebMvcTest(ReservationController::class)
class ReservationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var reservationService: ReservationService

    @Test
    @WithMockUser(username = "1") // customerId = 1L
    fun `given_valid_reservation_request_when_create_reservation_then_return_201`() {
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

        val responseData = ReservationResponseDto(
            id = 1L,
            storeId = 1L,
            storeName = "깨끗한 세차장",
            carId = 1L,
            carDisplayName = "현대 아반떼 (2023년)",
            reservationDate = request.reservationDate,
            reservationTime = request.reservationTime,
            status = ReservationStatus.PENDING,
            totalAmount = BigDecimal("40000"),
            discountAmount = BigDecimal.ZERO,
            finalAmount = BigDecimal("40000"),
            customerRequest = request.customerRequest,
            rejectionReason = null,
            estimatedDuration = 90,
            menus = listOf(
                ReservationMenuDto(1L, "일반세차", 1, BigDecimal("15000"), BigDecimal("15000")),
                ReservationMenuDto(2L, "프리미엄세차", 1, BigDecimal("25000"), BigDecimal("25000"))
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { reservationService.createReservation(customerId, request) } returns 
            ApiResponse.success(responseData, "예약이 완료되었습니다")

        // when & then
        mockMvc.perform(
            post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.storeName").value("깨끗한 세차장"))
            .andExpect(jsonPath("$.data.carDisplayName").value("현대 아반떼 (2023년)"))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.finalAmount").value(40000))
            .andExpect(jsonPath("$.message").value("예약이 완료되었습니다"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_invalid_request_when_create_reservation_then_return_400`() {
        // given
        val request = CreateReservationRequestDto(
            storeId = 0L, // 잘못된 매장 ID
            carId = 1L,
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            menuIds = emptyList() // 빈 메뉴 리스트
        )

        // when & then
        mockMvc.perform(
            post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_customer_when_get_my_reservations_then_return_200`() {
        // given
        val customerId = 1L
        val pageable = PageRequest.of(0, 10)
        val reservationList = PageImpl(emptyList())

        every { reservationService.getMyReservations(customerId, any()) } returns 
            ApiResponse.success(reservationList, "예약 목록 조회가 완료되었습니다")

        // when & then
        mockMvc.perform(
            get("/reservations")
                .param("page", "0")
                .param("size", "10")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray)
            .andExpect(jsonPath("$.message").value("예약 목록 조회가 완료되었습니다"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_reservation_id_when_get_reservation_detail_then_return_200`() {
        // given
        val customerId = 1L
        val reservationId = 1L
        val responseData = ReservationResponseDto(
            id = reservationId,
            storeId = 1L,
            storeName = "깨끗한 세차장",
            carId = 1L,
            carDisplayName = "현대 아반떼 (2023년)",
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            status = ReservationStatus.PENDING,
            totalAmount = BigDecimal("30000"),
            discountAmount = BigDecimal.ZERO,
            finalAmount = BigDecimal("30000"),
            customerRequest = null,
            rejectionReason = null,
            estimatedDuration = 60,
            menus = listOf(
                ReservationMenuDto(1L, "일반세차", 1, BigDecimal("30000"), BigDecimal("30000"))
            ),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        every { reservationService.getReservationDetail(customerId, reservationId) } returns 
            ApiResponse.success(responseData, "예약 상세 조회가 완료되었습니다")

        // when & then
        mockMvc.perform(get("/reservations/{id}", reservationId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(reservationId))
            .andExpect(jsonPath("$.data.storeName").value("깨끗한 세차장"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_reservation_id_when_cancel_reservation_then_return_200`() {
        // given
        val customerId = 1L
        val reservationId = 1L
        val reason = "일정 변경"

        every { reservationService.cancelReservation(customerId, reservationId, reason) } returns 
            ApiResponse.success("예약이 취소되었습니다")

        // when & then
        mockMvc.perform(
            put("/reservations/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reason": "$reason"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("예약이 취소되었습니다"))
    }
} 