package com.carava.carwash.car.entity

import com.carava.carwash.shared.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "car",
    indexes = [
        Index(name = "idx_car_customer_member_id", columnList = "customer_member_id"),
        Index(name = "idx_car_customer_license_plate", columnList = "customer_member_id, license_plate", unique = true)
    ]
)
data class Car(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "customer_member_id", nullable = false)
    var customerMemberId: Long,

    @Column(nullable = false, length = 50)
    var brand: String,

    @Column(nullable = false, length = 50)
    var model: String,

    @Column(nullable = false)
    var year: Int,

    @Column(length = 30)
    var color: String? = null,

    @Column(name = "license_plate", nullable = false, length = 20)
    var licensePlate: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", nullable = false)
    var carType: CarType,

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = false

) : BaseEntity() {

    fun setAsDefault() {
        this.isDefault = true
    }

    fun unsetDefault() {
        this.isDefault = false
    }

    fun updateInfo(
        color: String? = null,
        licensePlate: String? = null
    ) {
        color?.let { this.color = it }
        licensePlate?.let { this.licensePlate = it }
    }

    fun getDisplayName(): String {
        return if (color != null) {
            "$brand $model (${year}년, $color)"
        } else {
            "$brand $model (${year}년)"
        }
    }
} 