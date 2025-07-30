package com.carava.carwash.member.entity

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CustomerMemberTest {
    
    @Test
    fun `given_valid_customer_data_when_create_customer_member_then_success`() {
        // given
        val authId = 1L
        val name = "홍길동"
        val phone = "010-1234-5678"
        val birthDate = LocalDate.of(1990, 1, 1)
        val nickname = "길동이"
        
        // when
        val customer = CustomerMember(
            authId = authId,
            name = name,
            phone = phone,
            birthDate = birthDate,
            nickname = nickname
        )
        
        // then
        assertEquals(authId, customer.authId)
        assertEquals(name, customer.name)
        assertEquals(phone, customer.phone)
        assertEquals(birthDate, customer.birthDate)
        assertEquals(nickname, customer.nickname)
        assertNull(customer.addressId)
        assertNull(customer.profileImageUrl)
        assertNotNull(customer.createdAt)
        assertNotNull(customer.updatedAt)
    }
    
    @Test
    fun `given_customer_when_update_profile_then_profile_updated`() {
        // given
        val customer = CustomerMember(
            authId = 1L,
            name = "홍길동",
            phone = "010-1234-5678"
        )
        val newName = "홍길동 업데이트"
        val newPhone = "010-9876-5432"
        val newNickname = "새길동"
        val newBirthDate = LocalDate.of(1985, 5, 15)
        
        // when
        customer.updateProfile(
            name = newName,
            phone = newPhone,
            nickname = newNickname,
            birthDate = newBirthDate
        )
        
        // then
        assertEquals(newName, customer.name)
        assertEquals(newPhone, customer.phone)
        assertEquals(newNickname, customer.nickname)
        assertEquals(newBirthDate, customer.birthDate)
    }
    
    @Test
    fun `given_customer_when_update_address_then_address_updated`() {
        // given
        val customer = CustomerMember(
            authId = 1L,
            name = "홍길동",
            phone = "010-1234-5678"
        )
        val addressId = 100L
        
        // when
        customer.updateAddress(addressId)
        
        // then
        assertEquals(addressId, customer.addressId)
    }
    
    @Test
    fun `given_customer_when_update_profile_image_then_image_url_updated`() {
        // given
        val customer = CustomerMember(
            authId = 1L,
            name = "홍길동",
            phone = "010-1234-5678"
        )
        val imageUrl = "https://example.com/profile.jpg"
        
        // when
        customer.updateProfileImage(imageUrl)
        
        // then
        assertEquals(imageUrl, customer.profileImageUrl)
    }
} 