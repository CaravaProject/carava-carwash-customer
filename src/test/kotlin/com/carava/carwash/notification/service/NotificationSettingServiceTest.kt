package com.carava.carwash.notification.service

import com.carava.carwash.notification.dto.GlobalNotificationSettingUpdateRequestDto
import com.carava.carwash.notification.dto.NotificationSettingUpdateRequestDto
import com.carava.carwash.notification.entity.*
import com.carava.carwash.notification.repository.NotificationSettingRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationSettingServiceTest {

    private val notificationSettingRepository = mockk<NotificationSettingRepository>()
    private val notificationSettingService = NotificationSettingService(notificationSettingRepository)

    private val userId = 1L
    private val userType = RecipientType.CUSTOMER

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    @DisplayName("사용자 알림 설정 조회 - 성공")
    fun getUserNotificationSettings_Success() {
        // Given
        val settings = listOf(
            NotificationSetting.createDefault(userId, userType, NotificationType.RESERVATION_CONFIRMED),
            NotificationSetting.createDefault(userId, userType, NotificationType.RESERVATION_REMINDER_2H)
        )

        every { notificationSettingRepository.findByUserIdAndUserType(userId, userType) } returns settings

        // When
        val result = notificationSettingService.getUserNotificationSettings(userId, userType)

        // Then
        assertTrue(result.success)
        assertEquals("알림 설정 조회가 완료되었습니다", result.message)
        assertEquals(2, result.data?.settings?.size)
    }

    @Test
    @DisplayName("사용자 알림 설정 조회 - 설정 없음 (기본 설정 생성)")
    fun getUserNotificationSettings_NoSettings_CreateDefault() {
        // Given
        every { notificationSettingRepository.findByUserIdAndUserType(userId, userType) } returns emptyList()
        every { notificationSettingRepository.saveAll<NotificationSetting>(any()) } returns emptyList()

        // When
        val result = notificationSettingService.getUserNotificationSettings(userId, userType)

        // Then
        assertTrue(result.success)
        verify { notificationSettingRepository.saveAll<NotificationSetting>(any()) }
    }

    @Test
    @DisplayName("특정 알림 유형 설정 수정 - 성공")
    fun updateNotificationTypeSetting_Success() {
        // Given
        val notificationType = NotificationType.RESERVATION_CONFIRMED
        val setting = NotificationSetting.createDefault(userId, userType, notificationType)
        
        val request = NotificationSettingUpdateRequestDto(
            pushEnabled = false,
            smsEnabled = true,
            quietStartHour = 22,
            quietEndHour = 8
        )

        every { 
            notificationSettingRepository.findByUserIdAndUserTypeAndNotificationType(
                userId, userType, notificationType
            ) 
        } returns Optional.of(setting)
        every { notificationSettingRepository.save(any()) } returns setting

        // When
        val result = notificationSettingService.updateNotificationTypeSetting(
            userId, userType, notificationType, request
        )

        // Then
        assertTrue(result.success)
        assertEquals("알림 설정이 성공적으로 수정되었습니다", result.message)
        assertFalse(result.data?.pushEnabled ?: true)
        assertTrue(result.data?.smsEnabled ?: false)
        assertEquals(22, result.data?.quietStartHour)
        assertEquals(8, result.data?.quietEndHour)

        verify { notificationSettingRepository.save(any()) }
    }

    @Test
    @DisplayName("특정 알림 유형 설정 수정 - 설정 없음 (새로 생성)")
    fun updateNotificationTypeSetting_NoSetting_CreateNew() {
        // Given
        val notificationType = NotificationType.RESERVATION_CONFIRMED
        val newSetting = NotificationSetting.createDefault(userId, userType, notificationType)
        
        val request = NotificationSettingUpdateRequestDto(
            pushEnabled = false,
            smsEnabled = true
        )

        every { 
            notificationSettingRepository.findByUserIdAndUserTypeAndNotificationType(
                userId, userType, notificationType
            ) 
        } returns Optional.empty()
        every { notificationSettingRepository.save(any()) } returns newSetting

        // When
        val result = notificationSettingService.updateNotificationTypeSetting(
            userId, userType, notificationType, request
        )

        // Then
        assertTrue(result.success)
        assertEquals("알림 설정이 성공적으로 수정되었습니다", result.message)

        verify { notificationSettingRepository.save(any()) }
    }

    @Test
    @DisplayName("전체 알림 설정 수정 - 성공")
    fun updateGlobalNotificationSettings_Success() {
        // Given
        val settings = listOf(
            NotificationSetting.createDefault(userId, userType, NotificationType.RESERVATION_CONFIRMED),
            NotificationSetting.createDefault(userId, userType, NotificationType.PROMOTION)
        )
        
        val request = GlobalNotificationSettingUpdateRequestDto(
            globalPushEnabled = false,
            globalSmsEnabled = true,
            marketingEnabled = false
        )

        every { notificationSettingRepository.findByUserIdAndUserType(userId, userType) } returns settings
        every { notificationSettingRepository.saveAll<NotificationSetting>(any()) } returns settings

        // When
        val result = notificationSettingService.updateGlobalNotificationSettings(userId, userType, request)

        // Then
        assertTrue(result.success)
        assertEquals("전체 알림 설정이 성공적으로 수정되었습니다", result.message)

        verify { notificationSettingRepository.saveAll<NotificationSetting>(any()) }
    }

    @Test
    @DisplayName("마케팅 알림 수신 거부 - 성공")
    fun disableMarketingNotifications_Success() {
        // Given
        val updatedCount = 2

        every { 
            notificationSettingRepository.disableMarketingNotifications(
                userId, userType, any()
            ) 
        } returns updatedCount

        // When
        val result = notificationSettingService.disableMarketingNotifications(userId, userType)

        // Then
        assertTrue(result.success)
        assertEquals("${updatedCount}개의 마케팅 알림이 비활성화되었습니다", result.message)
    }

    @Test
    @DisplayName("기본 알림 설정 생성 - 성공")
    fun createDefaultSettings_Success() {
        // Given
        every { notificationSettingRepository.findByUserIdAndUserType(userId, userType) } returns emptyList()
        every { notificationSettingRepository.saveAll<NotificationSetting>(any()) } answers { 
            firstArg<List<NotificationSetting>>() 
        }

        // When
        val result = notificationSettingService.createDefaultSettings(userId, userType)

        // Then
        assertTrue(result.isNotEmpty())
        assertEquals(NotificationType.values().size, result.size)

        verify { notificationSettingRepository.saveAll<NotificationSetting>(any()) }
    }

    @Test
    @DisplayName("기본 알림 설정 생성 - 이미 존재하는 경우")
    fun createDefaultSettings_AlreadyExists() {
        // Given
        val existingSettings = listOf(
            NotificationSetting.createDefault(userId, userType, NotificationType.RESERVATION_CONFIRMED)
        )

        every { notificationSettingRepository.findByUserIdAndUserType(userId, userType) } returns existingSettings

        // When
        val result = notificationSettingService.createDefaultSettings(userId, userType)

        // Then
        assertEquals(existingSettings, result)
        verify(exactly = 0) { notificationSettingRepository.saveAll<NotificationSetting>(any()) }
    }

    @Test
    @DisplayName("사용자 설정 삭제 - 성공")
    fun deleteUserSettings_Success() {
        // Given
        val deletedCount = 5

        every { notificationSettingRepository.deleteAllByUser(userId, userType) } returns deletedCount

        // When
        val result = notificationSettingService.deleteUserSettings(userId, userType)

        // Then
        assertTrue(result.success)
        assertEquals("${deletedCount}개의 알림 설정이 삭제되었습니다", result.message)
    }

    @Test
    @DisplayName("활성화된 채널 사용자 목록 조회 - 성공")
    fun getUsersWithEnabledChannel_Success() {
        // Given
        val channel = NotificationChannel.PUSH
        val expectedUserIds = listOf(1L, 2L, 3L)
        val settings = expectedUserIds.map { id ->
            NotificationSetting.createDefault(id, userType, NotificationType.RESERVATION_CONFIRMED)
        }

        every { 
            notificationSettingRepository.findByUserAndEnabledChannel(userType, channel.name) 
        } returns settings

        // When
        val result = notificationSettingService.getUsersWithEnabledChannel(userType, channel)

        // Then
        assertEquals(expectedUserIds, result)
    }

    @Test
    @DisplayName("마케팅 수신 동의 사용자 목록 조회 - 성공")
    fun getUsersWithMarketingConsent_Success() {
        // Given
        val channel = NotificationChannel.PUSH
        val expectedUserIds = listOf(1L, 2L, 3L)

        every { 
            notificationSettingRepository.findUserIdsWithMarketingConsent(
                userType = userType,
                marketingTypes = any(),
                channel = channel.name
            ) 
        } returns expectedUserIds

        // When
        val result = notificationSettingService.getUsersWithMarketingConsent(userType, channel)

        // Then
        assertEquals(expectedUserIds, result)
    }

    @Test
    @DisplayName("사용자 설정 조회 (내부용) - 설정 없을 때 기본 생성")
    fun getUserSettings_NoSettings_CreateDefault() {
        // Given
        every { notificationSettingRepository.findByUserIdAndUserType(userId, userType) } returns emptyList()
        every { notificationSettingRepository.saveAll<NotificationSetting>(any()) } answers { 
            firstArg<List<NotificationSetting>>() 
        }

        // When
        val result = notificationSettingService.getUserSettings(userId, userType)

        // Then
        assertTrue(result.isNotEmpty())
        assertEquals(NotificationType.values().size, result.size)

        verify { notificationSettingRepository.saveAll<NotificationSetting>(any()) }
    }

    @Test
    @DisplayName("사용자 설정 조회 (내부용) - 기존 설정 반환")
    fun getUserSettings_ExistingSettings() {
        // Given
        val existingSettings = listOf(
            NotificationSetting.createDefault(userId, userType, NotificationType.RESERVATION_CONFIRMED),
            NotificationSetting.createDefault(userId, userType, NotificationType.RESERVATION_REMINDER_2H)
        )

        every { notificationSettingRepository.findByUserIdAndUserType(userId, userType) } returns existingSettings

        // When
        val result = notificationSettingService.getUserSettings(userId, userType)

        // Then
        assertEquals(existingSettings, result)
        verify(exactly = 0) { notificationSettingRepository.saveAll<NotificationSetting>(any()) }
    }
}