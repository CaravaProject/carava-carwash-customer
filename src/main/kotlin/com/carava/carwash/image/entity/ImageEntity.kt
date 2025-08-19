package com.carava.carwash.image.entity

import com.carava.carwash.global.entity.BaseEntity
import jakarta.persistence.*

/**
 * 이미지 엔티티
 * MinIO에 저장된 이미지 메타데이터 관리
 */
@Entity
@Table(
    name = "images",
    indexes = [
        Index(name = "idx_image_category", columnList = "category"),
        Index(name = "idx_image_entity", columnList = "entity_type, entity_id"),
        Index(name = "idx_image_uploader", columnList = "uploader_id"),
        Index(name = "idx_image_active", columnList = "is_active")
    ]
)
data class ImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    /**
     * 이미지 카테고리 (PROFILE, REVIEW, STORE, SYSTEM)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    val category: ImageCategory,

    /**
     * 연관 엔티티 타입 (CUSTOMER, STORE, REVIEW 등)
     */
    @Column(name = "entity_type", length = 50)
    val entityType: String? = null,

    /**
     * 연관 엔티티 ID
     */
    @Column(name = "entity_id")
    val entityId: Long? = null,

    /**
     * 업로더 ID (사용자 ID)
     */
    @Column(name = "uploader_id", nullable = false)
    val uploaderId: Long,

    /**
     * 원본 파일명
     */
    @Column(name = "original_filename", nullable = false, length = 255)
    val originalFileName: String,

    /**
     * 저장된 파일명 (UUID 기반)
     */
    @Column(name = "stored_filename", nullable = false, length = 255)
    val storedFileName: String,

    /**
     * 저장된 경로
     */
    @Column(name = "stored_path", nullable = false, length = 500)
    val storedPath: String,

    /**
     * 파일 크기 (bytes)
     */
    @Column(name = "file_size", nullable = false)
    val fileSize: Long,

    /**
     * MIME 타입
     */
    @Column(name = "mime_type", nullable = false, length = 100)
    val mimeType: String,

    /**
     * 이미지 너비 (픽셀)
     */
    @Column(name = "width")
    val width: Int? = null,

    /**
     * 이미지 높이 (픽셀)
     */
    @Column(name = "height")
    val height: Int? = null,

    /**
     * 썸네일 경로
     */
    @Column(name = "thumbnail_path", length = 500)
    val thumbnailPath: String? = null,

    /**
     * 중간 크기 경로
     */
    @Column(name = "medium_path", length = 500)
    val mediumPath: String? = null,

    /**
     * 임시 파일 여부
     */
    @Column(name = "is_temporary", nullable = false)
    val isTemporary: Boolean = false,

    /**
     * 이미지 설명
     */
    @Column(name = "description", length = 500)
    val description: String? = null,

    /**
     * 정렬 순서
     */
    @Column(name = "sort_order")
    val sortOrder: Int = 0

) : BaseEntity() {

    /**
     * 공개 URL 생성 (MinIO 엔드포인트 기반)
     */
    fun getPublicUrl(minioEndpoint: String, bucketName: String): String {
        return "$minioEndpoint/$bucketName/$storedPath"
    }

    /**
     * 썸네일 URL 생성
     */
    fun getThumbnailUrl(minioEndpoint: String, bucketName: String): String? {
        return thumbnailPath?.let { "$minioEndpoint/$bucketName/$it" }
    }

    /**
     * 중간 크기 URL 생성
     */
    fun getMediumUrl(minioEndpoint: String, bucketName: String): String? {
        return mediumPath?.let { "$minioEndpoint/$bucketName/$it" }
    }
}