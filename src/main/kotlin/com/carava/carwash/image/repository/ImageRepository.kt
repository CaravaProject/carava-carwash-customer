package com.carava.carwash.image.repository

import com.carava.carwash.image.entity.ImageEntity
import com.carava.carwash.image.entity.ImageCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 이미지 Repository
 */
@Repository
interface ImageRepository : JpaRepository<ImageEntity, Long> {

    /**
     * 업로더별 이미지 조회 (생성일 내림차순)
     */
    fun findByUploaderIdOrderByCreatedAtDesc(
        uploaderId: Long,
        pageable: Pageable
    ): Page<ImageEntity>

    /**
     * 업로더 + 카테고리별 이미지 조회
     */
    fun findByUploaderIdAndCategoryOrderByCreatedAtDesc(
        uploaderId: Long,
        category: ImageCategory,
        pageable: Pageable
    ): Page<ImageEntity>

    /**
     * 업로더 + 엔티티 타입 + 엔티티 ID별 이미지 조회
     */
    fun findByUploaderIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
        uploaderId: Long,
        entityType: String,
        entityId: Long,
        pageable: Pageable
    ): Page<ImageEntity>

    /**
     * 업로더 + 카테고리 + 엔티티 타입 + 엔티티 ID별 이미지 조회
     */
    fun findByUploaderIdAndCategoryAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
        uploaderId: Long,
        category: ImageCategory,
        entityType: String,
        entityId: Long,
        pageable: Pageable
    ): Page<ImageEntity>

    /**
     * 임시 이미지 중 생성일이 특정 시간보다 오래된 것들 조회
     */
    fun findByIsTemporaryTrueAndCreatedAtBefore(cutoffTime: LocalDateTime): List<ImageEntity>
}