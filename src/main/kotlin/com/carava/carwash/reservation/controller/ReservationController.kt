package com.carava.carwash.reservation.controller

import com.carava.carwash.reservation.dto.CreateReservationRequestDto
import com.carava.carwash.reservation.dto.ReservationListResponseDto
import com.carava.carwash.reservation.dto.ReservationResponseDto
import com.carava.carwash.reservation.service.ReservationService
import com.carava.carwash.global.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/reservations")
@Tag(name = "예약", description = "고객 예약 관리 API")
class ReservationController(
    private val reservationService: ReservationService
) {

    @PostMapping
    @Operation(
        summary = "예약 생성",
        description = "새로운 예약을 생성합니다"
    )
    @SwaggerApiResponse(responseCode = "201", description = "예약 생성 성공")
    @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    fun createReservation(
        @Valid @RequestBody request: CreateReservationRequestDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ReservationResponseDto>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = reservationService.createReservation(customerId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @Operation(
        summary = "내 예약 목록 조회",
        description = "고객의 예약 목록을 페이징으로 조회합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "조회 성공")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    fun getMyReservations(
        @PageableDefault(size = 10) pageable: Pageable,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Page<ReservationListResponseDto>>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = reservationService.getMyReservations(customerId, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "예약 상세 조회",
        description = "특정 예약의 상세 정보를 조회합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "조회 성공")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    @SwaggerApiResponse(responseCode = "403", description = "권한 없음")
    @SwaggerApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    fun getReservationDetail(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<ReservationResponseDto>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = reservationService.getReservationDetail(customerId, id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}/cancel")
    @Operation(
        summary = "예약 취소",
        description = "예약을 취소합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "취소 성공")
    @SwaggerApiResponse(responseCode = "400", description = "취소할 수 없는 예약")
    @SwaggerApiResponse(responseCode = "401", description = "인증 실패")
    @SwaggerApiResponse(responseCode = "403", description = "권한 없음")
    @SwaggerApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    fun cancelReservation(
        @PathVariable id: Long,
        @RequestBody request: CancelReservationRequestDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Nothing>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = reservationService.cancelReservation(customerId, id, request.reason)
        return ResponseEntity.ok(response)
    }

    private fun getCustomerIdFromAuth(authentication: Authentication): Long {
        // JWT에서 customerId 추출 (임시로 username을 사용)
        // 실제로는 JWT에서 customerId를 추출해야 함
        return authentication.name.toLongOrNull() 
            ?: throw IllegalArgumentException("유효하지 않은 인증 정보입니다")
    }

    data class CancelReservationRequestDto(
        val reason: String?
    )
} 