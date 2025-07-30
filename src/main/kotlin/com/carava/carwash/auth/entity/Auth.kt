package com.carava.carwash.auth.entity

import com.carava.carwash.shared.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "auth")
data class Auth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(unique = true, nullable = false, length = 255)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    var userType: UserType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AuthStatus = AuthStatus.ACTIVE,

    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,

    @Column(name = "refresh_token")
    var refreshToken: String? = null,

    @Column(name = "fcm_token")
    var fcmToken: String? = null,

    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null

) : BaseEntity() {

    fun verifyEmail() {
        this.emailVerified = true
    }

    fun updateRefreshToken(token: String?) {
        this.refreshToken = token
    }

    fun updateFcmToken(token: String?) {
        this.fcmToken = token
    }

    fun suspend() {
        this.status = AuthStatus.SUSPENDED
    }

    fun activate() {
        this.status = AuthStatus.ACTIVE
    }

    fun deactivate() {
        this.status = AuthStatus.INACTIVE
    }

    fun updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now()
    }

    fun isActive(): Boolean = status == AuthStatus.ACTIVE
}