# Carava 백엔드 개발 가이드

## 프로젝트 구조

```
src/main/kotlin/com/carava/
├── CaravaApplication.kt
├── config/
│   ├── SecurityConfig.kt
│   ├── JwtConfig.kt
│   └── DatabaseConfig.kt
├── domain/
│   ├── user/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   ├── shop/
│   ├── reservation/
│   ├── message/
│   └── review/
├── common/
│   ├── exception/
│   ├── response/
│   ├── util/
│   └── security/
└── external/
    ├── storage/
    ├── notification/
    └── payment/
```

## 핵심 엔티티 구현 예시

### 1. User 엔티티

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val nickname: String,

    @Column(nullable = false)
    val phone: String,

    @Column(nullable = false)
    val address: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val userType: UserType,

    @Enumerated(EnumType.STRING)
    val socialProvider: SocialProvider? = null,

    val socialId: String? = null,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class UserType {
    CUSTOMER, BUSINESS_OWNER, ADMIN
}

enum class SocialProvider {
    GOOGLE, KAKAO, NAVER
}
```

### 2. Shop 엔티티

```kotlin
@Entity
@Table(name = "shops")
data class Shop(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val ownerId: Long,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val address: String,

    @Column(nullable = false)
    val phone: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false)
    val operatingHours: String, // JSON 형태로 저장

    @Column(nullable = false)
    val holidays: String, // JSON 형태로 저장

    @Column(nullable = false, precision = 3, scale = 2)
    val rating: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val reviewCount: Int = 0,

    @Column(nullable = false)
    val isActive: Boolean = true,

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "shop", cascade = [CascadeType.ALL])
    val menus: List<Menu> = emptyList(),

    @OneToMany(mappedBy = "shop", cascade = [CascadeType.ALL])
    val images: List<ShopImage> = emptyList(),

    @OneToMany(mappedBy = "shop", cascade = [CascadeType.ALL])
    val socialLinks: List<SocialLink> = emptyList()
)
```

### 3. Reservation 엔티티

```kotlin
@Entity
@Table(name = "reservations")
data class Reservation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val customerId: Long,

    @Column(nullable = false)
    val shopId: Long,

    @Column(nullable = false)
    val reservationDate: LocalDate,

    @Column(nullable = false)
    val startTime: LocalTime,

    @Column(nullable = false)
    val endTime: LocalTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ReservationStatus = ReservationStatus.PENDING,

    @Column(columnDefinition = "TEXT")
    val specialRequests: String? = null,

    @Column(nullable = false)
    val totalPrice: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod = PaymentMethod.ON_SITE,

    val rejectionReason: String? = null,

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "reservation", cascade = [CascadeType.ALL])
    val reservationMenus: List<ReservationMenu> = emptyList(),

    @OneToMany(mappedBy = "reservation", cascade = [CascadeType.ALL])
    val attachments: List<ReservationAttachment> = emptyList()
)

enum class ReservationStatus {
    PENDING, APPROVED, COMPLETED, CANCELLED, REJECTED
}

enum class PaymentMethod {
    ON_SITE, CARD, BANK_TRANSFER, MOBILE_PAY
}
```

## Repository 구현 예시

### 1. ShopRepository

```kotlin
@Repository
interface ShopRepository : JpaRepository<Shop, Long> {
    
    fun findByOwnerIdAndIsActiveTrue(ownerId: Long): Shop?
    
    fun findByIsActiveTrueOrderByRatingDesc(): List<Shop>
    
    @Query("""
        SELECT s FROM Shop s 
        WHERE s.isActive = true 
        AND (:name IS NULL OR s.name LIKE %:name%)
        AND (:address IS NULL OR s.address LIKE %:address%)
        AND (:minRating IS NULL OR s.rating >= :minRating)
        ORDER BY s.rating DESC, s.reviewCount DESC
    """)
    fun searchShops(
        @Param("name") name: String?,
        @Param("address") address: String?,
        @Param("minRating") minRating: BigDecimal?,
        pageable: Pageable
    ): Page<Shop>
    
