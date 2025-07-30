package com.carava.carwash.auth.repository

import com.carava.carwash.auth.entity.Auth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthRepository : JpaRepository<Auth, Long> {
    
    fun findByEmail(email: String): Auth?
    
    fun existsByEmail(email: String): Boolean
    
    fun findByRefreshToken(refreshToken: String): Auth?
}