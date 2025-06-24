package com.carava.carwash.auth.service

import com.carava.carwash.auth.repository.AuthRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsService (
    private val authRepository: AuthRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): org.springframework.security.core.userdetails.UserDetails {
        val auth = authRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("customer - 사용자를 찾을 수 없습니다.") }

        return com.carava.carwash.auth.security.UserDetails(auth)
    }
}