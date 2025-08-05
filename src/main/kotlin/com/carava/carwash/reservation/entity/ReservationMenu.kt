package com.carava.carwash.reservation.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(
    name = "reservation_menu",
    indexes = [
        Index(name = "idx_reservation_menu_reservation_id", columnList = "reservation_id"),
        Index(name = "idx_reservation_menu_menu_id", columnList = "menu_id")
    ]
)
data class ReservationMenu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "reservation_id", nullable = false)
    var reservationId: Long,

    @Column(name = "menu_id", nullable = false)
    var menuId: Long,

    @Column(nullable = false)
    var quantity: Int = 1,

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal,

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    var totalPrice: BigDecimal

) : BaseEntity() {

    fun calculateTotalPrice() {
        this.totalPrice = unitPrice.multiply(BigDecimal(quantity))
    }

    fun updateQuantity(newQuantity: Int) {
        this.quantity = newQuantity
        calculateTotalPrice()
    }
} 