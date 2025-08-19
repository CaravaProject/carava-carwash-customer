package com.carava.carwash.image.service

import com.carava.carwash.global.dto.ApiResponse
import com.carava.carwash.image.dto.ImageListResponseDto
import com.carava.carwash.image.dto.ImageUploadResponseDto
import com.carava.carwash.image.entity.ImageCategory
import com.carava.carwash.image.entity.ImageEntity
import com.carava.carwash.image.repository.ImageRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ImageUploadService(
    private val imageRepository: ImageRepository,
    private val minioStorageService: MinioStorageService,
    private val imageProcessingService: ImageProcessingService,
    @Value("\${minio.endpoint}") private val minioEndpoint: String,
    @Value("\${minio.bucket-name}") private val bucketName: String
) {
    private val logger = LoggerFactory.getLogger(ImageUploadService::class.java)
    
    /**
     * 이미지 업로드 (임시 위치에 저장)
     */
    fun uploadImage(
        file: MultipartFile,
        uploaderId: Long,
        category: ImageCategory,
        description: String? = null
    ): ApiResponse<ImageUploadResponseDto> {
        try {
            // 1. 파일 검증
            validateImageFile(file)
            
            // 2. 고유 파일명 생성
            val fileExtension = getFileExtension(file.originalFilename)
            val uniqueFileName = generateUniqueFileName(fileExtension)
            
            // 3. 임시 경로 설정 (나중에 예약 확정 시 이동됨)
            val tempPath = "images/temp/$uniqueFileName"
            val thumbnailPath = "images/temp/thumb_$uniqueFileName"
            val mediumPath = "images/temp/medium_$uniqueFileName"
            
            // 4. 이미지 처리 및 저장
            val originalBytes = file.bytes
            val thumbnailBytes = imageProcessingService.createThumbnail(originalBytes)
            val mediumBytes = imageProcessingService.createMediumImage(originalBytes)
            
            // MinIO에 업로드
            minioStorageService.uploadFile(tempPath, ByteArrayInputStream(originalBytes), file.contentType ?: "image/jpeg", originalBytes.size.toLong())
            minioStorageService.uploadFile(thumbnailPath, ByteArrayInputStream(thumbnailBytes), "image/jpeg", thumbnailBytes.size.toLong())
            minioStorageService.uploadFile(mediumPath, ByteArrayInputStream(mediumBytes), "image/jpeg", mediumBytes.size.toLong())
            
            // 5. 메타데이터 DB 저장
            val imageEntity = ImageEntity(
                originalFileName = file.originalFilename ?: "unknown",
                storedFileName = uniqueFileName,
                storedPath = tempPath,
                thumbnailPath = thumbnailPath,
                mediumPath = mediumPath,
                fileSize = file.size,
                mimeType = file.contentType ?: "image/jpeg",
                category = category,
                description = description,
                uploaderId = uploaderId,
                isTemporary = true // 임시 파일로 표시
            )
            
            val savedImage = imageRepository.save(imageEntity)
            
            // 6. 응답 생성
            val baseUrl = "$minioEndpoint/$bucketName"
            val responseDto = ImageUploadResponseDto.from(savedImage, baseUrl)
            
            logger.info("이미지 업로드 완료: imageId=${savedImage.id!!}, fileName=$uniqueFileName")
            return ApiResponse.success(responseDto, "이미지 업로드가 완료되었습니다")
            
        } catch (e: Exception) {
            logger.error("이미지 업로드 실패: ${e.message}", e)
            return ApiResponse.error("IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다: ${e.message}")
        }
    }
    
    /**
     * 이미지를 특정 엔티티와 연결하고 정식 위치로 이동
     */
    fun linkImageToEntity(
        imageId: Long,
        uploaderId: Long,
        entityType: String,
        entityId: Long
    ): ApiResponse<ImageUploadResponseDto> {
        try {
            val image = imageRepository.findById(imageId).orElse(null)
                ?: return ApiResponse.error("IMAGE_NOT_FOUND", "이미지를 찾을 수 없습니다")
            
            // 소유권 확인
            if (image.uploaderId != uploaderId) {
                return ApiResponse.error("ACCESS_DENIED", "이미지에 대한 권한이 없습니다")
            }
            
            // 이미 연결된 이미지인지 확인
            if (!image.isTemporary) {
                return ApiResponse.error("ALREADY_LINKED", "이미 연결된 이미지입니다")
            }
            
            // 정식 경로로 이동
            val finalPath = "images/$entityType/$entityId/${image.storedFileName}"
            val finalThumbnailPath = "images/$entityType/$entityId/thumb_${image.storedFileName}"
            val finalMediumPath = "images/$entityType/$entityId/medium_${image.storedFileName}"
            
            // MinIO에서 파일 이동
            minioStorageService.moveFile(image.storedPath, finalPath)
            minioStorageService.moveFile(image.thumbnailPath!!, finalThumbnailPath)
            minioStorageService.moveFile(image.mediumPath!!, finalMediumPath)
            
            // 메타데이터 업데이트
            val updatedImage = ImageEntity(
                id = image.id,
                category = image.category,
                entityType = entityType,
                entityId = entityId,
                uploaderId = image.uploaderId,
                originalFileName = image.originalFileName,
                storedFileName = image.storedFileName,
                storedPath = finalPath,
                fileSize = image.fileSize,
                mimeType = image.mimeType,
                width = image.width,
                height = image.height,
                thumbnailPath = finalThumbnailPath,
                mediumPath = finalMediumPath,
                isTemporary = false,
                description = image.description,
                sortOrder = image.sortOrder
            )
            
            val savedImage = imageRepository.save(updatedImage)
            
            val baseUrl = "$minioEndpoint/$bucketName"
            val responseDto = ImageUploadResponseDto.from(savedImage, baseUrl)
            
            logger.info("이미지 연결 완료: imageId=$imageId, entityType=$entityType, entityId=$entityId")
            return ApiResponse.success(responseDto, "이미지가 성공적으로 연결되었습니다")
            
        } catch (e: Exception) {
            logger.error("이미지 연결 실패: ${e.message}", e)
            return ApiResponse.error("IMAGE_LINK_FAILED", "이미지 연결에 실패했습니다: ${e.message}")
        }
    }
    
    /**
     * 사용자의 이미지 목록 조회
     */
    @Transactional(readOnly = true)
    fun getUserImages(
        uploaderId: Long,
        category: ImageCategory? = null,
        entityType: String? = null,
        entityId: Long? = null,
        pageable: Pageable
    ): ApiResponse<Page<ImageListResponseDto>> {
        try {
            val images = when {
                category != null && entityType != null && entityId != null -> {
                    imageRepository.findByUploaderIdAndCategoryAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        uploaderId, category, entityType, entityId, pageable
                    )
                }
                category != null -> {
                    imageRepository.findByUploaderIdAndCategoryOrderByCreatedAtDesc(
                        uploaderId, category, pageable
                    )
                }
                entityType != null && entityId != null -> {
                    imageRepository.findByUploaderIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        uploaderId, entityType, entityId, pageable
                    )
                }
                else -> {
                    imageRepository.findByUploaderIdOrderByCreatedAtDesc(uploaderId, pageable)
                }
            }
            
            val baseUrl = "$minioEndpoint/$bucketName"
            val responseImages = images.map { ImageListResponseDto.from(it, baseUrl) }
            
            return ApiResponse.success(responseImages, "이미지 목록을 성공적으로 조회했습니다")
            
        } catch (e: Exception) {
            logger.error("이미지 목록 조회 실패: ${e.message}", e)
            return ApiResponse.error("IMAGE_LIST_FAILED", "이미지 목록 조회에 실패했습니다: ${e.message}")
        }
    }
    
    /**
     * 이미지 삭제
     */
    fun deleteImage(imageId: Long, uploaderId: Long): ApiResponse<Unit> {
        try {
            val image = imageRepository.findById(imageId).orElse(null)
                ?: return ApiResponse.error("IMAGE_NOT_FOUND", "이미지를 찾을 수 없습니다")
            
            // 소유권 확인
            if (image.uploaderId != uploaderId) {
                return ApiResponse.error("ACCESS_DENIED", "이미지에 대한 권한이 없습니다")
            }
            
            // MinIO에서 파일 삭제
            minioStorageService.deleteFile(image.storedPath)
            image.thumbnailPath?.let { minioStorageService.deleteFile(it) }
            image.mediumPath?.let { minioStorageService.deleteFile(it) }
            
            // DB에서 메타데이터 삭제
            imageRepository.delete(image)
            
            logger.info("이미지 삭제 완료: imageId=$imageId")
            return ApiResponse.success(Unit, "이미지가 성공적으로 삭제되었습니다")
            
        } catch (e: Exception) {
            logger.error("이미지 삭제 실패: ${e.message}", e)
            return ApiResponse.error("IMAGE_DELETE_FAILED", "이미지 삭제에 실패했습니다: ${e.message}")
        }
    }
    
    /**
     * 임시 이미지 정리 (스케줄러에서 호출)
     */
    fun cleanupTemporaryImages() {
        try {
            val cutoffTime = LocalDateTime.now().minusHours(24) // 24시간 이상 된 임시 파일
            val tempImages = imageRepository.findByIsTemporaryTrueAndCreatedAtBefore(cutoffTime)
            
            tempImages.forEach { image ->
                try {
                    // MinIO에서 파일 삭제
                    minioStorageService.deleteFile(image.storedPath)
                    image.thumbnailPath?.let { minioStorageService.deleteFile(it) }
                    image.mediumPath?.let { minioStorageService.deleteFile(it) }
                    
                    // DB에서 삭제
                    imageRepository.delete(image)
                    
                    logger.info("임시 이미지 정리: imageId=${image.id!!}")
                } catch (e: Exception) {
                    logger.warn("임시 이미지 정리 실패: imageId=${image.id!!}, error=${e.message}")
                }
            }
            
            logger.info("임시 이미지 정리 완료: 삭제된 파일 수=${tempImages.size}")
            
        } catch (e: Exception) {
            logger.error("임시 이미지 정리 작업 실패: ${e.message}", e)
        }
    }
    
    // === Private Methods ===
    
    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("파일이 비어있습니다")
        }
        
        if (file.size > 10 * 1024 * 1024) { // 10MB 제한
            throw IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다")
        }
        
        val allowedTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        if (file.contentType !in allowedTypes) {
            throw IllegalArgumentException("지원하지 않는 파일 형식입니다. (지원: JPEG, PNG, WebP)")
        }
    }
    
    private fun getFileExtension(filename: String?): String {
        return filename?.substringAfterLast('.', "jpg") ?: "jpg"
    }
    
    private fun generateUniqueFileName(extension: String): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
        return "${timestamp}_${uuid}.$extension"
    }
}