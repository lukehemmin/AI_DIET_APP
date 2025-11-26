# AI 식단 관리 앱 - 백엔드 서버 구현 계획 (Spring Boot)

## 📋 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택](#기술-스택)
3. [서버 아키텍처](#서버-아키텍처)
4. [API 설계](#api-설계)
5. [데이터베이스 스키마](#데이터베이스-스키마)
6. [구현 단계](#구현-단계)
7. [보안 고려사항](#보안-고려사항)

---

## 프로젝트 개요

### 현재 상태
- **Android 클라이언트**: Java 기반, Fragment 구조
- **목업 웹앱**: React + TypeScript, 클라이언트 사이드 전용
- **데이터 저장**: 로컬 스토리지만 사용
- **AI 기능**: 클라이언트에서 직접 Gemini API 호출

### 서버 구축 목적
1. 사용자 데이터 영구 저장 및 동기화
2. API 키 보안 (Gemini API를 서버에서 호출)
3. 소셜 기능 구현 (그룹, 친구, 피드)
4. 멀티 디바이스 지원
5. 분석 및 통계 데이터 중앙 관리

---

## 기술 스택

### 선택: Spring Boot (Java)

```
- Runtime: JDK 17+
- Framework: Spring Boot 3.2+
- Build Tool: Gradle 8.5+
- Database: Supabase PostgreSQL (클라우드 호스팅)
- Cache: Redis 7 (세션, 캐싱) - 선택적
- ORM: Spring Data JPA + Hibernate
- Authentication: Spring Security + JWT
- File Storage: Supabase Storage (클라우드) 또는 로컬 파일 시스템
- Image Processing: Thumbnailator
- AI Integration: WebClient (Gemini REST API)
- Validation: Spring Validation
- Logging: Logback (SLF4J)
- API Documentation: SpringDoc OpenAPI (Swagger)
- Push Notifications: Firebase Admin SDK
```

### 주요 의존성
```gradle
dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis' // 선택적
    
    // Database (Supabase PostgreSQL)
    runtimeOnly 'org.postgresql:postgresql'
    
    // Supabase Storage (선택적 - 이미지 저장용)
    implementation 'io.supabase:supabase-kt:1.3.2' // Kotlin 버전
    // 또는 REST API 직접 호출 (WebClient 사용)
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    // Image Processing
    implementation 'net.coobird:thumbnailator:0.4.20'
    
    // HTTP Client (Gemini API, Supabase Storage)
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // Firebase (FCM)
    implementation 'com.google.firebase:firebase-admin:9.2.0'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // API Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

### 선택 이유
1. **언어 통일**: Android (Java) + 백엔드 (Java) → 동일 언어
2. **익숙함**: Java 개발 경험 활용
3. **엔터프라이즈급**: 안정성, 보안, 확장성
4. **강력한 생태계**: Spring Security, JPA, Redis 등 완성도 높은 라이브러리
5. **Gemini API 연동**: WebClient로 REST API 호출 간단
6. **Supabase 장점**:
   - 무료 PostgreSQL 호스팅 (500MB, 무제한 API 요청)
   - 자동 백업 및 확장
   - Supabase Storage로 이미지 저장 가능 (1GB 무료)
   - Real-time 기능 (선택적 사용)
   - 관리 대시보드 제공

---

## 서버 아키텍처

### 전체 구조
```
┌─────────────────┐
│  Android Client │
└────────┬────────┘
         │ HTTPS
         ▼
┌──────────────────────────────────────────┐
│       Spring Boot Application            │
│  ┌──────────────────────────┐            │
│  │  Spring Security         │            │
│  │  - JWT Filter            │            │
│  │  - Authentication        │            │
│  ├──────────────────────────┤            │
│  │  Controllers (REST API)  │            │
│  │  - AuthController        │            │
│  │  - MealController        │            │
│  │  - AIController          │            │
│  │  - GroupController       │            │
│  ├──────────────────────────┤            │
│  │  Services (Business)     │            │
│  │  - UserService           │            │
│  │  - MealService           │            │
│  │  - GeminiService         │            │
│  │  - StorageService        │            │
│  ├──────────────────────────┤            │
│  │  Repositories (JPA)      │            │
│  │  - UserRepository        │            │
│  │  - MealRepository        │            │
│  │  - GroupRepository       │            │
│  ├──────────────────────────┤            │
│  │  Static Resources        │            │
│  │  /uploads/meals/...      │            │
│  └──────────────────────────┘            │
└────────┬──────────────┬──────────────────┘
         │              │
         ▼              ▼
┌────────────────┐  ┌──────────────┐
│   PostgreSQL   │  │    Redis     │
│  (Main DB)     │  │  (Cache)     │
└────────────────┘  └──────────────┘
         │
         ▼
┌────────────────────────────────┐
│   External Services            │
│  - Gemini API (AI)             │
│  - FCM (Push Notifications)    │
└────────────────────────────────┘

┌────────────────────────────────┐
│   Local File System            │
│  /uploads/                     │
│    └─ meals/                   │
│        ├─ user-uuid-1/         │
│        │   ├─ image1.jpg       │
│        │   └─ thumb/           │
│        │       └─ image1.jpg   │
│        └─ user-uuid-2/         │
│            └─ image1.jpg       │
└────────────────────────────────┘
```

### 디렉토리 구조 (Spring Boot 표준)
```
server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/healthmate/server/
│   │   │       ├── HealthMateApplication.java
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── RedisConfig.java
│   │   │       │   ├── WebConfig.java
│   │   │       │   └── FirebaseConfig.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── UserController.java
│   │   │       │   ├── MealController.java
│   │   │       │   ├── AIController.java
│   │   │       │   ├── GroupController.java
│   │   │       │   └── ChallengeController.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── MealService.java
│   │   │       │   ├── GeminiService.java
│   │   │       │   ├── StorageService.java
│   │   │       │   ├── AnalyticsService.java
│   │   │       │   └── NotificationService.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── MealRepository.java
│   │   │       │   ├── GroupRepository.java
│   │   │       │   ├── FriendshipRepository.java
│   │   │       │   └── WaterIntakeRepository.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   ├── User.java
│   │   │       │   ├── Meal.java
│   │   │       │   ├── Group.java
│   │   │       │   ├── GroupMember.java
│   │   │       │   ├── Friendship.java
│   │   │       │   ├── WaterIntake.java
│   │   │       │   ├── GroupFeed.java
│   │   │       │   └── ChatSession.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── request/
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── SignupRequest.java
│   │   │       │   │   ├── MealCreateRequest.java
│   │   │       │   │   └── ...
│   │   │       │   └── response/
│   │   │       │       ├── AuthResponse.java
│   │   │       │       ├── MealResponse.java
│   │   │       │       ├── AnalysisResult.java
│   │   │       │       └── ...
│   │   │       │
│   │   │       ├── security/
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   └── UserDetailsServiceImpl.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   └── UnauthorizedException.java
│   │   │       │
│   │   │       └── util/
│   │   │           ├── ImageUtil.java
│   │   │           ├── DateUtil.java
│   │   │           └── CalorieCalculator.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── static/
│   │           └── (empty - 정적 파일은 uploads/ 사용)
│   │
│   └── test/
│       └── java/
│           └── com/healthmate/server/
│               ├── controller/
│               ├── service/
│               └── repository/
│
├── uploads/                      # ⭐ 로컬 이미지 저장소
│   └── meals/
│       └── .gitkeep
│
├── build.gradle
├── settings.gradle
├── .gitignore
└── README.md
```

---

## API 설계

### Base URL
```
Production: https://api.healthmate.app
Development: http://localhost:8080
```

### 인증 방식
- **JWT (JSON Web Token)** 사용
- Header: `Authorization: Bearer <token>`
- Access Token (1일) + Refresh Token (30일)

---

### 1. 인증 API

#### 1.1 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "securePassword123!",
  "name": "홍길동",
  "profile": {
    "gender": "MALE",
    "age": 30,
    "height": 178,
    "weight": 75,
    "activityLevel": "MODERATE"
  }
}

Response: 200 OK
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "name": "홍길동"
    },
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

#### 1.2 로그인
```http
POST /api/auth/login
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "securePassword123!"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "user": { ... },
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci..."
  }
}
```

#### 1.3 토큰 갱신
```http
POST /api/auth/refresh
Content-Type: application/json

Request:
{
  "refreshToken": "eyJhbGci..."
}

Response: 200 OK
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci..."
  }
}
```

---

### 2. 식단 기록 API

#### 2.1 식단 이미지 분석
```http
POST /api/meals/analyze
Authorization: Bearer <token>
Content-Type: multipart/form-data

Request:
- image: File
- mealTime: "BREAKFAST"
- date: "2024-11-26"

Response: 200 OK
{
  "success": true,
  "data": {
    "analysisResults": [
      {
        "foodItem": "계란후라이",
        "servingSize": 50,
        "kcal": 90,
        "macro": {
          "carbs": 1,
          "protein": 7,
          "fat": 6
        }
      }
    ],
    "imageUrl": "http://localhost:8080/uploads/meals/user-uuid/1732600000000_abc123.jpg",
    "thumbnailUrl": "http://localhost:8080/uploads/meals/user-uuid/thumb/1732600000000_abc123.jpg"
  }
}
```

#### 2.2 식단 기록 확정
```http
POST /api/meals
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "meals": [
    {
      "foodItem": "계란후라이",
      "servingSize": 50,
      "kcal": 90,
      "carbs": 1,
      "protein": 7,
      "fat": 6,
      "mealTime": "BREAKFAST",
      "date": "2024-11-26",
      "imageUrl": "http://..."
    }
  ]
}

