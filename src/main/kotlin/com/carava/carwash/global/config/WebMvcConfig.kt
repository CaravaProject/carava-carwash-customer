package com.carava.carwash.global.config

import com.carava.carwash.global.resolver.CurrentMemberIdArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring MVC 설정
 */
@Configuration
class WebMvcConfig(
    private val currentMemberIdArgumentResolver: CurrentMemberIdArgumentResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(currentMemberIdArgumentResolver)
    }
}
