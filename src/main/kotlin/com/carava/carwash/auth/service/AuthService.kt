package com.carava.carwash.auth.service

import com.carava.carwash.auth.dto.*
import com.carava.carwash.auth.entity.Auth
import com.carava.carwash.auth.entity.UserType
import com.carava.carwash.auth.repository.AuthRepository
import com.carava.carwash.auth.util.JwtUtil
import com.carava.carwash.member.entity.CustomerMember
import com.carava.carwash.member.repository.CustomerMemberRepository
import com.carava.carwash.shared.dto.ApiResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val authRepository: AuthRepository,
    private val customerMemberRepository: CustomerMemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    fun signUp(request: SignUpRequestDto): ApiResponse<SignUpResponseDto> {
        // 이메일 중복 검사
        if (authRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다")
        }

        // 전화번호 중복 검사
        if (customerMemberRepository.existsByPhone(request.phone)) {
            throw IllegalArgumentException("이미 사용 중인 전화번호입니다")
        }

        // Auth 엔티티 생성 및 저장
        val auth = Auth(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            userType = UserType.CUSTOMER
        )
        val savedAuth = authRepository.save(auth)

        // CustomerMember 엔티티 생성 및 저장
        val customer = CustomerMember(
            authId = savedAuth.id,
            name = request.name,
            phone = request.phone,
            birthDate = request.birthDate,
            nickname = request.nickname
        )
        val savedCustomer = customerMemberRepository.save(customer)

        val responseDto = SignUpResponseDto.from(savedAuth, savedCustomer)
        return ApiResponse.success(responseDto, "회원가입이 완료되었습니다")
    }

    fun signIn(request: SignInRequestDto): ApiResponse<SignInResponseDto> {
        // 사용자 조회
        val auth = authRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("존재하지 않는 이메일입니다")

        // 계정 상태 확인
        if (!auth.isActive()) {
            throw IllegalArgumentException("비활성화된 계정입니다")
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password, auth.password)) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다")
        }

        // 고객 정보 조회
        val customer = customerMemberRepository.findByAuthId(auth.id)
            ?: throw IllegalArgumentException("고객 정보를 찾을 수 없습니다")

        // 토큰 생성
        val accessToken = jwtUtil.generateAccessToken(auth.email, auth.userType.name)
        val refreshToken = jwtUtil.generateRefreshToken(auth.email, auth.userType.name)

        // 리프레시 토큰 저장 및 로그인 시간 업데이트
        auth.updateRefreshToken(refreshToken)
        auth.updateLastLogin()
        authRepository.save(auth)

        val responseDto = SignInResponseDto.from(auth, customer, accessToken, refreshToken)
        return ApiResponse.success(responseDto, "로그인이 완료되었습니다")
    }

    fun checkEmail(email: String): ApiResponse<Boolean> {
        val exists = authRepository.existsByEmail(email)
        return ApiResponse.success(!exists, if (exists) "사용 중인 이메일입니다" else "사용 가능한 이메일입니다")
    }

    fun refreshToken(refreshToken: String): ApiResponse<String> {
        // 토큰 유효성 검사
        if (!jwtUtil.validateToken(refreshToken)) {
            throw IllegalArgumentException("유효하지 않은 리프레시 토큰입니다")
        }

        // 토큰으로 사용자 조회
        val auth = authRepository.findByRefreshToken(refreshToken)
            ?: throw IllegalArgumentException("존재하지 않는 리프레시 토큰입니다")

        // 새 액세스 토큰 생성
        val newAccessToken = jwtUtil.generateAccessToken(auth.email, auth.userType.name)
        
        return ApiResponse.success(newAccessToken, "토큰이 갱신되었습니다")
    }
}