    @Query("""
        SELECT DISTINCT s FROM Shop s 
        JOIN s.menus m 
        WHERE s.isActive = true 
        AND m.isActive = true
        AND NOT EXISTS (
            SELECT r FROM Reservation r 
            WHERE r.shopId = s.id 
            AND r.reservationDate = :date 
            AND r.status IN ('PENDING', 'APPROVED')
            AND ((r.startTime <= :startTime AND r.endTime > :startTime) 
                 OR (r.startTime < :endTime AND r.endTime >= :endTime)
                 OR (r.startTime >= :startTime AND r.endTime <= :endTime))
        )
    """)
    fun findAvailableShops(
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime
    ): List<Shop>
}
```

### 2. ReservationRepository

```kotlin
@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {
    
    fun findByCustomerIdOrderByReservationDateDescStartTimeDesc(customerId: Long): List<Reservation>
    
    fun findByShopIdAndStatusOrderByReservationDateAscStartTimeAsc(
        shopId: Long, 
        status: ReservationStatus
    ): List<Reservation>
    
    @Query("""
        SELECT r FROM Reservation r 
        WHERE r.shopId = :shopId 
        AND r.reservationDate = :date 
        AND r.status IN ('PENDING', 'APPROVED')
        AND ((r.startTime <= :endTime AND r.endTime > :startTime))
    """)
    fun findConflictingReservations(
        @Param("shopId") shopId: Long,
        @Param("date") date: LocalDate,
        @Param("startTime") startTime: LocalTime,
        @Param("endTime") endTime: LocalTime
    ): List<Reservation>
    
    @Query("""
        SELECT COUNT(r) FROM Reservation r 
        WHERE r.shopId = :shopId 
        AND r.status = 'COMPLETED'
        AND YEAR(r.createdAt) = :year 
        AND MONTH(r.createdAt) = :month
    """)
    fun countCompletedReservationsByMonth(
        @Param("shopId") shopId: Long,
        @Param("year") year: Int,
        @Param("month") month: Int
    ): Long
    
    @Query("""
        SELECT SUM(r.totalPrice) FROM Reservation r 
        WHERE r.shopId = :shopId 
        AND r.status = 'COMPLETED'
        AND YEAR(r.createdAt) = :year 
        AND MONTH(r.createdAt) = :month
    """)
    fun sumRevenueByMonth(
        @Param("shopId") shopId: Long,
        @Param("year") year: Int,
        @Param("month") month: Int
    ): Long?
}
```

## Service 구현 예시

### 1. ReservationService

```kotlin
@Service
@Transactional
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val shopRepository: ShopRepository,
    private val menuRepository: MenuRepository,
    private val userRepository: UserRepository
) {
    
    fun createReservation(request: CreateReservationRequest, customerId: Long): ReservationResponse {
        // 1. 업체 존재 확인
        val shop = shopRepository.findById(request.shopId)
            .orElseThrow { ShopNotFoundException("Shop not found") }
        
        // 2. 메뉴 유효성 검증
        val menus = menuRepository.findAllById(request.menuIds)
        if (menus.size != request.menuIds.size) {
            throw InvalidMenuException("Some menus not found")
        }
        
        // 3. 총 소요시간 계산
        val totalDuration = menus.sumOf { it.duration }
        val endTime = request.startTime.plusMinutes(totalDuration.toLong())
        
        // 4. 시간 충돌 검사
        val conflictingReservations = reservationRepository.findConflictingReservations(
            request.shopId, request.reservationDate, request.startTime, endTime
        )
        
        if (conflictingReservations.isNotEmpty()) {
            throw TimeConflictException("Time slot is already booked")
        }
        
        // 5. 총 가격 계산
        val totalPrice = menus.sumOf { it.price }
        
        // 6. 예약 생성
        val reservation = Reservation(
            customerId = customerId,
            shopId = request.shopId,
            reservationDate = request.reservationDate,
            startTime = request.startTime,
            endTime = endTime,
            specialRequests = request.specialRequests,
            totalPrice = totalPrice,
            paymentMethod = request.paymentMethod
        )
        
        val savedReservation = reservationRepository.save(reservation)
        
        // 7. 예약 메뉴 연결
        val reservationMenus = menus.map { menu ->
            ReservationMenu(
                reservationId = savedReservation.id,
                menuId = menu.id,
                price = menu.price,
                duration = menu.duration
            )
        }
        reservationMenuRepository.saveAll(reservationMenus)
        
        return ReservationResponse.from(savedReservation, menus)
    }
    
    fun approveReservation(reservationId: Long, shopOwnerId: Long): ReservationResponse {
        val reservation = getReservationWithShopOwnerCheck(reservationId, shopOwnerId)
        
        if (reservation.status != ReservationStatus.PENDING) {
            throw InvalidReservationStatusException("Only pending reservations can be approved")
        }
        
        val updatedReservation = reservation.copy(
            status = ReservationStatus.APPROVED,
            updatedAt = LocalDateTime.now()
        )
        
        return ReservationResponse.from(reservationRepository.save(updatedReservation))
    }
    
    fun rejectReservation(
        reservationId: Long, 
        shopOwnerId: Long, 
        rejectionReason: String
    ): ReservationResponse {
        val reservation = getReservationWithShopOwnerCheck(reservationId, shopOwnerId)
        
        if (reservation.status != ReservationStatus.PENDING) {
            throw InvalidReservationStatusException("Only pending reservations can be rejected")
        }
        
        val updatedReservation = reservation.copy(
            status = ReservationStatus.REJECTED,
            rejectionReason = rejectionReason,
            updatedAt = LocalDateTime.now()
        )
        
        return ReservationResponse.from(reservationRepository.save(updatedReservation))
    }
    
    private fun getReservationWithShopOwnerCheck(reservationId: Long, shopOwnerId: Long): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { ReservationNotFoundException("Reservation not found") }
        
        val shop = shopRepository.findById(reservation.shopId)
            .orElseThrow { ShopNotFoundException("Shop not found") }
        
        if (shop.ownerId != shopOwnerId) {
            throw UnauthorizedException("Not authorized to access this reservation")
        }
        
        return reservation
    }
}
```

## Controller 구현 예시

### 1. ReservationController

```kotlin
@RestController
@RequestMapping("/api/reservations")
@Validated
class ReservationController(
    private val reservationService: ReservationService
) {
    
    @PostMapping
    fun createReservation(
        @Valid @RequestBody request: CreateReservationRequest,
        @AuthenticationPrincipal userDetails: UserPrincipal
    ): ResponseEntity<ApiResponse<ReservationResponse>> {
        val response = reservationService.createReservation(request, userDetails.userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    @GetMapping
    fun getMyReservations(
        @AuthenticationPrincipal userDetails: UserPrincipal,
        @RequestParam(defaultValue = "ALL") status: String,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<ReservationResponse>>> {
        val response = reservationService.getCustomerReservations(
            userDetails.userId, 
            status, 
            pageable
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    fun approveReservation(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserPrincipal
    ): ResponseEntity<ApiResponse<ReservationResponse>> {
        val response = reservationService.approveReservation(id, userDetails.userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
    
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('BUSINESS_OWNER')")
    fun rejectReservation(
        @PathVariable id: Long,
        @Valid @RequestBody request: RejectReservationRequest,
        @AuthenticationPrincipal userDetails: UserPrincipal
    ): ResponseEntity<ApiResponse<ReservationResponse>> {
        val response = reservationService.rejectReservation(
            id, 
            userDetails.userId, 
            request.rejectionReason
        )
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
```

## DTO 및 Request/Response 클래스

### 1. Request DTOs

```kotlin
data class CreateReservationRequest(
    @field:NotNull(message = "Shop ID is required")
    val shopId: Long,
    
    @field:NotEmpty(message = "At least one menu must be selected")
    val menuIds: List<Long>,
    
    @field:NotNull(message = "Reservation date is required")
    @field:FutureOrPresent(message = "Reservation date must be today or in the future")
    val reservationDate: LocalDate,
    
    @field:NotNull(message = "Start time is required")
    val startTime: LocalTime,
    
    @field:Size(max = 500, message = "Special requests must be less than 500 characters")
    val specialRequests: String? = null,
    
    @field:NotNull(message = "Payment method is required")
    val paymentMethod: PaymentMethod = PaymentMethod.ON_SITE,
    
    val attachmentUrls: List<String> = emptyList()
)

data class RejectReservationRequest(
    @field:NotBlank(message = "Rejection reason is required")
    @field:Size(max = 200, message = "Rejection reason must be less than 200 characters")
    val rejectionReason: String
)

data class ShopSearchRequest(
    val name: String? = null,
    val address: String? = null,
    val serviceTypes: List<ServiceType> = emptyList(),
    val minRating: BigDecimal? = null,
    val availableDate: LocalDate? = null,
    val availableStartTime: LocalTime? = null,
    val availableEndTime: LocalTime? = null
)
```

### 2. Response DTOs

```kotlin
data class ReservationResponse(
    val id: Long,
    val shopId: Long,
    val shopName: String,
    val menus: List<MenuSummary>,
    val reservationDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: ReservationStatus,
    val specialRequests: String?,
    val totalPrice: Int,
    val paymentMethod: PaymentMethod,
    val rejectionReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(reservation: Reservation, menus: List<Menu> = emptyList()): ReservationResponse {
            return ReservationResponse(
                id = reservation.id,
                shopId = reservation.shopId,
                shopName = "", // 추후 Shop 정보 조회하여 설정
                menus = menus.map { MenuSummary.from(it) },
                reservationDate = reservation.reservationDate,
                startTime = reservation.startTime,
                endTime = reservation.endTime,
                status = reservation.status,
                specialRequests = reservation.specialRequests,
                totalPrice = reservation.totalPrice,
                paymentMethod = reservation.paymentMethod,
                rejectionReason = reservation.rejectionReason,
                createdAt = reservation.createdAt,
                updatedAt = reservation.updatedAt
            )
        }
    }
}

data class ShopResponse(
    val id: Long,
    val name: String,
    val address: String,
    val phone: String,
    val description: String?,
    val operatingHours: Map<String, String>,
    val holidays: List<String>,
    val rating: BigDecimal,
    val reviewCount: Int,
    val images: List<String>,
    val socialLinks: List<SocialLinkResponse>,
    val menus: List<MenuResponse>,
    val isLiked: Boolean = false
)

data class DashboardResponse(
    val todayReservations: List<ReservationSummary>,
    val monthlyStats: MonthlyStats,
    val recentReviews: List<ReviewSummary>
)

data class MonthlyStats(
    val revenue: Long,
    val completedJobs: Long,
    val reviewCount: Long,
    val targetRevenue: Long?,
    val achievementRate: Double?
)
```

## 예외 처리

### 1. 커스텀 예외 클래스

```kotlin
sealed class CaravaException(
    message: String,
    val errorCode: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class UserNotFoundException(message: String = "User not found") : 
    CaravaException(message, "USER_NOT_FOUND")

class ShopNotFoundException(message: String = "Shop not found") : 
    CaravaException(message, "SHOP_NOT_FOUND")

class ReservationNotFoundException(message: String = "Reservation not found") : 
    CaravaException(message, "RESERVATION_NOT_FOUND")

class TimeConflictException(message: String = "Time slot conflict") : 
    CaravaException(message, "TIME_CONFLICT")

class InvalidReservationStatusException(message: String) : 
    CaravaException(message, "INVALID_RESERVATION_STATUS")

class UnauthorizedException(message: String = "Unauthorized access") : 
    CaravaException(message, "UNAUTHORIZED")
```

### 2. 글로벌 예외 핸들러

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(this::class.java)
    
    @ExceptionHandler(CaravaException::class)
    fun handleCaravaException(ex: CaravaException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Carava exception occurred: ${ex.message}", ex)
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.errorCode, ex.message ?: "Unknown error"))
    }
    
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors.associate { 
            it.field to (it.defaultMessage ?: "Invalid value") 
        }
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors))
    }
    
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("ACCESS_DENIED", "Access denied"))
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "Internal server error"))
    }
}
```

## 공통 응답 형식

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }
        
        fun <T> error(
            code: String, 
            message: String, 
            details: Any? = null
        ): ApiResponse<T> {
            return ApiResponse(
                success = false, 
                error = ErrorResponse(code, message, details)
            )
        }
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Any? = null
)
```

## 테스트 예시

### 1. Service 테스트

```kotlin
@ExtendWith(MockitoExtension::class)
class ReservationServiceTest {
    
    @Mock
    private lateinit var reservationRepository: ReservationRepository
    
    @Mock
    private lateinit var shopRepository: ShopRepository
    
    @Mock
    private lateinit var menuRepository: MenuRepository
    
    @InjectMocks
    private lateinit var reservationService: ReservationService
    
    @Test
    fun `예약 생성 성공`() {
        // Given
        val customerId = 1L
        val shopId = 1L
        val menuIds = listOf(1L, 2L)
        
        val shop = createTestShop(shopId)
        val menus = createTestMenus(menuIds)
        val request = CreateReservationRequest(
            shopId = shopId,
            menuIds = menuIds,
            reservationDate = LocalDate.now().plusDays(1),
            startTime = LocalTime.of(10, 0),
            paymentMethod = PaymentMethod.ON_SITE
        )
        
        whenever(shopRepository.findById(shopId)).thenReturn(Optional.of(shop))
        whenever(menuRepository.findAllById(menuIds)).thenReturn(menus)
        whenever(reservationRepository.findConflictingReservations(any(), any(), any(), any()))
            .thenReturn(emptyList())
        whenever(reservationRepository.save(any<Reservation>()))
            .thenAnswer { it.arguments[0] as Reservation }
        
        // When
        val result = reservationService.createReservation(request, customerId)
        
        // Then
        assertThat(result.shopId).isEqualTo(shopId)
        assertThat(result.totalPrice).isEqualTo(menus.sumOf { it.price })
        verify(reservationRepository).save(any<Reservation>())
    }
    
    @Test
    fun `시간 충돌로 인한 예약 실패`() {
        // Given
        val customerId = 1L
        val shopId = 1L
        val menuIds = listOf(1L)
        
        val shop = createTestShop(shopId)
        val menus = createTestMenus(menuIds)
        val conflictingReservation = createTestReservation()
        val request = CreateReservationRequest(
            shopId = shopId,
            menuIds = menuIds,
            reservationDate = LocalDate.now().plusDays(1),
            startTime = LocalTime.of(10, 0),
            paymentMethod = PaymentMethod.ON_SITE
        )
        
        whenever(shopRepository.findById(shopId)).thenReturn(Optional.of(shop))
        whenever(menuRepository.findAllById(menuIds)).thenReturn(menus)
        whenever(reservationRepository.findConflictingReservations(any(), any(), any(), any()))
            .thenReturn(listOf(conflictingReservation))
        
        // When & Then
        assertThrows<TimeConflictException> {
            reservationService.createReservation(request, customerId)
        }
    }
    
    private fun createTestShop(id: Long) = Shop(
        id = id,
        ownerId = 1L,
        name = "Test Shop",
        address = "Test Address",
        phone = "010-1234-5678",
        operatingHours = "{}",
        holidays = "[]"
    )
    
    private fun createTestMenus(ids: List<Long>) = ids.map { id ->
        Menu(
            id = id,
            shopId = 1L,
            name = "Test Menu $id",
            price = 10000,
            duration = 60,
            serviceType = ServiceType.WASH,
            carType = CarType.SEDAN_MEDIUM
        )
    }
}
```

### 2. Controller 테스트

```kotlin
@WebMvcTest(ReservationController::class)
@Import(SecurityConfig::class)
class ReservationControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var reservationService: ReservationService
    
    @MockBean
    private lateinit var jwtTokenProvider: JwtTokenProvider
    
    @Test
    @WithMockUser(roles = ["CUSTOMER"])
    fun `예약 생성 API 테스트`() {
        // Given
        val request = CreateReservationRequest(
            shopId = 1L,
            menuIds = listOf(1L, 2L),
            reservationDate = LocalDate.now().plusDays(1),
            startTime = LocalTime.of(10, 0),
            paymentMethod = PaymentMethod.ON_SITE
        )
        
        val response = ReservationResponse(
            id = 1L,
            shopId = 1L,
            shopName = "Test Shop",
            menus = emptyList(),
            reservationDate = request.reservationDate,
            startTime = request.startTime,
            endTime = request.startTime.plusHours(1),
            status = ReservationStatus.PENDING,
            specialRequests = null,
            totalPrice = 20000,
            paymentMethod = PaymentMethod.ON_SITE,
            rejectionReason = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        whenever(reservationService.createReservation(any(), any())).thenReturn(response)
        
        // When & Then
        mockMvc.perform(
            post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.shopId").value(1L))
        .andExpect(jsonPath("$.data.status").value("PENDING"))
    }
}
```

## 개발 시 주의사항

### 1. 성능 최적화
- N+1 쿼리 문제 방지를 위한 `@EntityGraph` 사용
- 페이징 처리 시 `countQuery` 최적화
- 인덱스 설정으로 검색 성능 향상

### 2. 보안
- SQL Injection 방지를 위한 파라미터 바인딩
- XSS 방지를 위한 입력값 검증
- CSRF 토큰 적용

### 3. 데이터 정합성
- 트랜잭션 범위 적절히 설정
- 동시성 제어를 위한 낙관적/비관적 락 사용
- 예약 시간 충돌 방지 로직

### 4. 모니터링
- 로그 레벨 적절히 설정
- 중요 비즈니스 로직에 메트릭 추가
- 에러 알림 시스템 구축

이 개발 가이드를 참고하여 단계적으로 백엔드를 구현해나가시면 됩니다!