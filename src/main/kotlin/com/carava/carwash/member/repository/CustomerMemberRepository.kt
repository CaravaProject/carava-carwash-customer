package com.carava.carwash.member.repository

import com.carava.carwash.member.entity.CustomerMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerMemberRepository : JpaRepository<CustomerMember, Long> {
    
    fun findByAuthId(authId: Long): CustomerMember?
    
    fun existsByPhone(phone: String): Boolean
} 