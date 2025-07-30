package com.carava.carwash.car.repository

import com.carava.carwash.car.entity.Car
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CarRepository : JpaRepository<Car, Long> {
    
    fun findByCustomerMemberId(customerMemberId: Long): List<Car>
    
    fun findByCustomerMemberIdAndIsDefault(customerMemberId: Long, isDefault: Boolean): Car?
    
    fun existsByCustomerMemberIdAndLicensePlate(customerMemberId: Long, licensePlate: String): Boolean
    
    @Modifying
    @Query("UPDATE Car c SET c.isDefault = false WHERE c.customerMemberId = :customerMemberId")
    fun unsetAllDefaultCars(customerMemberId: Long)
} 