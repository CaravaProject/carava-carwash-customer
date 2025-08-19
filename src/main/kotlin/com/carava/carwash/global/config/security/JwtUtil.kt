package com.carava.carwash.global.config.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${jwt.secret}")
    private val secretKey: String,
    
    @Value("\${jwt.access-token-expire-time}")
    private val accessTokenExpireTime: Long,
    
    @Value("\${jwt.refresh-token-expire-time}")
    private val refreshTokenExpireTime: Long
) {
    
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    
    fun generateAccessToken(email: String, userType: String): String {
        return generateToken(email, userType, accessTokenExpireTime)
    }
    
    fun generateRefreshToken(email: String, userType: String): String {
        return generateToken(email, userType, refreshTokenExpireTime)
    }
    
    private fun generateToken(email: String, userType: String, expireTime: Long): String {
        val claims = mapOf(
            "email" to email,
            "userType" to userType
        )
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(email)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expireTime))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getEmailFromToken(token: String): String {
        return getClaims(token).subject
    }
    
    fun getUserTypeFromToken(token: String): String {
        return getClaims(token)["userType"] as String
    }

    fun getMemberIdFromToken(token: String): Long {
        // TODO: 실제 구현에서는 JWT에 memberId를 포함하거나, 
        // email로 DB에서 조회하여 memberId를 반환해야 함
        // 현재는 임시로 email을 Long으로 변환 (테스트용)
        val email = getEmailFromToken(token)
        return email.hashCode().toLong().let { if (it < 0) -it else it }
    }
    
    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
} 