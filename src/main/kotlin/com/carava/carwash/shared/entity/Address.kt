package com.carava.carwash.shared.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "address")
data class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var address: String,

    @Column(name = "detail_address")
    var detailAddress: String? = null,

    @Column(name = "postal_code", nullable = false)
    var postalCode: String,

    @Column(precision = 10, scale = 8)
    var latitude: BigDecimal? = null,

    @Column(precision = 11, scale = 8)
    var longitude: BigDecimal? = null

) : BaseEntity() {

    fun updateCoordinates(latitude: BigDecimal?, longitude: BigDecimal?) {
        this.latitude = latitude
        this.longitude = longitude
    }

    fun updateAddress(address: String, detailAddress: String?, postalCode: String) {
        this.address = address
        this.detailAddress = detailAddress
        this.postalCode = postalCode
    }
} 