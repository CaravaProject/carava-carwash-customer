package com.carava.carwash.auth.repository

import com.carava.carwash.auth.entity.Auth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository("authRepository")
interface AuthRepository : JpaRepository<Auth, Long> {
    fun findByEmail(email: String): Optional<Auth>
    fun existsByEmail(email: String): Boolean
}