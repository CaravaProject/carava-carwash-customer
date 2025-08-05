package com.carava.carwash.reservation.controller

import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.global.dto.ErrorResponse
import com.carava.carwash.reservation.dto.*
import com.carava.carwash.reservation.service.AvailableTimeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stores/{storeId}")
@Tag(name = "예약 가능 시간", description = "예약 가능한 시간대 조회 API")
class AvailableTimeController(
    private val availableTimeService: AvailableTimeService
) {

    @PostMapping("/available-times")
    @Operation(
        summary = "예약 가능 시간 조회",
        description = "선택한 메뉴들과 날짜를 기반으로 예약 가능한 시간대를 30분 단위로 조회합니다"
    )
    @SwaggerApiResponse(
        responseCode = "200", 
        description = "예약 가능 시간 조회 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(
                name = "성공 응답",
                value = """
                {
                    "success": true,
                    "data": {
                        "date": "2024-08-05",
                        "storeName": "카라바 세차장",
                        "openTime": "09:00",
                        "closeTime": "18:00",
                        "totalDuration": 90,
                        "availableSlots": [
                            {
                                "startTime": "09:00",
                                "endTime": "10:30",
                                "isAvailable": true,
                                "unavailableReason": null
                            },
                            {
                                "startTime": "09:30",
                                "endTime": "11:00",
                                "isAvailable": false,
                                "unavailableReason": "이미 예약된 시간입니다"
                            }
                        ]
                    },
                    "message": "예약 가능 시간 조회 완료"
                }
                """
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "400", 
        description = "잘못된 요청",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(
                name = "과거 날짜 선택",
                value = """{"success": false, "data": null, "message": "과거 날짜는 예약할 수 없습니다", "code": null, "errorCode": "INVALID_DATE"}"""
            ), ExampleObject(
                name = "메뉴 없음",
                value = """{"success": false, "data": null, "message": "최소 하나의 메뉴를 선택해야 합니다", "code": null, "errorCode": "VALIDATION_ERROR"}"""
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "404", 
        description = "매장 또는 메뉴를 찾을 수 없음",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(
                name = "매장 없음",
                value = """{"success": false, "data": null, "message": "매장을 찾을 수 없습니다", "code": null, "errorCode": "STORE_NOT_FOUND"}"""
            ), ExampleObject(
                name = "메뉴 없음",
                value = """{"success": false, "data": null, "message": "존재하지 않는 메뉴가 포함되어 있습니다", "code": null, "errorCode": "MENU_NOT_FOUND"}"""
            )]
        )]
    )
    fun getAvailableTimes(
        @PathVariable @Parameter(description = "매장 ID", example = "1") storeId: Long,
        @Valid @RequestBody request: AvailableTimeRequestDto
    ): ResponseEntity<ApiResponse<AvailableTimeResponseDto>> {
        val response = availableTimeService.getAvailableTimes(storeId, request)
        
        val httpStatus = when {
            response.success -> HttpStatus.OK
            response.errorCode == "STORE_NOT_FOUND" || response.errorCode == "MENU_NOT_FOUND" -> HttpStatus.NOT_FOUND
            else -> HttpStatus.BAD_REQUEST
        }
        
        return ResponseEntity.status(httpStatus).body(response)
    }

    @GetMapping("/menus")
    @Operation(
        summary = "매장 메뉴 목록 조회",
        description = "매장의 활성 메뉴 목록을 조회합니다 (예약 시 선택 가능한 메뉴들)"
    )
    @SwaggerApiResponse(
        responseCode = "200", 
        description = "메뉴 목록 조회 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(
                name = "성공 응답",
                value = """
                {
                    "success": true,
                    "data": [
                        {
                            "menuId": 1,
                            "menuName": "기본 세차",
                            "duration": 60,
                            "price": 15000
                        },
                        {
                            "menuId": 2,
                            "menuName": "프리미엄 세차",
                            "duration": 90,
                            "price": 25000
                        }
                    ],
                    "message": "매장 메뉴 목록 조회 완료"
                }
                """
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "404", 
        description = "매장을 찾을 수 없음",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(
                name = "매장 없음",
                value = """{"success": false, "data": null, "message": "매장을 찾을 수 없습니다", "code": null, "errorCode": "STORE_NOT_FOUND"}"""
            )]
        )]
    )
    fun getStoreMenus(
        @PathVariable @Parameter(description = "매장 ID", example = "1") storeId: Long
    ): ResponseEntity<ApiResponse<List<SelectedMenuSummaryDto>>> {
        val response = availableTimeService.getStoreMenus(storeId)
        
        val httpStatus = if (response.success) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity.status(httpStatus).body(response)
    }
} 