Response: 201 Created
{
  "success": true,
  "data": {
    "meals": [ ... ]
  }
}
```

#### 2.3 식단 목록 조회
```http
GET /api/meals?startDate=2024-11-20&endDate=2024-11-26
Authorization: Bearer <token>

Response: 200 OK
{
  "success": true,
  "data": {
    "meals": [ ... ],
    "totalCount": 25,
    "summary": {
      "totalKcal": 15000,
      "avgKcal": 2142,
      "totalProtein": 500
    }
  }
}
```

#### 2.4 수동 식단 추가
```http
POST /api/meals/manual
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "foodItem": "김치찌개",
  "mealTime": "LUNCH",
  "date": "2024-11-26"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "analysis": {
      "foodItem": "김치찌개",
      "servingSize": 250,
      "kcal": 150,
      "macro": { "carbs": 10, "protein": 15, "fat": 5 }
    }
  }
}
```

---

### 3. AI 서비스 API

#### 3.1 AI 챗봇
```http
POST /api/ai/chat
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "message": "오늘 저녁으로 뭐 먹으면 좋을까요?",
  "sessionId": "uuid"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "reply": "오늘 점심에 탄수화물이 많았으니...",
    "sessionId": "uuid"
  }
}
```

#### 3.2 AI 식단 플래너
```http
POST /api/ai/meal-plan
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "preferences": "고단백, 저탄수화물"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "mealPlan": {
      "breakfast": { "name": "...", "kcal": 350, "description": "..." },
      "lunch": { ... },
      "dinner": { ... },
      "totalKcal": 2200
    }
  }
}
```

#### 3.3 냉장고 파먹기 레시피
```http
POST /api/ai/fridge-recipe
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "ingredients": "계란, 양파, 감자, 치즈"
}

