package com.carava.carwash.image.service

import io.minio.*
import io.minio.errors.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.Duration

/**
 * MinIO 스토리지 서비스
 * AWS S3 호환 API 사용으로 추후 S3 마이그레이션 용이
 */
@Service
class MinioStorageService(
    private val minioClient: MinioClient,
    @Qualifier("minioBucketName") private val bucketName: String
) {

    private val logger = LoggerFactory.getLogger(MinioStorageService::class.java)

    init {
        // 애플리케이션 시작 시 버킷 존재 확인 및 생성
        initializeBucket()
    }

    /**
     * 버킷 초기화
     */
    private fun initializeBucket() {
        try {
            val bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            )

            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                )
                logger.info("MinIO 버킷 생성 완료: $bucketName")
            } else {
                logger.info("MinIO 버킷 연결 확인: $bucketName")
            }
        } catch (e: Exception) {
            logger.error("MinIO 버킷 초기화 실패: ${e.message}", e)
            throw RuntimeException("MinIO 초기화 실패", e)
        }
    }

    /**
     * 파일 업로드
     */
    fun uploadFile(
        objectKey: String,
        inputStream: InputStream,
        contentType: String,
        fileSize: Long
    ): Boolean {
        return try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectKey)
                    .stream(inputStream, fileSize, -1)
                    .contentType(contentType)
                    .build()
            )
            
            logger.debug("파일 업로드 성공: $objectKey")
            true
        } catch (e: Exception) {
            logger.error("파일 업로드 실패: $objectKey", e)
            false
        }
    }

    /**
     * 파일 다운로드
     */
    fun downloadFile(objectKey: String): InputStream? {
        return try {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectKey)
                    .build()
            )
        } catch (e: Exception) {
            logger.error("파일 다운로드 실패: $objectKey", e)
            null
        }
    }

    /**
     * 파일 삭제
     */
    fun deleteFile(objectKey: String): Boolean {
        return try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectKey)
                    .build()
            )
            
            logger.debug("파일 삭제 성공: $objectKey")
            true
        } catch (e: Exception) {
            logger.error("파일 삭제 실패: $objectKey", e)
            false
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    fun fileExists(objectKey: String): Boolean {
        return try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectKey)
                    .build()
            )
            true
        } catch (e: ErrorResponseException) {
            if (e.errorResponse().code() == "NoSuchKey") {
                false
            } else {
                logger.error("파일 존재 확인 실패: $objectKey", e)
                false
            }
        } catch (e: Exception) {
            logger.error("파일 존재 확인 중 오류: $objectKey", e)
            false
        }
    }

    /**
     * 파일 정보 조회
     */
    fun getFileInfo(objectKey: String): FileInfo? {
        return try {
            val statObject = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectKey)
                    .build()
            )
            
            FileInfo(
                objectKey = objectKey,
                size = statObject.size(),
                contentType = statObject.contentType(),
                lastModified = statObject.lastModified(),
                etag = statObject.etag()
            )
        } catch (e: Exception) {
            logger.error("파일 정보 조회 실패: $objectKey", e)
            null
        }
    }

    /**
     * 공개 URL 생성 (MinIO 엔드포인트 기반)
     */
    fun generatePublicUrl(objectKey: String): String {
        return "http://158.179.171.102:9000/$bucketName/$objectKey"
    }

    /**
     * 여러 파일 삭제
     */
    fun deleteFiles(objectKeys: List<String>): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        
        objectKeys.forEach { objectKey ->
            results[objectKey] = deleteFile(objectKey)
        }
        
        return results
    }

    /**
     * 파일 이동 (복사 후 원본 삭제)
     */
    fun moveFile(sourceKey: String, destinationKey: String) {
        try {
            // 1. 파일 복사
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(destinationKey)
                    .source(
                        CopySource.builder()
                            .bucket(bucketName)
                            .`object`(sourceKey)
                            .build()
                    )
                    .build()
            )

            // 2. 원본 파일 삭제
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(sourceKey)
                    .build()
            )

            logger.info("파일 이동 완료: $sourceKey -> $destinationKey")

        } catch (e: Exception) {
            logger.error("파일 이동 실패: $sourceKey -> $destinationKey, 오류: ${e.message}", e)
            throw RuntimeException("파일 이동에 실패했습니다: ${e.message}", e)
        }
    }

    /**
     * 파일 정보 데이터 클래스
     */
    data class FileInfo(
        val objectKey: String,
        val size: Long,
        val contentType: String,
        val lastModified: java.time.ZonedDateTime,
        val etag: String
    )
}