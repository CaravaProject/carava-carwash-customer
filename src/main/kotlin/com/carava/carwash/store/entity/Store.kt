package com.carava.carwash.store.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalTime

@Entity
@Table(
    name = "store",
    indexes = [
        Index(name = "idx_store_owner_member_id", columnList = "owner_member_id"),
        Index(name = "idx_store_category_status", columnList = "category, status"),
        Index(name = "idx_store_average_rating", columnList = "average_rating")
    ]
)
data class Store(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "owner_member_id", nullable = false)
    var ownerMemberId: Long,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(length = 20)
    var phone: String? = null,

    @Column(name = "address_id", nullable = false)
    var addressId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: StoreCategory = StoreCategory.CAR_WASH,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StoreStatus = StoreStatus.ACTIVE,

    @Column(name = "average_rating", precision = 2, scale = 1)
    var averageRating: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_reviews")
    var totalReviews: Int = 0,

    @Column(name = "view_count")
    var viewCount: Int = 0,

    @Column(name = "favorite_count")
    var favoriteCount: Int = 0,

    @Column(name = "open_time", nullable = false)
    var openTime: LocalTime = LocalTime.of(9, 0), // 기본 오전 9시

    @Column(name = "close_time", nullable = false)
    var closeTime: LocalTime = LocalTime.of(18, 0), // 기본 오후 6시

    @Column(name = "break_start_time")
    var breakStartTime: LocalTime? = null, // 휴게시간 시작 (선택사항)

    @Column(name = "break_end_time")
    var breakEndTime: LocalTime? = null, // 휴게시간 종료 (선택사항)

    @Column(name = "is_24_hours", nullable = false)
    var is24Hours: Boolean = false // 24시간 운영 여부

) : BaseEntity() {

    fun isActive(): Boolean = status == StoreStatus.ACTIVE

    fun activate() {
        this.status = StoreStatus.ACTIVE
    }

    fun deactivate() {
        this.status = StoreStatus.INACTIVE
    }

    fun suspend() {
        this.status = StoreStatus.SUSPENDED
    }

    fun increaseViewCount() {
        this.viewCount++
    }

    fun updateRating(newRating: BigDecimal, isNewReview: Boolean) {
        if (isNewReview) {
            val totalRating = averageRating.multiply(BigDecimal(totalReviews)).add(newRating)
            totalReviews++
            averageRating = totalRating.divide(BigDecimal(totalReviews), 1, BigDecimal.ROUND_HALF_UP)
        }
    }

    fun increaseFavoriteCount() {
        this.favoriteCount++
    }

    fun decreaseFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--
        }
    }

    /**
     * 현재 시간이 영업시간인지 확인
     */
    fun isOpenAt(time: LocalTime): Boolean {
        if (is24Hours) return true
        
        return if (breakStartTime != null && breakEndTime != null) {
            // 휴게시간이 있는 경우
            (time >= openTime && time < breakStartTime) || 
            (time >= breakEndTime && time < closeTime)
        } else {
            // 휴게시간이 없는 경우
            time >= openTime && time < closeTime
        }
    }

    /**
     * 영업시간 내에서 예약 가능한 시간인지 확인
     */
    fun canMakeReservationAt(startTime: LocalTime, endTime: LocalTime): Boolean {
        if (!isActive()) return false
        if (is24Hours) return true
        
        return if (breakStartTime != null && breakEndTime != null) {
            // 휴게시간과 겹치지 않는지 확인
            val noBreakConflict = endTime <= breakStartTime || startTime >= breakEndTime
            val withinBusinessHours = startTime >= openTime && endTime <= closeTime
            
            noBreakConflict && withinBusinessHours
        } else {
            startTime >= openTime && endTime <= closeTime
        }
    }
} 