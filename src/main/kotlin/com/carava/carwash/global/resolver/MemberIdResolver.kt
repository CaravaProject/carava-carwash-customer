package com.carava.carwash.global.resolver

import com.carava.carwash.global.annotation.CurrentMemberId
import com.carava.carwash.global.config.security.JwtUtil
import com.carava.carwash.global.exception.BusinessException
import com.carava.carwash.global.exception.ErrorCode
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class MemberIdResolver(
    private val jwtUtil: JwtUtil,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter) =
        parameter.hasParameterAnnotation(CurrentMemberId::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Long {
        val token = webRequest.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: throw BusinessException(ErrorCode.UNAUTHORIZED_ACCESS, "Authorization 헤더가 없습니다")
        
        try {
            return jwtUtil.getMemberIdFromToken(token)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_CREDENTIALS, "유효하지 않은 토큰입니다")
        }
    }
}