Response: 200 OK
{
  "success": true,
  "data": {
    "recipe": {
      "name": "스페니시 오믈렛",
      "kcal": 380,
      "ingredients": ["계란 3개", "양파 1/2개", ...],
      "instructions": ["1. 감자와 양파를 썬다", ...]
    }
  }
}
```

---

### 4. 그룹 & 소셜 API

#### 4.1 그룹 생성
```http
POST /api/groups
Authorization: Bearer <token>
Content-Type: application/json

Request:
{
  "name": "여름 준비 다이어트방",
  "description": "여름까지 함께 달려봐요!",
  "challenge": "그룹 칼로리 50,000kcal 소모"
}

Response: 201 Created
{
  "success": true,
  "data": {
    "group": {
      "id": "uuid",
      "name": "여름 준비 다이어트방",
      ...
    }
  }
}
```

#### 4.2 그룹 목록 조회
```http
GET /api/groups
Authorization: Bearer <token>

Response: 200 OK
{
  "success": true,
  "data": {
    "groups": [ ... ]
  }
}
```

---

## 데이터베이스 스키마

### JPA Entity 설계

#### User Entity
```java
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password; // BCrypt 암호화
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender; // MALE, FEMALE
    
    @Column(nullable = false)
    private Integer age;
    
    @Column(nullable = false)
    private Double height;
    
    @Column(nullable = false)
    private Double weight;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLevel activityLevel; // SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
    
    @ElementCollection
    @CollectionTable(name = "user_badges", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "badge_id")
    private Set<String> unlockedBadges = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meal> meals = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaterIntake> waterIntakes = new ArrayList<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Meal Entity
