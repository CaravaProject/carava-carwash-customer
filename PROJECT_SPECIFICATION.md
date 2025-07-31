# Carava - 디테일링 세차장/PPF샵 예약 서비스 기획서

## 프로젝트 개요

### 서비스 소개
Carava는 고객이 디테일링 세차장 및 PPF(Paint Protection Film) 샵을 손쉽게 찾고 예약할 수 있는 플랫폼 서비스입니다. 고객과 업체를 연결하여 효율적인 예약 관리와 매출 증대를 지원합니다.

### 기술 스택
- **Backend**: Kotlin + Spring Boot 3.5.0
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **Architecture**: RESTful API
- **Build**: Gradle with Kotlin DSL

## 사용자 유형 및 주요 기능

### 1. 일반 고객 (Customer)

#### 1.1 인증 및 회원관리
##### 회원가입
- **소셜 회원가입**: Google, Kakao, Naver 지원
- **이메일 회원가입**: 
  - 필수 입력: 이메일, 비밀번호, 비밀번호 확인, 휴대폰 번호, 닉네임, 주소
  - 약관 동의 (필수/선택 구분)
- **차량 정보** (선택사항, 나중 입력 가능):
  - 차량 번호, 차종, 연식

##### 로그인
- **소셜 로그인**: OAuth 2.0 기반
- **이메일 로그인**: JWT 토큰 기반 인증

##### 비밀번호 관리
- **비밀번호 찾기**: 이메일 인증을 통한 재설정

#### 1.2 메인 서비스
##### 홈 화면
- **알림**: 예약 상태 변경, 프로모션 정보
- **추천 업체**: 지역 기반, 평점 기반 추천
- **추천 작업**: 계절별, 차량 상태별 추천

##### 업체 검색 및 조회
- **검색 조건**:
  - 업체명 검색
  - 지역별 검색 (시/군/구 단위)
  - 날짜/시간별 예약 가능 업체 검색
  - 작업 유형별 검색 (세차, 코팅, PPF 등)
- **업체 상세 정보**:
  - 업체 사진 갤러리
  - 메뉴 및 가격 정보
  - 업체명, 주소, 연락처
  - 영업시간 및 휴무일
  - 좋아요 상태
  - 평점 및 리뷰
  - 홍보 링크 (유튜브, 블로그, 인스타그램)

##### 예약 시스템
- **예약 프로세스**:
  1. 메뉴 선택 → 소요시간 자동 계산
  2. 예약 가능 시간 조회 (30분 단위 타임블록)
  3. 예약 정보 입력: 메뉴, 시간, 요청사항, 첨부사진
  4. 예약 완료 → 결제 페이지 이동 (현재는 현장결제만)

##### 예약 관리
- **예약 내역**: 과거/현재/미래 예약 조회
- **예약 상태**: 신청중, 승인됨, 완료, 취소됨
- **예약 변경/취소**: 일정 변경 및 취소 기능

#### 1.3 커뮤니케이션
##### 메시지 시스템
- **업체와의 1:1 채팅**
- **파일 업로드**: 사진 첨부 기능
- **사용자 신고**: 부적절한 행위 신고

#### 1.4 마이페이지
- **프로필 수정**: 개인정보, 차량정보 수정
- **예약 관리**: 예약 내역 조회 및 관리
- **나의 저장**: 관심 업체, 즐겨찾기
- **내 리뷰**: 작성한 리뷰 관리

### 2. 업체 사장 (Business Owner)

#### 2.1 인증 및 회원관리
##### 회원가입
- **기본 정보**: 이메일, 비밀번호, 휴대폰 번호, 닉네임
- **사업자 정보**: 사업자 번호, 업종 (세차장, PPF샵 등)
- **약관 동의**: 사업자 대상 약관

##### 로그인
- 일반 고객과 동일한 인증 시스템

#### 2.2 업체 관리
##### 업체 정보 설정
- **기본 정보**: 업체명, 주소, 연락처
- **영업 정보**: 영업시간, 휴무일 설정
- **홍보 링크**: 유튜브, 블로그, 인스타그램

##### 메뉴 관리
- **메뉴 등록**:
  - 상품명, 가격, 소요시간
  - 작업 구분: 세차, 하부세차, 코팅, 왁스, PPF 등
  - 차량 구분: SUV(소형/중형/대형), 세단(소형/중형/대형), 스포츠카
  - 메뉴 사진
- **메뉴 수정/삭제**

#### 2.3 운영 관리
##### 홈 화면 (대시보드)
- **오늘의 예약**: 당일 예약 일정 목록
- **매출 현황**: 
  - 이번 달 매출액
  - 완료된 작업 수
  - 받은 리뷰 수
