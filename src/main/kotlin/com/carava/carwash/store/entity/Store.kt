package com.carava.carwash.store.entity

import com.carava.carwash.shared.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

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
    var favoriteCount: Int = 0

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
} 