```java
@Entity
@Table(name = "meals", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, date")
})
@Getter @Setter
@NoArgsConstructor
public class Meal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String foodItem;
    
    @Column(nullable = false)
    private Double servingSize;
    
    @Column(nullable = false)
    private Double kcal;
    
    @Column(nullable = false)
    private Double carbs;
    
    @Column(nullable = false)
    private Double protein;
    
    @Column(nullable = false)
    private Double fat;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealTime mealTime; // BREAKFAST, LUNCH, DINNER, SNACK, LATE_NIGHT
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Column(length = 500)
    private String thumbnailUrl;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

#### WaterIntake Entity
```java
@Entity
@Table(name = "water_intakes", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
@Getter @Setter
@NoArgsConstructor
public class WaterIntake {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private Integer glasses = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

#### Group Entity
```java
@Entity
@Table(name = "groups")
@Getter @Setter
@NoArgsConstructor
public class Group {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String challenge;
    
    @Column(nullable = false)
    private UUID ownerId;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> members = new ArrayList<>();
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupFeed> feeds = new ArrayList<>();
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

#### GroupMember Entity
```java
@Entity
@Table(name = "group_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
@Getter @Setter
@NoArgsConstructor
public class GroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
}
```

#### Friendship Entity
```java
@Entity
@Table(name = "friendships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
@Getter @Setter
@NoArgsConstructor
public class Friendship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### GroupFeed Entity
```java
@Entity
@Table(name = "group_feeds", indexes = {
    @Index(name = "idx_group_created", columnList = "group_id, created_at")
})
@Getter @Setter
@NoArgsConstructor
public class GroupFeed {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String type; // "meal", "challenge"
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(nullable = false)
    private Integer likes = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### ChatSession Entity
```java
@Entity
@Table(name = "chat_sessions")
@Getter @Setter
@NoArgsConstructor
public class ChatSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(columnDefinition = "jsonb")
    private String messages; // JSON array of messages
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### Enum 정의

```java
public enum Gender {
    MALE, FEMALE
}

public enum ActivityLevel {
    SEDENTARY,      // 거의 운동 안함
    LIGHT,          // 가벼운 운동 (주 1-3일)
    MODERATE,       // 보통 운동 (주 3-5일)
    ACTIVE,         // 활발한 운동 (주 6-7일)
    VERY_ACTIVE     // 매우 활발 (매일, 강도 높음)
}

public enum MealTime {
    BREAKFAST,      // 아침
    LUNCH,          // 점심
    DINNER,         // 저녁
    SNACK,          // 간식
    LATE_NIGHT      // 야식
}
```

---

## 이미지 저장 구현

### Option 1: 로컬 파일 시스템 (간단)

#### StorageService (Local)
```java
@Service
@Slf4j
public class LocalStorageService implements StorageService {
    
    @Value("${app.upload.dir}")
    private String uploadDir; // uploads/meals
    
    @Value("${app.base-url}")
    private String baseUrl; // http://localhost:8080
    
    /**
     * 이미지 업로드 (원본 + 썸네일)
     */
    @Override
    public ImageUploadResult uploadMealImage(MultipartFile file, UUID userId) 
            throws IOException {
        
        // 사용자 디렉토리 생성
        Path userDir = Paths.get(uploadDir, userId.toString());
        Path thumbDir = userDir.resolve("thumb");
        Files.createDirectories(userDir);
        Files.createDirectories(thumbDir);
        
        // 파일명 생성
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        String fileName = timestamp + "_" + uuid + ".jpg";
        
        Path originalPath = userDir.resolve(fileName);
        Path thumbnailPath = thumbDir.resolve(fileName);
        
        // 원본 저장 (최대 1200px, 압축)
        Thumbnails.of(file.getInputStream())
            .size(1200, 1200)
            .outputQuality(0.85)
            .outputFormat("jpg")
            .toFile(originalPath.toFile());
        
        // 썸네일 저장 (300px)
        Thumbnails.of(file.getInputStream())
            .size(300, 300)
            .crop(Positions.CENTER)
            .outputQuality(0.80)
            .outputFormat("jpg")
            .toFile(thumbnailPath.toFile());
        
        // URL 생성
        String originalUrl = String.format("%s/uploads/meals/%s/%s", 
            baseUrl, userId, fileName);
        String thumbnailUrl = String.format("%s/uploads/meals/%s/thumb/%s", 
            baseUrl, userId, fileName);
        
        return ImageUploadResult.builder()
            .originalUrl(originalUrl)
            .thumbnailUrl(thumbnailUrl)
            .build();
    }
    
    /**
     * 이미지 삭제
     */
    @Override
    public void deleteImage(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            String pathStr = uri.getPath();
            
            if (pathStr.startsWith("/")) {
                pathStr = pathStr.substring(1);
            }
            
            Path filePath = Paths.get(pathStr);
            Files.deleteIfExists(filePath);
            
            // 썸네일도 삭제
            String thumbPath = pathStr.replace("/", "/thumb/");
            Path thumbFilePath = Paths.get(thumbPath);
            Files.deleteIfExists(thumbFilePath);
            
            log.info("Deleted image: {}", imageUrl);
        } catch (Exception e) {
            log.error("Failed to delete image: {}", imageUrl, e);
        }
    }
}
```

---

### Option 2: Supabase Storage (권장 - 클라우드)

#### SupabaseConfig
```java
@Configuration
public class SupabaseConfig {
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.key}")
    private String supabaseKey;
    
    @Bean
    public WebClient supabaseWebClient() {
        return WebClient.builder()
            .baseUrl(supabaseUrl)
            .defaultHeader("apikey", supabaseKey)
            .defaultHeader("Authorization", "Bearer " + supabaseKey)
            .build();
    }
}
```

#### StorageService (Supabase)
```java
@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.type", havingValue = "supabase")
public class SupabaseStorageService implements StorageService {
    
    private final WebClient supabaseClient;
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.bucket}")
    private String bucketName; // meal-images
    
    public SupabaseStorageService(WebClient supabaseWebClient) {
        this.supabaseClient = supabaseWebClient;
    }
    
    /**
     * 이미지 업로드 (원본 + 썸네일)
     */
    @Override
    public ImageUploadResult uploadMealImage(MultipartFile file, UUID userId) 
            throws IOException {
        
        // 파일명 생성
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        String fileName = timestamp + "_" + uuid + ".jpg";
        
        String originalPath = String.format("%s/%s", userId, fileName);
        String thumbnailPath = String.format("%s/thumb/%s", userId, fileName);
        
        // 원본 이미지 리사이징
        ByteArrayOutputStream originalStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
            .size(1200, 1200)
            .outputQuality(0.85)
            .outputFormat("jpg")
            .toOutputStream(originalStream);
        byte[] originalBytes = originalStream.toByteArray();
        
        // 썸네일 생성
        ByteArrayOutputStream thumbStream = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(originalBytes))
            .size(300, 300)
            .crop(Positions.CENTER)
            .outputQuality(0.80)
            .outputFormat("jpg")
            .toOutputStream(thumbStream);
        byte[] thumbBytes = thumbStream.toByteArray();
        
        // Supabase Storage에 업로드 (원본)
        uploadToSupabase(bucketName, originalPath, originalBytes, "image/jpeg");
        
        // Supabase Storage에 업로드 (썸네일)
        uploadToSupabase(bucketName, thumbnailPath, thumbBytes, "image/jpeg");
        
        // Public URL 생성
        String originalUrl = String.format("%s/storage/v1/object/public/%s/%s", 
            supabaseUrl, bucketName, originalPath);
        String thumbnailUrl = String.format("%s/storage/v1/object/public/%s/%s", 
            supabaseUrl, bucketName, thumbnailPath);
        
        return ImageUploadResult.builder()
            .originalUrl(originalUrl)
            .thumbnailUrl(thumbnailUrl)
            .build();
    }
    
    /**
     * Supabase Storage 업로드
     */
    private void uploadToSupabase(String bucket, String path, byte[] data, String contentType) {
        try {
            supabaseClient.post()
                .uri("/storage/v1/object/{bucket}/{path}", bucket, path)
                .contentType(MediaType.parseMediaType(contentType))
                .bodyValue(data)
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            log.info("Uploaded to Supabase: {}/{}", bucket, path);
        } catch (Exception e) {
            log.error("Failed to upload to Supabase: {}/{}", bucket, path, e);
            throw new RuntimeException("Supabase 업로드 실패", e);
        }
    }
    
    /**
     * 이미지 삭제
     */
    @Override
    public void deleteImage(String imageUrl) {
        try {
            // URL에서 경로 추출
            // https://xxx.supabase.co/storage/v1/object/public/meal-images/user-uuid/file.jpg
            String pathPart = imageUrl.substring(imageUrl.indexOf("/public/") + 8);
            String[] parts = pathPart.split("/", 2);
            String bucket = parts[0];
            String filePath = parts[1];
            
            // 원본 삭제
            deleteFromSupabase(bucket, filePath);
            
            // 썸네일 삭제
            String thumbPath = filePath.replace("/", "/thumb/");
            deleteFromSupabase(bucket, thumbPath);
            
            log.info("Deleted from Supabase: {}", imageUrl);
        } catch (Exception e) {
            log.error("Failed to delete from Supabase: {}", imageUrl, e);
        }
    }
    
    /**
     * Supabase Storage에서 삭제
     */
    private void deleteFromSupabase(String bucket, String path) {
        try {
            supabaseClient.delete()
                .uri("/storage/v1/object/{bucket}/{path}", bucket, path)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            log.warn("Failed to delete from Supabase: {}/{}", bucket, path, e);
        }
    }
}
```

#### StorageService Interface
```java
public interface StorageService {
    ImageUploadResult uploadMealImage(MultipartFile file, UUID userId) throws IOException;
    void deleteImage(String imageUrl);
}
```

#### ImageUploadResult DTO
```java
@Data
@Builder
public class ImageUploadResult {
    private String originalUrl;
    private String thumbnailUrl;
}
```

### WebConfig (정적 파일 서빙)
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 요청을 로컬 디렉토리로 매핑
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadDir + "/")
            .setCachePeriod(31536000); // 1년 캐싱
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:8080")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

---

## Gemini API 연동 구현

### GeminiService
```java
@Service
@Slf4j
public class GeminiService {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public GeminiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();
        this.objectMapper = objectMapper;
    }
    
    /**
     * 이미지 분석 (음식 인식)
     */
    public List<AnalysisResult> analyzeImage(MultipartFile file) throws IOException {
        
        // Base64 인코딩
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        
        // 요청 생성
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(
                        new TextPart("이 음식 사진을 분석해서 사진에 보이는 모든 음식 각각의 칼로리와 영양 정보를 알려줘. " +
                                   "음식 이름, 1인분 기준 양(g), 칼로리(kcal), 탄수화물, 단백질, 지방 함량(g)을 포함해서 " +
                                   "JSON 배열 형태로 응답해줘."),
                        new ImagePart(base64Image, file.getContentType())
                    ))
                    .build()
            ))
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .build())
            .build();
        
        // API 호출
        GeminiResponse response = webClient.post()
            .uri("/v1/models/gemini-2.0-flash:generateContent?key=" + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(GeminiResponse.class)
            .block();
        
        if (response == null || response.getCandidates().isEmpty()) {
            throw new RuntimeException("AI 분석에 실패했습니다.");
        }
        
        // JSON 파싱
        String jsonText = response.getCandidates().get(0)
            .getContent().getParts().get(0).getText();
        
        List<AnalysisResult> results = objectMapper.readValue(
            jsonText, 
            new TypeReference<List<AnalysisResult>>() {}
        );
        
        if (results.isEmpty()) {
            throw new RuntimeException("인식할 수 없는 음식입니다.");
        }
        
        return results;
    }
    
    /**
     * 텍스트 기반 분석
     */
    public AnalysisResult analyzeText(String foodItem) {
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(
                        new TextPart(String.format(
                            "음식 '%s'에 대한 일반적인 1인분 기준 영양 정보를 알려줘. " +
                            "JSON 형태로 응답해줘: {foodItem, servingSize, kcal, carbs, protein, fat}",
                            foodItem
                        ))
                    ))
                    .build()
            ))
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .build())
            .build();
        
        GeminiResponse response = webClient.post()
            .uri("/v1/models/gemini-2.0-flash:generateContent?key=" + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(GeminiResponse.class)
            .block();
        
        String jsonText = response.getCandidates().get(0)
            .getContent().getParts().get(0).getText();
        
        try {
            return objectMapper.readValue(jsonText, AnalysisResult.class);
        } catch (IOException e) {
            throw new RuntimeException("AI 분석 결과 파싱 실패", e);
        }
    }
    
    /**
     * AI 챗봇
     */
    public String chat(String message, List<ChatMessage> history) {
        
        // 대화 히스토리를 Gemini 형식으로 변환
        List<GeminiContent> contents = new ArrayList<>();
        for (ChatMessage msg : history) {
            contents.add(GeminiContent.builder()
                .role(msg.getRole())
                .parts(List.of(new TextPart(msg.getText())))
                .build());
        }
        
        // 현재 메시지 추가
        contents.add(GeminiContent.builder()
            .role("user")
            .parts(List.of(new TextPart(message)))
            .build());
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(contents)
            .systemInstruction("당신은 전문 영양사입니다. 사용자의 건강 데이터를 바탕으로 조언을 제공하세요.")
            .build();
        
        GeminiResponse response = webClient.post()
            .uri("/v1/models/gemini-2.0-flash:generateContent?key=" + apiKey)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(GeminiResponse.class)
            .block();
        
        return response.getCandidates().get(0)
            .getContent().getParts().get(0).getText();
    }
}

// DTO 클래스들
@Data
@Builder
class GeminiRequest {
    private List<GeminiContent> contents;
    private GenerationConfig generationConfig;
    private String systemInstruction;
}

@Data
@Builder
class GeminiContent {
    private String role; // "user" or "model"
    private List<Part> parts;
}

interface Part {
    String getType();
}

@Data
@AllArgsConstructor
class TextPart implements Part {
    private String text;
    
    @Override
    public String getType() { return "text"; }
}

@Data
class ImagePart implements Part {
    private InlineData inlineData;
    
    public ImagePart(String base64Data, String mimeType) {
        this.inlineData = new InlineData(base64Data, mimeType);
    }
    
    @Override
    public String getType() { return "image"; }
    
    @Data
    @AllArgsConstructor
    static class InlineData {
        private String data;
        private String mimeType;
    }
}

@Data
@Builder
class GenerationConfig {
    private String responseMimeType;
}

@Data
class GeminiResponse {
    private List<Candidate> candidates;
    
    @Data
    static class Candidate {
        private GeminiContent content;
    }
}
```

### AnalysisResult DTO
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String foodItem;
    private Double servingSize;
    private Double kcal;
    private Double carbs;
    private Double protein;
    private Double fat;
}
```

---

## MealController 구현

```java
@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
@Slf4j
public class MealController {
    
    private final MealService mealService;
    private final GeminiService geminiService;
    private final StorageService storageService;
    
    /**
     * 이미지 분석
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<MealAnalysisResponse>> analyzeMealImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("mealTime") MealTime mealTime,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            UUID userId = UUID.fromString(userDetails.getUsername());
            
            // 1. AI 분석
            List<AnalysisResult> analysisResults = geminiService.analyzeImage(image);
            
            // 2. 이미지 저장
            ImageUploadResult imageResult = storageService.uploadMealImage(image, userId);
            
            // 3. 응답 생성
            MealAnalysisResponse response = MealAnalysisResponse.builder()
                .analysisResults(analysisResults)
                .imageUrl(imageResult.getOriginalUrl())
                .thumbnailUrl(imageResult.getThumbnailUrl())
                .build();
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("Meal image analysis failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("AI 분석에 실패했습니다."));
        }
    }
    
    /**
     * 식단 기록 확정
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<MealResponse>>> createMeals(
            @Valid @RequestBody MealCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<MealResponse> meals = mealService.createMeals(userId, request.getMeals());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(meals));
    }
    
    /**
     * 식단 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MealListResponse>> getMeals(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        MealListResponse response = mealService.getMealsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 식단 삭제
     */
    @DeleteMapping("/{mealId}")
    public ResponseEntity<ApiResponse<Void>> deleteMeal(
            @PathVariable UUID mealId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        mealService.deleteMeal(userId, mealId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    /**
     * 수동 식단 추가
     */
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<AnalysisResult>> analyzeManualInput(
            @Valid @RequestBody ManualMealRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        AnalysisResult result = geminiService.analyzeText(request.getFoodItem());
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

// 공통 응답 포맷
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String error;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(false, null, error);
    }
}
```

---

## 구현 단계

### Phase 1: 기본 인프라 구축 (1주)
- [ ] Supabase 프로젝트 생성 및 설정
- [ ] Supabase PostgreSQL 연결 확인
- [ ] Supabase Storage 버킷 생성 (선택적)
- [ ] Spring Boot 프로젝트 생성 (Spring Initializr)
- [ ] Gradle 빌드 설정
- [ ] application.yml 환경 설정 (Supabase 연결)
- [ ] JPA Entity 설계 및 생성
- [ ] 로깅 설정 (Logback)
- [ ] 전역 예외 처리 (GlobalExceptionHandler)
- [ ] 공통 응답 포맷 (ApiResponse)

### Phase 2: 인증 시스템 (1주)
- [ ] Spring Security 설정
- [ ] JWT 토큰 생성/검증 (JwtTokenProvider)
- [ ] JWT 필터 (JwtAuthenticationFilter)
- [ ] UserDetailsService 구현
- [ ] 회원가입 API
- [ ] 로그인 API
- [ ] 토큰 갱신 API
- [ ] 비밀번호 암호화 (BCrypt)

### Phase 3: 사용자 & 프로필 관리 (3일)
- [ ] UserService 구현
- [ ] 사용자 프로필 조회 API
- [ ] 사용자 프로필 수정 API
- [ ] 목표 칼로리 계산 로직 (BMR/TDEE)
- [ ] Validation 설정

### Phase 4: 식단 기록 시스템 (1주)
- [ ] StorageService 인터페이스 설계
- [ ] LocalStorageService 구현 (로컬 저장) 또는
- [ ] SupabaseStorageService 구현 (Supabase Storage)
- [ ] MultipartFile 설정
- [ ] Thumbnailator 이미지 처리
- [ ] WebConfig 정적 파일 서빙 (로컬 저장시)
- [ ] GeminiService 구현 (AI 분석)
- [ ] MealService 구현
- [ ] MealController 구현
- [ ] 식단 기록 추가 API (사진 기반)
- [ ] 식단 기록 추가 API (수동 입력)
- [ ] 식단 목록 조회 API
- [ ] 식단 수정/삭제 API
- [ ] 수분 섭취 기록 API

### Phase 5: AI 서비스 통합 (1주)
- [ ] WebClient 설정
- [ ] Gemini API DTO 설계
- [ ] AI 챗봇 API (대화 저장)
- [ ] AI 식단 플래너 API
- [ ] 냉장고 파먹기 레시피 API
- [ ] AI 챌린지 생성 API
- [ ] AI 운동 계획 API
- [ ] AI 피드백 API

### Phase 6: 분석 & 통계 (1주)
- [ ] AnalyticsService 구현
- [ ] 일일 통계 조회 API
- [ ] 주간 리포트 API
- [ ] 월간 통계 API
- [ ] 식습관 패턴 분석 API
- [ ] 칼로리 트렌드 데이터
- [ ] 영양소 분석

### Phase 7: 소셜 기능 (1주)
- [ ] GroupService 구현
- [ ] 그룹 생성 API
- [ ] 그룹 가입/탈퇴 API
- [ ] 그룹 피드 조회 API
- [ ] 피드 게시 API
- [ ] 좋아요 기능
- [ ] 친구 추가/삭제 API
- [ ] 친구 목록 조회 API

### Phase 8: 챌린지 & 업적 시스템 (1주)
- [ ] ChallengeService 구현
- [ ] 챌린지 데이터 초기화
- [ ] 챌린지 진행 상황 계산 로직
- [ ] 챌린지 조회 API
- [ ] 업적(배지) 체크 로직
- [ ] 업적 조회 API
- [ ] 업적 알림

### Phase 9: 푸시 알림 (3일)
- [ ] Firebase Admin SDK 설정
- [ ] FCM 서비스 구현
- [ ] 디바이스 토큰 등록 API
- [ ] 알림 전송 서비스
- [ ] 식단 기록 리마인더
- [ ] 그룹 활동 알림
- [ ] 업적 달성 알림

### Phase 10: 최적화 & 배포 (1주)
- [ ] API 응답 캐싱 (Redis)
- [ ] 데이터베이스 인덱스 최적화
- [ ] API Rate Limiting
- [ ] 성능 테스트
- [ ] Docker 컨테이너화
- [ ] CI/CD 파이프라인 (GitHub Actions)
- [ ] AWS 또는 클라우드 배포
- [ ] 모니터링 설정 (Actuator, Prometheus)
- [ ] Swagger API 문서 생성

**총 예상 기간: 8-10주**

---

## 보안 고려사항

### 1. Spring Security 설정
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors()
            .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
```

### 2. JWT 토큰 보안
- Access Token: 짧은 만료 시간 (1일)
- Refresh Token: 긴 만료 시간 (30일), 암호화 저장
- 비밀키: 환경 변수로 관리 (최소 256bit)

### 3. API 키 보안
- Gemini API 키: application.yml에서 환경 변수로 관리
- 절대 클라이언트에 노출 금지
- 정기적인 키 로테이션

### 4. 파일 업로드 보안
```java
@Configuration
public class FileUploadConfig {
    
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(10)); // 최대 10MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        return factory.createMultipartConfig();
    }
}

// 파일 검증
public boolean isValidImageFile(MultipartFile file) {
    String contentType = file.getContentType();
    return contentType != null && 
           (contentType.equals("image/jpeg") || 
            contentType.equals("image/png") ||
            contentType.equals("image/jpg"));
}
```

### 5. SQL Injection 방지
- JPA 사용 (자동 방지)
- Native Query 사용시 파라미터 바인딩 필수

### 6. XSS 방지
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Bean
    public FilterRegistrationBean<XSSFilter> xssFilterRegistration() {
        FilterRegistrationBean<XSSFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XSSFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
```

---

## Supabase 설정

### 1. Supabase 프로젝트 생성

1. https://supabase.com 회원가입
2. 새 프로젝트 생성
   - Organization 선택 또는 생성
   - Project Name: `healthmate`
   - Database Password: 강력한 비밀번호 설정
   - Region: `Northeast Asia (Seoul)` 선택
3. 프로젝트 생성 대기 (약 2분)

### 2. 데이터베이스 연결 정보 확인

**Project Settings > Database**에서 확인:

```
Connection String (URI):
postgresql://postgres.[PROJECT_REF]:[PASSWORD]@aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres

Connection pooling (권장):
postgresql://postgres.[PROJECT_REF]:[PASSWORD]@aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres?pgbouncer=true
```

### 3. Supabase Storage 설정 (선택적)

이미지를 Supabase Storage에 저장하려면:

1. **Storage** 메뉴 클릭
2. **Create a new bucket** 클릭
   - Name: `meal-images`
   - Public bucket: **체크** (이미지 공개 접근 허용)
3. **Policies** 설정
   - INSERT: Authenticated users only
   - SELECT: Public (모든 사용자 읽기 가능)
   - DELETE: Authenticated users (본인 이미지만 삭제)

### 4. API Keys 확인

**Project Settings > API**에서 확인:
- **anon (public) key**: 클라이언트에서 사용 (읽기 전용)
- **service_role key**: 서버에서 사용 (모든 권한) ⚠️ 비밀 유지!

---

## 배포 환경

### 개발 환경 (로컬)
```yaml
# application-dev.yml
server:
  port: 8080

spring:
  datasource:
    # Supabase PostgreSQL (Connection Pooling)
    url: jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres?pgbouncer=true
    username: postgres.abcdefghijklmnop  # Supabase Project Reference
    password: your-supabase-password
    driver-class-name: org.postgresql.Driver
    
    hikari:
      maximum-pool-size: 5
      connection-timeout: 20000
  
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update  # 개발 환경에서만 사용
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

app:
  upload:
    dir: ./uploads/meals
  base-url: http://localhost:8080
  
# Supabase Storage (선택적)
supabase:
  url: https://abcdefghijklmnop.supabase.co
  key: your-supabase-service-role-key
  bucket: meal-images
```

### 프로덕션 환경
```yaml
# application-prod.yml
server:
  port: 8080

spring:
  datasource:
    # Supabase PostgreSQL (환경 변수 사용)
    url: ${SUPABASE_DB_URL}
    username: ${SUPABASE_DB_USERNAME}
    password: ${SUPABASE_DB_PASSWORD}
    
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate  # 프로덕션에서는 validate만 사용
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

app:
  upload:
    dir: /var/healthmate/uploads/meals  # 로컬 저장 사용시
  base-url: https://api.healthmate.app
  
# Supabase Storage (선택적)
supabase:
  url: ${SUPABASE_URL}
  key: ${SUPABASE_SERVICE_KEY}
  bucket: meal-images
```

### 환경 변수 (.env)
```env
# Supabase Database
SUPABASE_DB_URL=jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres?pgbouncer=true
SUPABASE_DB_USERNAME=postgres.abcdefghijklmnop
SUPABASE_DB_PASSWORD=your-supabase-password

# Supabase Storage (선택적)
SUPABASE_URL=https://abcdefghijklmnop.supabase.co
SUPABASE_SERVICE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Gemini API
GEMINI_API_KEY=your-gemini-api-key

# JWT
JWT_SECRET=your-super-secret-jwt-key-min-256-bit
JWT_REFRESH_SECRET=your-refresh-secret-key

# Application
BASE_URL=https://api.healthmate.app
```
  
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

app:
  upload:
    dir: /var/healthmate/uploads/meals
  base-url: https://api.healthmate.app
```

### 배포 방법

#### Option A: Supabase + 클라우드 서버 (권장)
```
- Server: AWS EC2, Google Cloud, DigitalOcean 등
  - 최소 스펙: 2vCPU, 2GB RAM, 20GB SSD
  - Supabase 사용시 대용량 디스크 불필요
- Database: Supabase PostgreSQL (클라우드 호스팅)
  - 무료 플랜: 500MB, 무제한 API 요청
  - Pro 플랜: $25/월 (8GB, 자동 백업)
- Storage: Supabase Storage (클라우드 호스팅)
  - 무료 플랜: 1GB
  - Pro 플랜: $25/월 (100GB)
- Cache: Redis (선택적) - Upstash Redis 무료 플랜
- 백업: Supabase 자동 백업 (매일)
- SSL: Let's Encrypt (무료)
```

**비용 예상 (월)**:
- Supabase 무료: $0 (소규모)
- Supabase Pro: $25 (1,000명 이상)
- 서버 (AWS EC2 t3.small): $15~30
- **총: $15~55/월**

**장점**:
- 데이터베이스 관리 불필요
- 자동 백업 및 확장
- 이미지 CDN 자동 제공
- 관리 콘솔에서 모니터링

#### Option B: 로컬 저장 + VPS
```
- Server: VPS (최소 50GB 디스크)
- Database: Supabase PostgreSQL
- Storage: 서버 로컬 디스크
- 백업: cron + rsync (수동 설정 필요)
```

**비용 예상 (월)**:
- Supabase 무료: $0
- VPS (50GB): $10~20
- **총: $10~20/월**

**단점**:
- 이미지 백업 수동 관리
- 서버 확장시 파일 동기화 필요

---

## 참고 자료

### 필요한 외부 서비스
1. **Supabase**: https://supabase.com
   - PostgreSQL 데이터베이스 호스팅
   - Storage (이미지 저장)
   - 무료 플랜 제공
2. **Google Gemini API**: https://ai.google.dev/
3. **Firebase Cloud Messaging**: https://firebase.google.com/docs/cloud-messaging

### 추천 학습 자료
- **Supabase 공식 문서**: https://supabase.com/docs
- **Supabase Storage API**: https://supabase.com/docs/guides/storage
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Thumbnailator**: https://github.com/coobird/thumbnailator
- **JWT**: https://jwt.io/

---

## 다음 단계

### 1. Supabase 프로젝트 생성
- https://supabase.com 회원가입
- 새 프로젝트 생성 (Region: Seoul)
- Database 연결 문자열 복사
- (선택) Storage bucket 생성: `meal-images`

### 2. Spring Boot 프로젝트 생성
- Spring Initializr 사용: https://start.spring.io
- Dependencies 선택:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - PostgreSQL Driver
  - Lombok
  - Validation

### 3. 로컬 개발 환경 설정
```bash
# application-dev.yml에 Supabase 연결 정보 입력
spring:
  datasource:
    url: jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres?pgbouncer=true
    username: postgres.YOUR_PROJECT_REF
    password: YOUR_PASSWORD
```

### 4. 기본 Entity 및 Repository 생성
- User, Meal, WaterIntake Entity 생성
- JPA Repository 인터페이스 생성

### 5. Phase 1 시작: 기본 인프라 구축
- [ ] Spring Security 설정
- [ ] JWT 인증 구현
- [ ] 전역 예외 처리
- [ ] API 문서 (Swagger)

---

**작성일**: 2024-11-26  
**버전**: 2.1 (Spring Boot + Supabase)  
**작성자**: AI Assistant
