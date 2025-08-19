package com.carava.carwash.image.dto

import com.carava.carwash.image.entity.ImageCategory
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "이미지 업로드 요청 DTO")
data class ImageUploadRequestDto(
    @field:NotNull(message = "이미지 카테고리는 필수입니다")
    @Schema(description = "이미지 카테고리", example = "RESERVATION")
    val category: ImageCategory,
    
    @field:Size(max = 500, message = "설명은 500자 이내로 입력해주세요")
    @Schema(description = "이미지 설명", example = "차량 앞범퍼 스크래치 상태")
    val description: String? = null,
    
    @Schema(description = "관련 엔티티 타입", example = "RESERVATION")
    val entityType: String? = null,
    
    @Schema(description = "관련 엔티티 ID", example = "123")
    val entityId: Long? = null
)