- **최근 후기**: 최신 리뷰 목록

##### 예약 관리
- **예약 요청 확인**: 대기 중인 예약 목록
- **예약 승인/거절**:
  - 거절 시 거절 사유 입력 필수
  - 시간 중복 시 경고 알림
- **예약 일정 관리**: 캘린더 뷰로 전체 일정 확인

##### 매출 관리
- **매출 분석**:
  - 월별 매출 통계
  - 인기 메뉴 분석
  - 고객 분석
- **목표 설정**: 월간 매출 목표 설정 및 달성률

##### 리뷰 관리
- **리뷰 조회**: 업체에 대한 모든 리뷰
- **평점 관리**: 평균 평점 및 개선사항 분석

## 데이터베이스 설계

### 주요 엔티티

#### User (사용자)
```kotlin
- id: Long (PK)
- email: String (Unique)
- password: String (암호화)
- nickname: String
- phone: String
- address: String
- userType: UserType (CUSTOMER, BUSINESS_OWNER)
- socialProvider: SocialProvider? (GOOGLE, KAKAO, NAVER)
- socialId: String?
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### Customer (고객)
```kotlin
- id: Long (PK)
- userId: Long (FK)
- vehicles: List<Vehicle>
```

#### Vehicle (차량)
```kotlin
- id: Long (PK)
- customerId: Long (FK)
- licensePlate: String
- carModel: String
- year: Int
```

#### BusinessOwner (업체 사장)
```kotlin
- id: Long (PK)
- userId: Long (FK)
- businessNumber: String
- businessType: BusinessType
- shop: Shop?
```

#### Shop (업체)
```kotlin
- id: Long (PK)
- ownerId: Long (FK)
- name: String
- address: String
- phone: String
- description: String?
- operatingHours: String
- holidays: String
- socialLinks: List<SocialLink>
- images: List<ShopImage>
- rating: Double
- reviewCount: Int
- createdAt: LocalDateTime
```

#### Menu (메뉴)
```kotlin
- id: Long (PK)
- shopId: Long (FK)
- name: String
- price: Int
- duration: Int (분 단위)
- serviceType: ServiceType (WASH, COATING, WAX, PPF 등)
- carType: CarType (SUV_SMALL, SUV_MEDIUM, SUV_LARGE, SEDAN_SMALL 등)
- description: String?
- images: List<MenuImage>
- isActive: Boolean
```

#### Reservation (예약)
```kotlin
- id: Long (PK)
- customerId: Long (FK)
- shopId: Long (FK)
- menus: List<ReservationMenu>
- reservationDate: LocalDate
- startTime: LocalTime
- endTime: LocalTime
- status: ReservationStatus (PENDING, APPROVED, COMPLETED, CANCELLED)
- specialRequests: String?
- attachments: List<ReservationAttachment>
- totalPrice: Int
- paymentMethod: PaymentMethod
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

#### Review (리뷰)
```kotlin
- id: Long (PK)
- reservationId: Long (FK)
- customerId: Long (FK)
- shopId: Long (FK)
- rating: Int (1-5)
- content: String
- images: List<ReviewImage>
- createdAt: LocalDateTime
```

#### Message (메시지)
```kotlin
- id: Long (PK)
- senderId: Long (FK)
- receiverId: Long (FK)
- chatRoomId: Long (FK)
- content: String
- messageType: MessageType (TEXT, IMAGE)
- attachments: List<MessageAttachment>
- isRead: Boolean
- createdAt: LocalDateTime
```

## API 설계

### 인증 API
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인
- `POST /api/auth/logout` - 로그아웃
- `POST /api/auth/refresh` - 토큰 갱신
- `POST /api/auth/forgot-password` - 비밀번호 찾기
- `POST /api/auth/social/login` - 소셜 로그인

### 고객 API
- `GET /api/customer/home` - 홈 화면 데이터
- `GET /api/customer/shops` - 업체 검색
- `GET /api/customer/shops/{id}` - 업체 상세 조회
- `POST /api/customer/reservations` - 예약 생성
- `GET /api/customer/reservations` - 예약 목록 조회
- `GET /api/customer/profile` - 프로필 조회
- `PUT /api/customer/profile` - 프로필 수정

### 업체 API
- `GET /api/business/dashboard` - 대시보드 데이터
- `GET /api/business/reservations` - 예약 목록 조회
- `PUT /api/business/reservations/{id}/approve` - 예약 승인
- `PUT /api/business/reservations/{id}/reject` - 예약 거절
- `GET /api/business/shop` - 업체 정보 조회
- `PUT /api/business/shop` - 업체 정보 수정
- `GET /api/business/menus` - 메뉴 목록 조회
- `POST /api/business/menus` - 메뉴 생성
- `PUT /api/business/menus/{id}` - 메뉴 수정
- `DELETE /api/business/menus/{id}` - 메뉴 삭제

