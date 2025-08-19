package com.carava.carwash.global.config

import com.carava.carwash.global.resolver.MemberIdResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring MVC 웹 설정 클래스
 * 
 * 역할:
 * - 커스텀 ArgumentResolver들을 Spring MVC에 등록
 * - @CurrentMemberId 어노테이션이 붙은 파라미터를 자동으로 JWT에서 추출한 memberId로 주입
 * 
 * 동작 과정:
 * 1. 클라이언트 요청 → 컨트롤러 메서드 호출
 * 2. Spring이 메서드 파라미터 분석 → @CurrentMemberId 발견
 * 3. 등록된 ArgumentResolver 중 처리 가능한 것 찾기 → MemberIdResolver 선택
 * 4. MemberIdResolver가 JWT 토큰에서 memberId 추출
 * 5. 추출한 memberId를 파라미터에 자동 주입
 * 6. 컨트롤러 메서드 실행
 * 
 * 이점:
 * - 모든 컨트롤러에서 JWT 파싱 코드 중복 제거
 * - 인증 로직과 비즈니스 로직 분리
 * - 컨트롤러 코드 간소화 및 가독성 향상
 */
@Configuration
class WebConfig(
    private val memberIdResolver: MemberIdResolver
) : WebMvcConfigurer {
    
    /**
     * 커스텀 ArgumentResolver들을 Spring MVC에 등록
     * 
     * @param resolvers Spring MVC의 ArgumentResolver 목록
     */
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(memberIdResolver)  // @CurrentMemberId 처리를 위한 resolver 등록
    }
}
