package com.carava.carwash.menu.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(
    name = "menu",
    indexes = [
        Index(name = "idx_menu_store_id", columnList = "store_id"),
        Index(name = "idx_menu_category_active", columnList = "category_name, is_active")
    ]
)
data class Menu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "store_id", nullable = false)
    var storeId: Long,

    @Column(name = "category_name", nullable = false, length = 50)
    var categoryName: String,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,

    @Column(nullable = false)
    var duration: Int, // 분 단위

    @Column(name = "car_types", columnDefinition = "jsonb")
    var carTypes: String? = null, // JSON 형태로 저장

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "display_order")
    var displayOrder: Int = 0

) : BaseEntity() {

    fun activate() {
        this.isActive = true
    }

    fun deactivate() {
        this.isActive = false
    }

    fun updatePrice(newPrice: BigDecimal) {
        this.price = newPrice
    }

    fun updateInfo(
        name: String? = null,
        description: String? = null,
        price: BigDecimal? = null,
        duration: Int? = null
    ) {
        name?.let { this.name = it }
        description?.let { this.description = it }
        price?.let { this.price = it }
        duration?.let { this.duration = it }
    }
} 