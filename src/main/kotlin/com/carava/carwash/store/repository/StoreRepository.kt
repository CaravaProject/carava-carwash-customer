package com.carava.carwash.store.repository

import com.carava.carwash.store.entity.Store
import com.carava.carwash.store.entity.StoreCategory
import com.carava.carwash.store.entity.StoreStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreRepository : JpaRepository<Store, Long> {
    
    fun findByCategoryAndStatus(category: StoreCategory, status: StoreStatus, pageable: Pageable): Page<Store>
    
    fun findByIdAndStatus(id: Long, status: StoreStatus): Store?
} 