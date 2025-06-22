package com.carava.carwash.customer.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "customer_member")
class Member (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "customer_auth_id", nullable = false)
    var authId: Long = 0,

    @Column(nullable = false)
    var name: String,
) : BaseEntity()