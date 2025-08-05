package com.carava.carwash.reservation.controller

import com.carava.carwash.reservation.dto.CreateReservationRequestDto
import com.carava.carwash.reservation.dto.ReservationMenuDto
import com.carava.carwash.reservation.dto.ReservationResponseDto
import com.carava.carwash.reservation.dto.ReservationListResponseDto
import com.carava.carwash.reservation.entity.ReservationStatus
import com.carava.carwash.reservation.service.ReservationService
import com.carava.carwash.global.dto.ApiResponse
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

    // ===== 예약 생성 성공 케이스 =====
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

    // ===== 예약 생성 실패 케이스들 =====
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
    fun `given_past_date_when_create_reservation_then_return_400`() {
        // given
        val customerId = 1L
        val request = CreateReservationRequestDto(
            storeId = 1L,
            carId = 1L,
            reservationDate = LocalDate.now().minusDays(1), // 과거 날짜
            reservationTime = LocalTime.of(10, 0),
            menuIds = listOf(1L)
        )

        every { reservationService.createReservation(customerId, request) } returns 
            ApiResponse.error("INVALID_RESERVATION_DATE", "과거 날짜로 예약할 수 없습니다")

        // when & then
        mockMvc.perform(
            post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("INVALID_RESERVATION_DATE"))
            .andExpect(jsonPath("$.message").value("과거 날짜로 예약할 수 없습니다"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_nonexistent_store_when_create_reservation_then_return_404`() {
        // given
        val customerId = 1L
        val request = CreateReservationRequestDto(
            storeId = 999L, // 존재하지 않는 매장
            carId = 1L,
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            menuIds = listOf(1L)
        )

        every { reservationService.createReservation(customerId, request) } returns 
            ApiResponse.error("STORE_NOT_FOUND", "존재하지 않는 매장입니다")

        // when & then
        mockMvc.perform(
            post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("STORE_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 매장입니다"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_nonexistent_menu_when_create_reservation_then_return_404`() {
        // given
        val customerId = 1L
        val request = CreateReservationRequestDto(
            storeId = 1L,
            carId = 1L,
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            menuIds = listOf(999L) // 존재하지 않는 메뉴
        )

        every { reservationService.createReservation(customerId, request) } returns 
            ApiResponse.error("MENU_NOT_FOUND", "존재하지 않는 메뉴입니다")

        // when & then
        mockMvc.perform(
            post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("MENU_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 메뉴입니다"))
    }

    @Test
    fun `given_unauthenticated_user_when_create_reservation_then_return_401`() {
        // given
        val request = CreateReservationRequestDto(
            storeId = 1L,
            carId = 1L,
            reservationDate = LocalDate.now().plusDays(1),
            reservationTime = LocalTime.of(10, 0),
            menuIds = listOf(1L)
        )

        // when & then (인증되지 않은 사용자)
        mockMvc.perform(
            post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    // ===== 예약 목록 조회 성공 케이스 =====
    @Test
    @WithMockUser(username = "1")
    fun `given_customer_when_get_my_reservations_then_return_200`() {
        // given
        val customerId = 1L
        val pageable = PageRequest.of(0, 10)
        val reservationList = PageImpl<ReservationListResponseDto>(emptyList())

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

    // ===== 예약 목록 조회 실패 케이스들 =====
    @Test
    fun `given_unauthenticated_user_when_get_my_reservations_then_return_401`() {
        // when & then (인증되지 않은 사용자)
        mockMvc.perform(get("/reservations"))
            .andExpect(status().isUnauthorized)
    }

    // ===== 예약 상세 조회 성공 케이스 =====
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

    // ===== 예약 상세 조회 실패 케이스들 =====
    @Test
    @WithMockUser(username = "1")
    fun `given_nonexistent_reservation_when_get_detail_then_return_404`() {
        // given
        val customerId = 1L
        val reservationId = 999L

        every { reservationService.getReservationDetail(customerId, reservationId) } returns 
            ApiResponse.error("RESERVATION_NOT_FOUND", "존재하지 않는 예약입니다")

        // when & then
        mockMvc.perform(get("/reservations/{id}", reservationId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("RESERVATION_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 예약입니다"))
    }

    @Test
    @WithMockUser(username = "2")
    fun `given_other_customer_reservation_when_get_detail_then_return_403`() {
        // given
        val customerId = 2L // 다른 고객
        val reservationId = 1L

        every { reservationService.getReservationDetail(customerId, reservationId) } returns 
            ApiResponse.error("ACCESS_DENIED", "다른 사용자의 예약은 조회할 수 없습니다")

        // when & then
        mockMvc.perform(get("/reservations/{id}", reservationId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
            .andExpect(jsonPath("$.message").value("다른 사용자의 예약은 조회할 수 없습니다"))
    }

    @Test
    fun `given_unauthenticated_user_when_get_reservation_detail_then_return_401`() {
        // when & then (인증되지 않은 사용자)
        mockMvc.perform(get("/reservations/{id}", 1L))
            .andExpect(status().isUnauthorized)
    }

    // ===== 예약 취소 성공 케이스 =====
    @Test
    @WithMockUser(username = "1")
    fun `given_reservation_id_when_cancel_reservation_then_return_200`() {
        // given
        val customerId = 1L
        val reservationId = 1L
        val reason = "일정 변경"

        every { reservationService.cancelReservation(customerId, reservationId, reason) } returns 
            ApiResponse.success(data = null, message = "예약이 취소되었습니다")

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

    // ===== 예약 취소 실패 케이스들 =====
    @Test
    @WithMockUser(username = "1")
    fun `given_nonexistent_reservation_when_cancel_then_return_404`() {
        // given
        val customerId = 1L
        val reservationId = 999L
        val reason = "일정 변경"

        every { reservationService.cancelReservation(customerId, reservationId, reason) } returns 
            ApiResponse.error("RESERVATION_NOT_FOUND", "존재하지 않는 예약입니다")

        // when & then
        mockMvc.perform(
            put("/reservations/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reason": "$reason"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("RESERVATION_NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 예약입니다"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_already_cancelled_reservation_when_cancel_then_return_400`() {
        // given
        val customerId = 1L
        val reservationId = 1L
        val reason = "일정 변경"

        every { reservationService.cancelReservation(customerId, reservationId, reason) } returns 
            ApiResponse.error("ALREADY_CANCELLED", "이미 취소된 예약입니다")

        // when & then
        mockMvc.perform(
            put("/reservations/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reason": "$reason"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("ALREADY_CANCELLED"))
            .andExpect(jsonPath("$.message").value("이미 취소된 예약입니다"))
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_too_late_to_cancel_when_cancel_then_return_400`() {
        // given
        val customerId = 1L
        val reservationId = 1L
        val reason = "일정 변경"

        every { reservationService.cancelReservation(customerId, reservationId, reason) } returns 
            ApiResponse.error("CANCELLATION_TIME_EXPIRED", "예약 시간 1시간 전까지만 취소할 수 있습니다")

        // when & then
        mockMvc.perform(
            put("/reservations/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reason": "$reason"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("CANCELLATION_TIME_EXPIRED"))
            .andExpect(jsonPath("$.message").value("예약 시간 1시간 전까지만 취소할 수 있습니다"))
    }

    @Test
    @WithMockUser(username = "2")
    fun `given_other_customer_reservation_when_cancel_then_return_403`() {
        // given
        val customerId = 2L // 다른 고객
        val reservationId = 1L
        val reason = "일정 변경"

        every { reservationService.cancelReservation(customerId, reservationId, reason) } returns 
            ApiResponse.error("ACCESS_DENIED", "다른 사용자의 예약은 취소할 수 없습니다")

        // when & then
        mockMvc.perform(
            put("/reservations/{id}/cancel", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reason": "$reason"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
            .andExpect(jsonPath("$.message").value("다른 사용자의 예약은 취소할 수 없습니다"))
    }

    @Test
    fun `given_unauthenticated_user_when_cancel_reservation_then_return_401`() {
        // when & then (인증되지 않은 사용자)
        mockMvc.perform(
            put("/reservations/{id}/cancel", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"reason": "일정 변경"}""")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser(username = "1")
    fun `given_malformed_json_when_cancel_reservation_then_return_400`() {
        // when & then
        mockMvc.perform(
            put("/reservations/{id}/cancel", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"invalidJson":}""")
        )
            .andExpect(status().isBadRequest)
    }
} 