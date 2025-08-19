package com.carava.carwash.global.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * MinIO 설정 클래스
 * AWS S3 호환 API를 사용하여 추후 S3 마이그레이션 용이
 */
@Configuration
class MinioConfig {

    @Value("\${minio.endpoint:http://158.179.171.102:9000}")
    private lateinit var endpoint: String

    @Value("\${minio.access-key:carvana_admin}")
    private lateinit var accessKey: String

    @Value("\${minio.secret-key:Carvana2024!}")
    private lateinit var secretKey: String

    @Value("\${minio.bucket-name:carava-bucket}")
    private lateinit var bucketName: String

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }

    @Bean
    fun minioBucketName(): String {
        return bucketName
    }
}