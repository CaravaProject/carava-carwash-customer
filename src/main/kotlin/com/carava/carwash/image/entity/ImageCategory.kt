package com.carava.carwash.image.entity

/**
 * 이미지 카테고리 정의
 */
enum class ImageCategory(
    val displayName: String,
    val description: String,
    val path: String, // MinIO 내 경로
    val maxSize: Long = 10_000_000L, // 기본 10MB
    val allowedExtensions: Set<String> = setOf("jpg", "jpeg", "png", "webp")
) {
    /**
     * 프로필 이미지
     */
    PROFILE("프로필", "사용자 프로필 이미지", "profiles", 5_000_000L),

    /**
     * 리뷰 이미지
     */
    REVIEW("리뷰", "서비스 리뷰 이미지", "reviews", 10_000_000L),

    /**
     * 업체 이미지
     */
    STORE("업체", "업체 관련 이미지", "stores", 10_000_000L),

    /**
     * 시스템 이미지
     */
    SYSTEM("시스템", "시스템 기본 이미지", "system", 10_000_000L),

    /**
     * 인증서류 이미지
     */
    CERTIFICATE("인증서류", "업체 인증서류 이미지", "certificates", 10_000_000L);

    /**
     * 파일 확장자 검증
     */
    fun isValidExtension(extension: String): Boolean {
        return allowedExtensions.contains(extension.lowercase())
    }

    /**
     * 파일 크기 검증
     */
    fun isValidSize(size: Long): Boolean {
        return size <= maxSize
    }

    /**
     * 객체 키 생성
     */
    fun generateObjectKey(entityType: String?, entityId: Long?, filename: String): String {
        return when {
            entityType != null && entityId != null -> 
                "$path/${entityType.lowercase()}/$entityId/$filename"
            else -> 
                "$path/$filename"
        }
    }
}