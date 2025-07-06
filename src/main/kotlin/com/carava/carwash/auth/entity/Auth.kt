package com.carava.carwash.auth.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*

@Entity(name = "auth")
@Table(name = "customer_auth")
class Auth (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(unique = true, nullable = false, length = 50)
    var email: String,

    @Column(nullable = false)
    var password: String,
) : BaseEntity()