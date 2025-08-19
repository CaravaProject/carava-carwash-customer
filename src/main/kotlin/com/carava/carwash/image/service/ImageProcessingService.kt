package com.carava.carwash.image.service

import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * 이미지 처리 서비스
 * 이미지 리사이징, 압축, 유효성 검증 등
 */
@Service
class ImageProcessingService {

    private val logger = LoggerFactory.getLogger(ImageProcessingService::class.java)

    @Value("\${image.thumbnail.width:300}")
    private var thumbnailWidth: Int = 300

    @Value("\${image.thumbnail.height:300}")
    private var thumbnailHeight: Int = 300

    @Value("\${image.medium.width:800}")
    private var mediumWidth: Int = 800

    @Value("\${image.medium.height:600}")
    private var mediumHeight: Int = 600

    @Value("\${image.max-width:4096}")
    private var maxWidth: Int = 4096

    @Value("\${image.max-height:4096}")
    private var maxHeight: Int = 4096

    /**
     * 이미지 유효성 검증
     */
    fun validateImage(file: MultipartFile): ImageValidationResult {
        try {
            // MIME 타입 검증
            val mimeType = file.contentType ?: ""
            if (!isValidMimeType(mimeType)) {
                return ImageValidationResult(
                    isValid = false,
                    errorMessage = "지원하지 않는 이미지 형식입니다. (JPG, PNG, WEBP만 지원)"
                )
            }

            // 파일 크기 검증
            if (file.size > 10_000_000L) { // 10MB
                return ImageValidationResult(
                    isValid = false,
                    errorMessage = "파일 크기가 너무 큽니다. (최대 10MB)"
                )
            }

            // 이미지 파일인지 확인 및 크기 검증
            file.inputStream.use { inputStream ->
                val bufferedImage = ImageIO.read(inputStream)
                    ?: return ImageValidationResult(
                        isValid = false,
                        errorMessage = "유효하지 않은 이미지 파일입니다."
                    )

                if (bufferedImage.width > maxWidth || bufferedImage.height > maxHeight) {
                    return ImageValidationResult(
                        isValid = false,
                        errorMessage = "이미지 해상도가 너무 큽니다. (최대 ${maxWidth}x${maxHeight})"
                    )
                }

                return ImageValidationResult(
                    isValid = true,
                    width = bufferedImage.width,
                    height = bufferedImage.height,
                    mimeType = mimeType
                )
            }
        } catch (e: Exception) {
            logger.error("이미지 유효성 검증 실패", e)
            return ImageValidationResult(
                isValid = false,
                errorMessage = "이미지 파일을 읽을 수 없습니다."
            )
        }
    }

    /**
     * 썸네일 생성 (ByteArray 입력)
     */
    fun createThumbnail(inputBytes: ByteArray): ByteArray {
        return try {
            val bufferedImage = ImageIO.read(ByteArrayInputStream(inputBytes))
            val thumbnail = Thumbnails.of(bufferedImage)
                .size(thumbnailWidth, thumbnailHeight)
                .outputQuality(0.8)
                .outputFormat("jpg")
                .asBufferedImage()

            ByteArrayOutputStream().use { outputStream ->
                ImageIO.write(thumbnail, "jpg", outputStream)
                outputStream.toByteArray()
            }
        } catch (e: Exception) {
            logger.error("썸네일 생성 실패: ${e.message}", e)
            throw RuntimeException("썸네일 생성에 실패했습니다", e)
        }
    }

    /**
     * 썸네일 생성 (InputStream 입력)
     */
    fun generateThumbnail(inputStream: InputStream, outputFormat: String = "jpg"): ByteArray? {
        return try {
            val outputStream = ByteArrayOutputStream()
            
            Thumbnails.of(inputStream)
                .size(thumbnailWidth, thumbnailHeight)
                .outputFormat(outputFormat)
                .outputQuality(0.8) // 80% 품질
                .toOutputStream(outputStream)
                
            outputStream.toByteArray()
        } catch (e: Exception) {
            logger.error("썸네일 생성 실패", e)
            null
        }
    }

