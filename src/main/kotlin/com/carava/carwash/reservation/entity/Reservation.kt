package com.carava.carwash.reservation.entity

import com.carava.carwash.car.entity.Car
import com.carava.carwash.car.entity.CarType
import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(
    name = "reservation",
    indexes = [
        Index(name = "idx_reservation_customer_member_id", columnList = "customer_member_id"),
        Index(name = "idx_reservation_store_id", columnList = "store_id"),
        Index(name = "idx_reservation_car_id", columnList = "car_id"),
        Index(name = "idx_reservation_date", columnList = "reservation_date"),
        Index(name = "idx_reservation_status", columnList = "status"),
        Index(name = "idx_reservation_store_date_time", columnList = "store_id, reservation_date, reservation_time", unique = true)
    ]
)
data class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "customer_member_id", nullable = false)
    var customerMemberId: Long,

    @Column(name = "store_id", nullable = false)
    var storeId: Long,

    @Column(name = "car_id", nullable = false)
    var carId: Long,

    @Column(name = "reservation_date", nullable = false)
    var reservationDate: LocalDate,

    @Column(name = "reservation_time", nullable = false)
    var reservationTime: LocalTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.PENDING,

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal,

    @Column(name = "discount_amount", precision = 10, scale = 2)
    var discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    var finalAmount: BigDecimal,

    @Column(name = "customer_request", columnDefinition = "TEXT")
    var customerRequest: String? = null,

    @Column(name = "image_urls", columnDefinition = "jsonb")
    var imageUrls: String? = null,

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    var rejectionReason: String? = null,

    @Column(name = "estimated_duration")
    var estimatedDuration: Int? = null, // 예상 소요시간 (분)

    // ✅ 예약 시점 차량 정보 스냅샷 (불변 데이터)
    @Column(name = "car_brand", nullable = false, length = 50)
    var carBrand: String,

    @Column(name = "car_model", nullable = false, length = 50)
    var carModel: String,

    @Column(name = "car_year", nullable = false)
    var carYear: Int,

    @Column(name = "car_color", length = 30)
    var carColor: String? = null,

    @Column(name = "car_license_plate", nullable = false, length = 20)
    var carLicensePlate: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", nullable = false)
    var carType: CarType,

    @Column(name = "car_display_name", nullable = false, length = 200)
    var carDisplayName: String

) : BaseEntity() {

    fun confirm(): Reservation {
        require(status == ReservationStatus.PENDING) {
            "대기 중인 예약만 확인할 수 있습니다"
        }
        return this.copy(status = ReservationStatus.CONFIRMED)
    }

    fun startService(): Reservation {
        require(status == ReservationStatus.CONFIRMED) {
            "확인된 예약만 시작할 수 있습니다"
        }
        return this.copy(status = ReservationStatus.IN_PROGRESS)
    }

    fun complete(): Reservation {
        require(status == ReservationStatus.IN_PROGRESS) {
            "진행 중인 예약만 완료할 수 있습니다"
        }
        return this.copy(status = ReservationStatus.COMPLETED)
    }

    fun cancel(reason: String? = null): Reservation {
        require(status in listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)) {
            "대기중이거나 확인된 예약만 취소할 수 있습니다"
        }
        
        // 예약 시간 2시간 전까지만 취소 가능
        val reservationDateTime = LocalDateTime.of(reservationDate, reservationTime)
        val now = LocalDateTime.now()
        require(now.isBefore(reservationDateTime.minusHours(2))) {
            "예약 시간 2시간 전까지만 취소할 수 있습니다"
        }
        
        return this.copy(
            status = ReservationStatus.CANCELLED,
            rejectionReason = reason
        )
    }

    fun reject(reason: String): Reservation {
        require(status == ReservationStatus.PENDING) {
            "대기 중인 예약만 거절할 수 있습니다"
        }
        return this.copy(
            status = ReservationStatus.CANCELLED,
            rejectionReason = reason
        )
    }

    fun markAsNoShow(): Reservation {
        require(status in listOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING)) {
            "확인된 예약이나 대기중인 예약만 노쇼 처리할 수 있습니다"
        }
        return this.copy(status = ReservationStatus.NO_SHOW)
    }

    fun isPending(): Boolean = status == ReservationStatus.PENDING
    fun isConfirmed(): Boolean = status == ReservationStatus.CONFIRMED
    fun isInProgress(): Boolean = status == ReservationStatus.IN_PROGRESS
    fun isCompleted(): Boolean = status == ReservationStatus.COMPLETED
    fun isCancelled(): Boolean = status == ReservationStatus.CANCELLED
    fun isNoShow(): Boolean = status == ReservationStatus.NO_SHOW

    fun canBeCancelled(): Boolean {
        if (status !in listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)) {
            return false
        }
        
        val reservationDateTime = LocalDateTime.of(reservationDate, reservationTime)
        val now = LocalDateTime.now()
        return now.isBefore(reservationDateTime.minusHours(2))
    }

    companion object {
        /**
         * 예약 생성 시 차량 정보 스냅샷과 함께 생성
         */
        fun create(
            customerMemberId: Long,
            storeId: Long,
            car: Car,
            reservationDate: LocalDate,
            reservationTime: LocalTime,
            totalAmount: BigDecimal,
            finalAmount: BigDecimal,
            customerRequest: String? = null,
            estimatedDuration: Int? = null
        ): Reservation {
            return Reservation(
                customerMemberId = customerMemberId,
                storeId = storeId,
                carId = car.id,
                reservationDate = reservationDate,
                reservationTime = reservationTime,
                totalAmount = totalAmount,
                finalAmount = finalAmount,
                customerRequest = customerRequest,
                estimatedDuration = estimatedDuration,
                
                // 차량 정보 스냅샷 저장
                carBrand = car.brand,
                carModel = car.model,
                carYear = car.year,
                carColor = car.color,
                carLicensePlate = car.licensePlate,
                carType = car.carType,
                carDisplayName = car.getDisplayName()
            )
        }
    }
} 