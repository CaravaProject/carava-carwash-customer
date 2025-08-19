package com.carava.carwash.global.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import jakarta.annotation.PostConstruct

/**
 * Firebase 설정 클래스
 * Firebase Admin SDK를 초기화하고 FirebaseMessaging 빈을 생성
 */
@Configuration
class FirebaseConfig {

    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    /**
     * Firebase 초기화
     */
    @PostConstruct
    fun initializeFirebase() {
        try {
            // Firebase App이 이미 초기화되었는지 확인
            if (FirebaseApp.getApps().isEmpty()) {
                val serviceAccount = ClassPathResource("firebase-service-account-key.json")
                
                if (!serviceAccount.exists()) {
                    logger.error("Firebase service account key file not found: firebase-service-account-key.json")
                    throw IOException("Firebase service account key file not found")
                }

                val credentials = GoogleCredentials.fromStream(serviceAccount.inputStream)
                val options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId("carvana-431e7")
                    .build()

                FirebaseApp.initializeApp(options)
                logger.info("Firebase has been initialized successfully")
            } else {
                logger.info("Firebase is already initialized")
            }
        } catch (e: IOException) {
            logger.error("Failed to initialize Firebase", e)
            throw RuntimeException("Failed to initialize Firebase", e)
        }
    }

    /**
     * FirebaseMessaging 빈 생성
     */
    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}