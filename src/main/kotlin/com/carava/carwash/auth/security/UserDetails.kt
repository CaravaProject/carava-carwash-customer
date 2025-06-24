package com.carava.carwash.auth.security

import com.carava.carwash.auth.entity.Auth
import com.carava.carwash.global.constants.UserType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetails(
    private val auth: Auth
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority("ROLE" + UserType.CUSTOMER))
    }

    override fun getPassword(): String = auth.password

    override fun getUsername(): String = auth.email
}