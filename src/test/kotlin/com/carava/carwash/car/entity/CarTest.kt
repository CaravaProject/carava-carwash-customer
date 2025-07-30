package com.carava.carwash.car.entity

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CarTest {
    
    @Test
    fun `given_valid_car_data_when_create_car_then_success`() {
        // given
        val customerMemberId = 1L
        val brand = "현대"
        val model = "아반떼"
        val year = 2023
        val color = "화이트"
        val licensePlate = "12가3456"
        val carType = CarType.SEDAN
        
        // when
        val car = Car(
            customerMemberId = customerMemberId,
            brand = brand,
            model = model,
            year = year,
            color = color,
            licensePlate = licensePlate,
            carType = carType
        )
        
        // then
        assertEquals(customerMemberId, car.customerMemberId)
        assertEquals(brand, car.brand)
        assertEquals(model, car.model)
        assertEquals(year, car.year)
        assertEquals(color, car.color)
        assertEquals(licensePlate, car.licensePlate)
        assertEquals(carType, car.carType)
        assertFalse(car.isDefault) // 기본값
        assertNotNull(car.createdAt)
        assertNotNull(car.updatedAt)
    }
    
    @Test
    fun `given_car_when_set_as_default_then_is_default_true`() {
        // given
        val car = Car(
            customerMemberId = 1L,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )
        
        // when
        car.setAsDefault()
        
        // then
        assertTrue(car.isDefault)
    }
    
    @Test
    fun `given_default_car_when_unset_default_then_is_default_false`() {
        // given
        val car = Car(
            customerMemberId = 1L,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            licensePlate = "12가3456",
            carType = CarType.SEDAN,
            isDefault = true
        )
        
        // when
        car.unsetDefault()
        
        // then
        assertFalse(car.isDefault)
    }
    
    @Test
    fun `given_car_when_update_info_then_info_updated`() {
        // given
        val car = Car(
            customerMemberId = 1L,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )
        val newColor = "블랙"
        val newLicensePlate = "34나5678"
        
        // when
        car.updateInfo(
            color = newColor,
            licensePlate = newLicensePlate
        )
        
        // then
        assertEquals(newColor, car.color)
        assertEquals(newLicensePlate, car.licensePlate)
    }
    
    @Test
    fun `given_car_when_get_display_name_then_return_formatted_name`() {
        // given
        val car = Car(
            customerMemberId = 1L,
            brand = "현대",
            model = "아반떼",
            year = 2023,
            color = "화이트",
            licensePlate = "12가3456",
            carType = CarType.SEDAN
        )
        
        // when
        val displayName = car.getDisplayName()
        
        // then
        assertEquals("현대 아반떼 (2023년, 화이트)", displayName)
    }
} 