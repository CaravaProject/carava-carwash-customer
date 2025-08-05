package com.carava.carwash.car.controller

import com.carava.carwash.car.dto.CarListResponseDto
import com.carava.carwash.car.dto.CarRequestDto
import com.carava.carwash.car.dto.CarResponseDto
import com.carava.carwash.car.service.CarService
import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.global.dto.ErrorResponse
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
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cars")
@Tag(name = "차량 관리", description = "고객 차량 관리 API")
class CarController(
    private val carService: CarService
) {

    @GetMapping
    @Operation(
        summary = "내 차량 목록 조회",
        description = "로그인한 고객의 차량 목록을 조회합니다. 기본 차량이 먼저 표시됩니다."
    )
    @SwaggerApiResponse(
        responseCode = "200", 
        description = "조회 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(
                value = """
                {
                    "success": true,
                    "data": [
                        {
                            "id": 1,
                            "displayName": "현대 아반떼 (2023년, 화이트)",
                            "licensePlate": "12가3456",
                            "carType": "SEDAN",
                            "isDefault": true
                        },
                        {
                            "id": 2,
                            "displayName": "기아 스포티지 (2022년)",
                            "licensePlate": "34나5678",
                            "carType": "SUV",
                            "isDefault": false
                        }
                    ],
                    "message": "차량 목록 조회가 완료되었습니다"
                }
                """
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "401", 
        description = "인증 실패",
        content = [Content(
            schema = Schema(implementation = ErrorResponse::class)
        )]
    )
    fun getMyCars(
        authentication: Authentication
    ): ResponseEntity<ApiResponse<List<CarListResponseDto>>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = carService.getMyCars(customerId)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    @Operation(
        summary = "차량 등록",
        description = "새로운 차량을 등록합니다. 첫 번째 등록 차량은 자동으로 기본 차량으로 설정됩니다."
    )
    @SwaggerApiResponse(
        responseCode = "201", 
        description = "등록 성공",
        content = [Content(
            mediaType = "application/json",
            examples = [ExampleObject(
                value = """
                {
                    "success": true,
                    "data": {
                        "id": 1,
                        "brand": "현대",
                        "model": "아반떼",
                        "year": 2023,
                        "color": "화이트",
                        "licensePlate": "12가3456", 
                        "carType": "SEDAN",
                        "isDefault": true,
                        "displayName": "현대 아반떼 (2023년, 화이트)",
                        "createdAt": "2024-01-01T10:00:00",
                        "updatedAt": "2024-01-01T10:00:00"
                    },
                    "message": "차량이 성공적으로 등록되었습니다"
                }
                """
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "400", 
        description = "잘못된 요청",
        content = [Content(
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(
                name = "유효성 검증 실패",
                value = """{"success": false, "message": "입력값이 올바르지 않습니다", "errorCode": "VALIDATION_ERROR"}"""
            )]
        )]
    )
    @SwaggerApiResponse(
        responseCode = "409", 
        description = "번호판 중복",
        content = [Content(
            schema = Schema(implementation = ErrorResponse::class),
            examples = [ExampleObject(
                value = """{"success": false, "message": "이미 등록된 번호판입니다", "errorCode": "LICENSE_PLATE_DUPLICATE"}"""
            )]
        )]
    )
    fun createCar(
        @Valid @RequestBody request: CarRequestDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CarResponseDto>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = carService.createCar(customerId, request)
        
        val status = if (response.success) HttpStatus.CREATED else HttpStatus.CONFLICT
        return ResponseEntity.status(status).body(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "차량 상세 조회",
        description = "특정 차량의 상세 정보를 조회합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "조회 성공")
    @SwaggerApiResponse(responseCode = "403", description = "접근 권한 없음")
    @SwaggerApiResponse(responseCode = "404", description = "차량을 찾을 수 없음")
    fun getCarDetail(
        @PathVariable @Parameter(description = "차량 ID", example = "1") id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CarResponseDto>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = carService.getCarDetail(customerId, id)
        
        val status = when {
            response.success -> HttpStatus.OK
            response.errorCode == "CAR_NOT_FOUND" -> HttpStatus.NOT_FOUND
            response.errorCode == "ACCESS_DENIED" -> HttpStatus.FORBIDDEN
            else -> HttpStatus.BAD_REQUEST
        }
        return ResponseEntity.status(status).body(response)
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "차량 정보 수정",
        description = "차량 정보를 수정합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "수정 성공")
    @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청")
    @SwaggerApiResponse(responseCode = "403", description = "접근 권한 없음")
    @SwaggerApiResponse(responseCode = "404", description = "차량을 찾을 수 없음")
    @SwaggerApiResponse(responseCode = "409", description = "번호판 중복")
    fun updateCar(
        @PathVariable @Parameter(description = "차량 ID", example = "1") id: Long,
        @Valid @RequestBody request: CarRequestDto,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CarResponseDto>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = carService.updateCar(customerId, id, request)
        
        val status = when {
            response.success -> HttpStatus.OK
            response.errorCode == "CAR_NOT_FOUND" -> HttpStatus.NOT_FOUND
            response.errorCode == "ACCESS_DENIED" -> HttpStatus.FORBIDDEN
            response.errorCode == "LICENSE_PLATE_DUPLICATE" -> HttpStatus.CONFLICT
            else -> HttpStatus.BAD_REQUEST
        }
        return ResponseEntity.status(status).body(response)
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "차량 삭제",
        description = "차량을 삭제합니다. 기본 차량 삭제 시 다른 차량이 자동으로 기본 차량으로 설정됩니다."
    )
    @SwaggerApiResponse(responseCode = "200", description = "삭제 성공")
    @SwaggerApiResponse(responseCode = "403", description = "접근 권한 없음")
    @SwaggerApiResponse(responseCode = "404", description = "차량을 찾을 수 없음")
    fun deleteCar(
        @PathVariable @Parameter(description = "차량 ID", example = "1") id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<Nothing>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = carService.deleteCar(customerId, id)
        
        val status = when {
            response.success -> HttpStatus.OK
            response.errorCode == "CAR_NOT_FOUND" -> HttpStatus.NOT_FOUND
            response.errorCode == "ACCESS_DENIED" -> HttpStatus.FORBIDDEN
            else -> HttpStatus.BAD_REQUEST
        }
        return ResponseEntity.status(status).body(response)
    }

    @PutMapping("/{id}/default")
    @Operation(
        summary = "기본 차량 설정",
        description = "해당 차량을 기본 차량으로 설정합니다"
    )
    @SwaggerApiResponse(responseCode = "200", description = "설정 성공")
    @SwaggerApiResponse(responseCode = "403", description = "접근 권한 없음")
    @SwaggerApiResponse(responseCode = "404", description = "차량을 찾을 수 없음")
    fun setDefaultCar(
        @PathVariable @Parameter(description = "차량 ID", example = "1") id: Long,
        authentication: Authentication
    ): ResponseEntity<ApiResponse<CarResponseDto>> {
        val customerId = getCustomerIdFromAuth(authentication)
        val response = carService.setDefaultCar(customerId, id)
        
        val status = when {
            response.success -> HttpStatus.OK
            response.errorCode == "CAR_NOT_FOUND" -> HttpStatus.NOT_FOUND
            response.errorCode == "ACCESS_DENIED" -> HttpStatus.FORBIDDEN
            else -> HttpStatus.BAD_REQUEST
        }
        return ResponseEntity.status(status).body(response)
    }

    private fun getCustomerIdFromAuth(authentication: Authentication): Long {
        // JWT에서 customerId 추출 (임시로 username을 사용)
        // 실제로는 JWT에서 customerId를 추출해야 함
        return authentication.name.toLongOrNull() 
            ?: throw IllegalArgumentException("유효하지 않은 인증 정보입니다")
    }
}