    /**
     * 중간 크기 이미지 생성 (ByteArray 입력)
     */
    fun createMediumImage(inputBytes: ByteArray): ByteArray {
        return try {
            val bufferedImage = ImageIO.read(ByteArrayInputStream(inputBytes))
            val medium = Thumbnails.of(bufferedImage)
                .size(mediumWidth, mediumHeight)
                .outputQuality(0.9)
                .outputFormat("jpg")
                .asBufferedImage()

            ByteArrayOutputStream().use { outputStream ->
                ImageIO.write(medium, "jpg", outputStream)
                outputStream.toByteArray()
            }
        } catch (e: Exception) {
            logger.error("중간 크기 이미지 생성 실패: ${e.message}", e)
            throw RuntimeException("중간 크기 이미지 생성에 실패했습니다", e)
        }
    }

    /**
     * 중간 크기 이미지 생성 (InputStream 입력)
     */
    fun generateMediumImage(inputStream: InputStream, outputFormat: String = "jpg"): ByteArray? {
        return try {
            val outputStream = ByteArrayOutputStream()
            
            Thumbnails.of(inputStream)
                .size(mediumWidth, mediumHeight)
                .outputFormat(outputFormat)
                .outputQuality(0.9) // 90% 품질
                .toOutputStream(outputStream)
                
            outputStream.toByteArray()
        } catch (e: Exception) {
            logger.error("중간 크기 이미지 생성 실패", e)
            null
        }
    }

    /**
     * 이미지 압축 (원본 크기 유지)
     */
    fun compressImage(inputStream: InputStream, outputFormat: String = "jpg", quality: Double = 0.85): ByteArray? {
        return try {
            val outputStream = ByteArrayOutputStream()
            
            Thumbnails.of(inputStream)
                .scale(1.0) // 크기 유지
                .outputFormat(outputFormat)
                .outputQuality(quality)
                .toOutputStream(outputStream)
                
            outputStream.toByteArray()
        } catch (e: Exception) {
            logger.error("이미지 압축 실패", e)
            null
        }
    }

    /**
     * 이미지 크기 조정 (최대 크기 제한)
     */
    fun resizeIfNeeded(inputStream: InputStream, outputFormat: String = "jpg"): ProcessedImageResult? {
        return try {
            val bufferedImage = ImageIO.read(inputStream)
                ?: return null

            val needsResize = bufferedImage.width > maxWidth || bufferedImage.height > maxHeight
            
            if (!needsResize) {
                // 크기 조정이 필요 없으면 압축만 수행
                val compressedData = compressImage(
                    ByteArrayInputStream(inputStream.readAllBytes()),
                    outputFormat
                ) ?: return null
                
                return ProcessedImageResult(
                    data = compressedData,
                    width = bufferedImage.width,
                    height = bufferedImage.height,
                    wasResized = false
                )
            }

            // 크기 조정 필요
            val outputStream = ByteArrayOutputStream()
            
            Thumbnails.of(bufferedImage)
                .size(maxWidth, maxHeight)
                .outputFormat(outputFormat)
                .outputQuality(0.9)
                .toOutputStream(outputStream)
                
            // 조정된 크기 계산
            val scaleFactor = minOf(
                maxWidth.toDouble() / bufferedImage.width,
                maxHeight.toDouble() / bufferedImage.height
            )
            
            val newWidth = (bufferedImage.width * scaleFactor).toInt()
            val newHeight = (bufferedImage.height * scaleFactor).toInt()
                
            ProcessedImageResult(
                data = outputStream.toByteArray(),
                width = newWidth,
                height = newHeight,
                wasResized = true
            )
        } catch (e: Exception) {
            logger.error("이미지 크기 조정 실패", e)
            null
        }
    }

    /**
     * MIME 타입 유효성 검증
     */
    private fun isValidMimeType(mimeType: String): Boolean {
        val validMimeTypes = setOf(
            "image/jpeg",
            "image/jpg", 
            "image/png",
            "image/webp"
        )
        return validMimeTypes.contains(mimeType.lowercase())
    }

    /**
     * 파일 확장자 추출
     */
    fun getFileExtension(filename: String): String {
        return filename.substringAfterLast('.', "").lowercase()
    }

    /**
     * MIME 타입에서 파일 확장자 추출
     */
    fun getExtensionFromMimeType(mimeType: String): String {
        return when (mimeType.lowercase()) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg" // 기본값
        }
    }

    /**
     * 이미지 유효성 검증 결과
     */
    data class ImageValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val width: Int? = null,
        val height: Int? = null,
        val mimeType: String? = null
    )

    /**
     * 처리된 이미지 결과
     */
    data class ProcessedImageResult(
        val data: ByteArray,
        val width: Int,
        val height: Int,
        val wasResized: Boolean
    )
}