package com.carava.carwash.auth.service

import com.carava.carwash.auth.dto.*
import com.carava.carwash.auth.entity.Auth

import com.carava.carwash.auth.repository.AuthRepository
import com.carava.carwash.global.config.security.JwtUtil
import com.carava.carwash.global.constants.UserType
import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.global.exception.EmailAlreadyExistsException
import jakarta.transaction.Transactional
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service("authService")
@Transactional
class AuthService(
    private val authRepository: AuthRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val authenticationManager: AuthenticationManager
) {

    fun signUp(request: SignUpRequestDto): ApiResponse<SignUpResponseDto> {
        if (authRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException("이미 존재하는 이메일입니다.")
        }

        val auth = Auth(
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        val savedAuth = authRepository.save(auth)

        return ApiResponse.success(
            data = SignUpResponseDto(
                email = savedAuth.email,
                createdAt = savedAuth.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        )
    }

    fun signIn(request: SignInRequestDto): ApiResponse<SignInResponseDto> {

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val auth = authRepository.findByEmail(request.email)
            .orElseThrow{ UsernameNotFoundException("사용자를 찾을 수 없습니다.") }

        val token = jwtUtil.generateToken(auth.email, UserType.CUSTOMER)

        return ApiResponse.success(
            data = SignInResponseDto(
                accessToken = token,
                expiresIn = 86400
            )
        )
    }

    fun checkUsername(email: String): ApiResponse<CheckUsernameResponseDto> {
        return ApiResponse.success(
            data = CheckUsernameResponseDto(
                isDuplicate = authRepository.existsByEmail(email)
            )
        )
    }

}
