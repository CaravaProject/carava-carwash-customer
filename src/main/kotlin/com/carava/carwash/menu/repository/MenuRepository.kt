package com.carava.carwash.menu.repository

import com.carava.carwash.menu.entity.Menu
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MenuRepository : JpaRepository<Menu, Long> {
    
    fun findByStoreIdAndIsActive(storeId: Long, isActive: Boolean): List<Menu>
    
    fun findByIdInAndIsActive(ids: List<Long>, isActive: Boolean): List<Menu>
} 