package com.carava.carwash.car.service

import com.carava.carwash.car.dto.CarListResponseDto
import com.carava.carwash.car.dto.CarRequestDto
import com.carava.carwash.car.dto.CarResponseDto
import com.carava.carwash.car.entity.Car
import com.carava.carwash.car.repository.CarRepository
import com.carava.carwash.global.dto.ApiResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CarService(
    private val carRepository: CarRepository
) {

    /**
     * 차량 등록
     */
    @Transactional
    fun createCar(customerId: Long, request: CarRequestDto): ApiResponse<CarResponseDto> {
        return try {
            // 1. 번호판 중복 검증
            if (carRepository.existsByCustomerMemberIdAndLicensePlate(customerId, request.licensePlate)) {
                return ApiResponse.error("LICENSE_PLATE_DUPLICATE", "이미 등록된 번호판입니다")
            }

            // 2. 고객의 기존 차량 개수 확인
            val existingCars = carRepository.findByCustomerMemberId(customerId)
            val isFirstCar = existingCars.isEmpty()

            // 3. 차량 생성
            val car = Car(
                customerMemberId = customerId,
                brand = request.brand,
                model = request.model,
                year = request.year,
                color = request.color,
                licensePlate = request.licensePlate,
                carType = request.carType,
                isDefault = isFirstCar // 첫 번째 차량은 자동으로 기본 차량
            )

            val savedCar = carRepository.save(car)
            val responseDto = CarResponseDto.from(savedCar)

            ApiResponse.success(responseDto, "차량이 성공적으로 등록되었습니다")
        } catch (e: Exception) {
            ApiResponse.error("INTERNAL_ERROR", "차량 등록 중 오류가 발생했습니다")
        }
    }

    /**
     * 내 차량 목록 조회
     */
    fun getMyCars(customerId: Long): ApiResponse<List<CarListResponseDto>> {
        return try {
            val cars = carRepository.findByCustomerMemberId(customerId)
            val carDtos = cars.map { CarListResponseDto.from(it) }
                .sortedWith(compareByDescending<CarListResponseDto> { it.isDefault }
                    .thenBy { it.id })

            ApiResponse.success(carDtos, "차량 목록 조회가 완료되었습니다")
        } catch (e: Exception) {
            ApiResponse.error("INTERNAL_ERROR", "차량 목록 조회 중 오류가 발생했습니다")
        }
    }

    /**
     * 차량 상세 조회
     */
    fun getCarDetail(customerId: Long, carId: Long): ApiResponse<CarResponseDto> {
        return try {
            val car = carRepository.findById(carId)
                .orElse(null) ?: return ApiResponse.error("CAR_NOT_FOUND", "차량을 찾을 수 없습니다")

            // 소유권 검증
            if (car.customerMemberId != customerId) {
                return ApiResponse.error("ACCESS_DENIED", "본인 소유의 차량만 조회할 수 있습니다")
            }

            val responseDto = CarResponseDto.from(car)
            ApiResponse.success(responseDto, "차량 정보 조회가 완료되었습니다")
        } catch (e: Exception) {
            ApiResponse.error("INTERNAL_ERROR", "차량 정보 조회 중 오류가 발생했습니다")
        }
    }

    /**
     * 차량 정보 수정
     */
    @Transactional
    fun updateCar(customerId: Long, carId: Long, request: CarRequestDto): ApiResponse<CarResponseDto> {
        return try {
            val car = carRepository.findById(carId)
                .orElse(null) ?: return ApiResponse.error("CAR_NOT_FOUND", "차량을 찾을 수 없습니다")

            // 소유권 검증
            if (car.customerMemberId != customerId) {
                return ApiResponse.error("ACCESS_DENIED", "본인 소유의 차량만 수정할 수 있습니다")
            }

            // 번호판 변경 시 중복 검증
            if (car.licensePlate != request.licensePlate) {
                if (carRepository.existsByCustomerMemberIdAndLicensePlate(customerId, request.licensePlate)) {
                    return ApiResponse.error("LICENSE_PLATE_DUPLICATE", "이미 등록된 번호판입니다")
                }
            }

            // 차량 정보 업데이트
            val updatedCar = car.copy(
                brand = request.brand,
                model = request.model,
                year = request.year,
                color = request.color,
                licensePlate = request.licensePlate,
                carType = request.carType
            )

            val savedCar = carRepository.save(updatedCar)
            val responseDto = CarResponseDto.from(savedCar)

            ApiResponse.success(responseDto, "차량 정보가 성공적으로 수정되었습니다")
        } catch (e: Exception) {
            ApiResponse.error("INTERNAL_ERROR", "차량 정보 수정 중 오류가 발생했습니다")
        }
    }

    /**
     * 차량 삭제
     */
    @Transactional
    fun deleteCar(customerId: Long, carId: Long): ApiResponse<Nothing> {
        return try {
            val car = carRepository.findById(carId)
                .orElse(null) ?: return ApiResponse.error("CAR_NOT_FOUND", "차량을 찾을 수 없습니다")

            // 소유권 검증
            if (car.customerMemberId != customerId) {
                return ApiResponse.error("ACCESS_DENIED", "본인 소유의 차량만 삭제할 수 있습니다")
            }

            // 기본 차량 삭제 시 다른 차량을 기본으로 설정
            if (car.isDefault) {
                val otherCars = carRepository.findByCustomerMemberId(customerId)
                    .filter { it.id != carId }
                
                if (otherCars.isNotEmpty()) {
                    // 가장 오래된 차량을 새로운 기본 차량으로 설정
                    val newDefaultCar = otherCars.minByOrNull { it.id }!!
                    newDefaultCar.setAsDefault()
                    carRepository.save(newDefaultCar)
                }
            }

            carRepository.delete(car)
            ApiResponse.success(null, "차량이 성공적으로 삭제되었습니다")
        } catch (e: Exception) {
            ApiResponse.error("INTERNAL_ERROR", "차량 삭제 중 오류가 발생했습니다")
        }
    }

    /**
     * 기본 차량 설정
     */
    @Transactional
    fun setDefaultCar(customerId: Long, carId: Long): ApiResponse<CarResponseDto> {
        return try {
            val car = carRepository.findById(carId)
                .orElse(null) ?: return ApiResponse.error("CAR_NOT_FOUND", "차량을 찾을 수 없습니다")

            // 소유권 검증
            if (car.customerMemberId != customerId) {
                return ApiResponse.error("ACCESS_DENIED", "본인 소유의 차량만 기본 차량으로 설정할 수 있습니다")
            }

            // 이미 기본 차량인 경우
            if (car.isDefault) {
                val responseDto = CarResponseDto.from(car)
                return ApiResponse.success(responseDto, "이미 기본 차량으로 설정되어 있습니다")
            }

            // 기존 기본 차량 해제
            carRepository.unsetAllDefaultCars(customerId)

            // 새로운 기본 차량 설정
            car.setAsDefault()
            val savedCar = carRepository.save(car)
            val responseDto = CarResponseDto.from(savedCar)

            ApiResponse.success(responseDto, "기본 차량이 성공적으로 변경되었습니다")
        } catch (e: Exception) {
            ApiResponse.error("INTERNAL_ERROR", "기본 차량 설정 중 오류가 발생했습니다")
        }
    }
}