### 공통 API
- `GET /api/messages` - 메시지 조회
- `POST /api/messages` - 메시지 전송
- `POST /api/upload` - 파일 업로드
- `GET /api/reviews` - 리뷰 조회
- `POST /api/reviews` - 리뷰 작성

## 보안 및 인증

### JWT 토큰 관리
- **Access Token**: 15분 유효기간
- **Refresh Token**: 7일 유효기간
- **토큰 저장**: HttpOnly 쿠키 또는 로컬 스토리지

### 권한 관리
- **CUSTOMER**: 고객 전용 기능 접근
- **BUSINESS_OWNER**: 업체 관리 기능 접근
- **ADMIN**: 시스템 관리 기능 접근 (추후 구현)

### 데이터 보안
- **비밀번호**: BCrypt 암호화
- **개인정보**: AES 암호화 (필요시)
- **API Rate Limiting**: 계정별 요청 제한

## 파일 관리

### 이미지 업로드
- **지원 형식**: JPG, PNG, WEBP
- **크기 제한**: 최대 10MB
- **저장 위치**: AWS S3 또는 로컬 스토리지
- **이미지 최적화**: 자동 리사이징 및 압축

### 첨부파일 관리
- **예약 첨부파일**: 차량 사진, 손상 부위 사진
- **메시지 첨부파일**: 참고 이미지
- **리뷰 첨부파일**: 완료 사진

## 알림 시스템

### 알림 유형
- **예약 관련**: 예약 승인/거절, 예약 완료
- **메시지 알림**: 새 메시지 도착
- **프로모션**: 할인 이벤트, 신규 업체

### 알림 방법
- **푸시 알림**: 모바일 앱
- **이메일**: 중요 알림
- **SMS**: 예약 확정 시 (선택)

## 추가 기능 제안

### 1. 리뷰 시스템 강화
- **사진 리뷰**: Before/After 사진 업로드
- **태그 시스템**: 서비스 품질 태그 (#친절함, #꼼꼼함 등)
- **리뷰 신뢰도**: 인증된 예약자만 리뷰 작성 가능

### 2. 프로모션 시스템
- **쿠폰**: 할인 쿠폰 발행 및 사용
- **포인트**: 예약 완료 시 포인트 적립
- **멤버십**: 단골 고객 우대 혜택

### 3. 예약 최적화
- **자동 예약**: 정기 예약 설정
- **대기열**: 예약 마감 시 대기 순번 등록
- **예약 변경**: 업체 승인 하에 일정 변경

### 4. 분석 도구
- **고객 분석**: 재방문률, 선호 서비스 분석
- **매출 분석**: 시간대별, 요일별 매출 분석
- **트렌드 분석**: 지역별 인기 서비스

### 5. 결제 시스템 (추후 구현)
- **결제 수단**: 카드, 계좌이체, 간편결제
- **분할 결제**: 예약금/잔금 분할
- **자동 결제**: 정기 예약 자동 결제

## 개발 우선순위

### Phase 1 (MVP)
1. 사용자 인증 시스템 (회원가입, 로그인)
2. 기본 CRUD API (업체, 메뉴, 예약)
3. 예약 시스템 핵심 기능
4. 기본 검색 기능

### Phase 2
1. 메시지 시스템
2. 리뷰 시스템
3. 파일 업로드 기능
4. 알림 시스템

### Phase 3
1. 소셜 로그인
2. 고급 검색 및 필터링
3. 대시보드 및 분석 기능
4. 프로모션 시스템

### Phase 4
1. 결제 연동
2. 모바일 앱 최적화
3. 성능 최적화
4. 관리자 기능

## 기술적 고려사항

### 성능 최적화
- **데이터베이스 인덱싱**: 검색 쿼리 최적화
- **캐싱**: Redis를 활용한 자주 조회되는 데이터 캐싱
- **페이징**: 대용량 데이터 조회 시 페이징 처리

### 확장성
- **마이크로서비스**: 기능별 서비스 분리 (추후)
- **로드 밸런싱**: 트래픽 분산 처리
- **데이터베이스 샤딩**: 대용량 데이터 분산

### 모니터링
- **로깅**: 애플리케이션 로그 관리
- **메트릭**: 성능 지표 모니터링
- **알림**: 시스템 오류 알림

이 기획서를 바탕으로 단계적으로 백엔드 개발을 진행하시면 됩니다. 추가로 궁금한 점이나 더 자세히 정의해야 할 부분이 있다면 언제든 말씀해 주세요!