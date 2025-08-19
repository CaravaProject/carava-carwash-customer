package com.carava.carwash.image.dto

import com.carava.carwash.image.entity.ImageCategory
import com.carava.carwash.image.entity.ImageEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "이미지 목록 조회 응답 DTO")
data class ImageListResponseDto(
    @Schema(description = "이미지 ID", example = "123")
    val imageId: Long,
    
    @Schema(description = "원본 파일명", example = "car_damage.jpg")
    val originalFileName: String,
    
    @Schema(description = "이미지 카테고리", example = "RESERVATION")
    val category: ImageCategory,
    
    @Schema(description = "썸네일 URL", example = "http://158.179.171.102:9000/carava-bucket/images/thumb_20241215_abc123.jpg")
    val thumbnailUrl: String,
    
    @Schema(description = "공개 URL", example = "http://158.179.171.102:9000/carava-bucket/images/20241215_abc123.jpg")
    val publicUrl: String,
    
    @Schema(description = "파일 크기 (bytes)", example = "1024000")
    val fileSize: Long,
    
    @Schema(description = "이미지 설명", example = "차량 앞범퍼 스크래치 상태")
    val description: String? = null,
    
    @Schema(description = "연결된 엔티티 타입", example = "RESERVATION")
    val entityType: String? = null,
    
    @Schema(description = "연결된 엔티티 ID", example = "456")
    val entityId: Long? = null,
    
    @Schema(description = "업로드 일시", example = "2024-12-15T14:30:00")
    val uploadedAt: LocalDateTime
) {
    companion object {
        fun from(image: ImageEntity, baseUrl: String): ImageListResponseDto {
            return ImageListResponseDto(
                imageId = image.id!!,
                originalFileName = image.originalFileName,
                category = image.category,
                thumbnailUrl = "$baseUrl/${image.thumbnailPath ?: image.storedPath}",
                publicUrl = "$baseUrl/${image.storedPath}",
                fileSize = image.fileSize,
                description = image.description,
                entityType = image.entityType,
                entityId = image.entityId,
                uploadedAt = image.createdAt
            )
        }
    }
}