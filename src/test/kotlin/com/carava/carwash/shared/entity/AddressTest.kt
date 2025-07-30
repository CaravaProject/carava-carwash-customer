package com.carava.carwash.shared.entity

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AddressTest {
    
    @Test
    fun `given_valid_address_data_when_create_address_then_success`() {
        // given
        val address = "서울특별시 강남구 테헤란로 123"
        val detailAddress = "456호"
        val postalCode = "06234"
        val latitude = BigDecimal("37.5665")
        val longitude = BigDecimal("126.9780")
        
        // when
        val addressEntity = Address(
            address = address,
            detailAddress = detailAddress,
            postalCode = postalCode,
            latitude = latitude,
            longitude = longitude
        )
        
        // then
        assertEquals(address, addressEntity.address)
        assertEquals(detailAddress, addressEntity.detailAddress)
        assertEquals(postalCode, addressEntity.postalCode)
        assertEquals(latitude, addressEntity.latitude)
        assertEquals(longitude, addressEntity.longitude)
        assertNotNull(addressEntity.createdAt)
        assertNotNull(addressEntity.updatedAt)
    }
    
    @Test
    fun `given_address_without_detail_when_create_then_detail_address_null`() {
        // given
        val address = "서울특별시 강남구 테헤란로 123"
        val postalCode = "06234"
        
        // when
        val addressEntity = Address(
            address = address,
            postalCode = postalCode
        )
        
        // then
        assertEquals(address, addressEntity.address)
        assertNull(addressEntity.detailAddress)
        assertEquals(postalCode, addressEntity.postalCode)
        assertNull(addressEntity.latitude)
        assertNull(addressEntity.longitude)
    }
    
    @Test
    fun `given_address_when_update_coordinates_then_coordinates_updated`() {
        // given
        val addressEntity = Address(
            address = "서울특별시 강남구 테헤란로 123",
            postalCode = "06234"
        )
        val newLatitude = BigDecimal("37.5665")
        val newLongitude = BigDecimal("126.9780")
        
        // when
        addressEntity.updateCoordinates(newLatitude, newLongitude)
        
        // then
        assertEquals(newLatitude, addressEntity.latitude)
        assertEquals(newLongitude, addressEntity.longitude)
    }
} 