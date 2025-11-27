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

#### Challenge Entity (추가)
```java
@Entity
@Table(name = "challenges")
@Getter @Setter
@NoArgsConstructor
public class Challenge {
    
    @Id
    private String id; // 예: "morning_meal", "protein_goal"
    
    @Column(nullable = false)
    private String title; // 예: "아침 식사 챙기기"
    
    @Column(nullable = false, length = 500)
    private String description; // 예: "3일 연속 아침 식사를 기록하세요"
    
    @Column(nullable = false)
    private String icon; // Lucide 아이콘 이름: "Sunrise", "Target" 등
    
    @Column(nullable = false)
    private Integer goal; // 목표 횟수 (예: 3, 7)
    
    @Column(nullable = false)
    private String goalUnit; // "days", "times", "kcal"
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### UserChallengeProgress Entity (추가)
```java
@Entity
@Table(name = "user_challenge_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_id"}),
    indexes = {
        @Index(name = "idx_user_challenge", columnList = "user_id, status")
    })
@Getter @Setter
@NoArgsConstructor
public class UserChallengeProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;
    
    @Column(nullable = false)
    private Integer current = 0; // 현재 진행 횟수
    
    @Column(nullable = false)
    private Double progress = 0.0; // 0.0 ~ 1.0
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status = ChallengeStatus.IN_PROGRESS;
    
    @Column
    private LocalDateTime completedAt; // 완료 시점
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 진행률 계산
    public void updateProgress() {
        if (challenge != null && challenge.getGoal() > 0) {
            this.progress = Math.min(1.0, (double) current / challenge.getGoal());
            if (this.progress >= 1.0 && this.status != ChallengeStatus.COMPLETED) {
                this.status = ChallengeStatus.COMPLETED;
                this.completedAt = LocalDateTime.now();
            }
        }
    }
}
```

#### GroupFeedLike Entity (추가 - 좋아요 관리)
```java
@Entity
@Table(name = "group_feed_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"feed_id", "user_id"}))
@Getter @Setter
@NoArgsConstructor
public class GroupFeedLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private GroupFeed feed;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### Badge Entity (추가)
```java
@Entity
@Table(name = "badges")
@Getter @Setter
@NoArgsConstructor
public class Badge {
    
    @Id
    private String id; // 예: "first_meal", "week_streak"
    
    @Column(nullable = false)
    private String name; // 예: "첫 식단 기록"
    
    @Column(nullable = false, length = 500)
    private String description; // 예: "첫 번째 식단을 기록했습니다!"
    
    @Column(nullable = false)
    private String icon; // Lucide 아이콘 이름
    
    @Column(nullable = false)
    private String condition; // 획득 조건 설명
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

#### AIGeneratedChallenge Entity (추가 - AI가 생성한 일일 챌린지)
```java
@Entity
@Table(name = "ai_generated_challenges",
    indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, generated_date")
    })
@Getter @Setter
@NoArgsConstructor
public class AIGeneratedChallenge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String icon; // "Zap", "Target", "Award"
    
    @Column(nullable = false)
    private LocalDate generatedDate;
    
    @Column(nullable = false)
    private Boolean isCompleted = false;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

### Enum 정의

```java
public enum Gender {
    MALE("남성"), 
    FEMALE("여성");
    
    private final String koreanName;
    
    Gender(String koreanName) {
        this.koreanName = koreanName;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
}

public enum ActivityLevel {
    SEDENTARY(1.2, "거의 운동 안함"),
    LIGHT(1.375, "가벼운 운동 (주 1-3일)"),
    MODERATE(1.55, "보통 운동 (주 3-5일)"),
    ACTIVE(1.725, "활발한 운동 (주 6-7일)"),
    VERY_ACTIVE(1.9, "매우 활발 (매일, 강도 높음)");
    
    private final double multiplier;
    private final String description;
    
    ActivityLevel(double multiplier, String description) {
        this.multiplier = multiplier;
        this.description = description;
    }
    
    public double getMultiplier() {
        return multiplier;
    }
    
    public String getDescription() {
        return description;
    }
}

public enum MealTime {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식"),
    LATE_NIGHT("야식");
    
    private final String koreanName;
    
    MealTime(String koreanName) {
        this.koreanName = koreanName;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
    
    // 한글 → Enum 변환
    public static MealTime fromKorean(String korean) {
        for (MealTime mt : values()) {
            if (mt.koreanName.equals(korean)) {
                return mt;
            }
        }
        throw new IllegalArgumentException("Unknown MealTime: " + korean);
    }
}

public enum DateRange {
    SEVEN_DAYS("7d", 7),
    THIRTY_DAYS("30d", 30);
    
    private final String code;
    private final int days;
    
    DateRange(String code, int days) {
        this.code = code;
        this.days = days;
    }
    
    public String getCode() {
        return code;
    }
    
    public int getDays() {
        return days;
    }
    
    public static DateRange fromCode(String code) {
        for (DateRange dr : values()) {
            if (dr.code.equals(code)) {
                return dr;
            }
        }
        return SEVEN_DAYS; // 기본값
    }
}

public enum ChallengeStatus {
    IN_PROGRESS("진행중"),
    COMPLETED("완료");
    
    private final String koreanName;
    
    ChallengeStatus(String koreanName) {
        this.koreanName = koreanName;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
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

### 프롬프트 관리 클래스 (GeminiPrompts)
```java
/**
 * Gemini API 프롬프트 중앙 관리
 * 목업(geminiService.ts)과 동일한 프롬프트 사용
 */
public class GeminiPrompts {
    
    // ===== 시스템 지시문 =====
    public static final String SYSTEM_NUTRITIONIST = 
        "당신은 전문 영양사입니다. 사용자가 업로드한 음식 사진을 보고, 사진에 있는 모든 음식의 정확한 영양 정보를 분석하고 제공하는 역할을 합니다. 모든 답변은 제시된 JSON 스키마를 엄격히 따라야 합니다.";
    
    public static final String SYSTEM_PERSONAL_TRAINER = 
        "당신은 사용자의 건강 데이터를 기반으로 맞춤형 운동 계획을 제안하는 전문 퍼스널 트레이너입니다.";
    
    public static final String SYSTEM_HEALTH_CONSULTANT = 
        "당신은 사용자의 식단 데이터를 분석하여 라이프스타일 패턴을 파악하고 맞춤형 조언을 제공하는 전문 건강 컨설턴트입니다.";
    
    public static final String SYSTEM_CHEF = 
        "당신은 사용자가 가진 재료를 바탕으로 맞춤형 건강 레시피를 만들어주는 창의적인 요리사입니다.";
    
    public static final String SYSTEM_MEAL_PLANNER = 
        "당신은 사용자의 프로필과 요구사항에 맞춰 개인화된 하루 식단을 계획해주는 전문 영양사입니다. 모든 답변은 제공된 JSON 스키마를 엄격히 준수해야 합니다.";
    
    public static final String SYSTEM_CHATBOT = """
        당신은 사용자의 건강 데이터를 잘 아는 전문 영양사 'HealthMate'입니다.
        친절하고 과학적인 근거를 바탕으로 대화해주세요.
        사용자의 질문에 답변하고, 식단 개선을 위한 구체적이고 실천 가능한 조언을 제공하세요.
        """;
    
    // ===== 이미지 분석 =====
    public static final String ANALYZE_IMAGE = 
        "이 음식 사진을 분석해서 사진에 보이는 모든 음식 각각의 칼로리와 영양 정보를 알려줘. " +
        "음식 이름, 1인분 기준 양(g), 칼로리(kcal), 그리고 탄수화물, 단백질, 지방 함량(g)을 포함해서 " +
        "아래 JSON 스키마에 맞춰서 배열(array) 형태로 정확하게 응답해줘. " +
        "만약 음식이 없거나 분석이 불가능하면, 빈 배열 `[]`을 반환해줘.";
    
    // ===== 텍스트 분석 =====
    public static String analyzeText(String foodItem) {
        return String.format(
            "음식 '%s'에 대한 일반적인 1인분 기준 영양 정보를 알려줘. " +
            "음식 이름, 1인분 기준 양(g), 칼로리(kcal), 그리고 탄수화물, 단백질, 지방 함량(g)을 포함해서 " +
            "아래 JSON 스키마에 맞춰서 정확하게 응답해줘. " +
            "만약 분석이 불가능하면, foodItem에 '분석 불가'라고 응답해줘.",
            foodItem
        );
    }
    
    // ===== 운동 추천 =====
    public static String exerciseSuggestion(double weight, double surplusKcal) {
        return String.format("""
            사용자의 현재 체중은 %.1fkg이고, 오늘 목표보다 %.0fkcal를 초과하여 섭취했습니다.
            이 초과 칼로리를 소모할 수 있는 효과적인 운동 루틴을 추천해주세요.
            - 구체적인 운동 종류와 시간을 포함해주세요. (예: 30분 빠르게 걷기 + 15분 스쿼트)
            - 전문적이면서도 동기부여가 되는 친근한 말투로 작성해주세요.
            - 답변은 1-2 문장으로 간결하게 요약해주세요.
            """, weight, surplusKcal);
    }
    
    // ===== AI 피드백 =====
    public static String aiFeedback(String mealHistory) {
        return String.format("""
            다음은 사용자의 최근 식단 기록입니다.
            %s

            이 기록을 바탕으로 전문 영양사의 관점에서 사용자의 식습관을 분석하고, 칭찬할 점과 개선할 점을 찾아 구체적인 조언을 해주세요.
            답변은 친근하고 이해하기 쉬운 말투로, 2-3문장으로 요약해서 제공해주세요.
            예: "점심에 단백질을 잘 챙겨 드시고 계시네요! 다만, 저녁 식사 칼로리가 다소 높은 경향이 있으니 샐러드를 곁들여보는 건 어떨까요?"
            """, mealHistory);
    }
    
    // ===== 주간 리포트 =====
    public static String weeklyReport(String mealHistory) {
        return String.format("""
            다음은 사용자의 지난 7일간의 식단 기록입니다.
            %s

            이 기록을 바탕으로, 당신은 매우 친절하고 지지적인 건강 코치입니다. 사용자의 노력을 칭찬하고 격려하는 따뜻한 주간 총평을 작성해주세요.
            1. 가장 잘한 점을 구체적으로 칭찬해주세요. (예: 주말에도 꾸준히 아침을 챙겨드신 점)
            2. 딱 한 가지만 개선하면 좋을 점을 부드럽게 제안해주세요. (예: 저녁 식사 후 간식 섭취 빈도를 조금 줄여보는 건 어떨까요?)
            3. 전체적인 답변은 긍정적인 톤으로, 3~4문장 이내로 작성해주세요.
            """, mealHistory);
    }
    
    // ===== 패턴 분석 =====
    public static String patternAnalysis(int lateNightMeals, double weekdayAvgKcal, 
                                          double weekendAvgKcal, String period) {
        String base = String.format("""
            다음은 사용자의 %s간 식습관 패턴 데이터입니다.
            - 야식 횟수: %d회
            - 평일 평균 섭취 칼로리: %.0f kcal
            - 주말 평균 섭취 칼로리: %.0f kcal

            이 데이터를 바탕으로 전문 영양사의 관점에서 사용자의 식습관 패턴을 심층 분석하고, 긍정적인 점과 개선이 필요한 점을 찾아 구체적인 조언을 해주세요.
            - 야식 습관이 있다면, 건강에 미치는 영향과 줄일 수 있는 현실적인 팁을 제안해주세요.
            - 주말과 평일의 칼로리 섭취량 차이에 대해 분석하고, 그 원인과 긍정적/부정적 측면을 설명해주세요.
            - 전체적인 답변은 **친근하고 격려하는 말투**로 작성하되, 분석 내용은 **구체적이고 명확**해야 합니다.
            - 답변은 2-3개의 문단으로 구성해주세요. 중요한 키워드는 **굵은 글씨**로 강조해주세요.
            """, period, lateNightMeals, weekdayAvgKcal, weekendAvgKcal);
        
        // 좋은 습관일 경우 추가 지시
        if (lateNightMeals == 0 && Math.abs(weekdayAvgKcal - weekendAvgKcal) < 150) {
            base += "\n\n 사용자는 야식도 먹지 않고 주중/주말 섭취량도 매우 일정합니다. " +
                    "이 좋은 습관을 칭찬하고, 꾸준히 유지할 수 있도록 격려하는 메시지를 중심으로 작성해주세요.";
        }
        
        return base;
    }
    
    // ===== AI 챌린지 =====
    public static String aiChallenge(String mealHistory) {
        return String.format("""
            당신은 사용자가 건강한 습관을 만들도록 돕는 유능한 헬스 코치입니다.
            아래는 사용자의 최근 식단 기록입니다.
            %s

            이 기록을 분석하여, 사용자가 재미있게 시도해볼 만한 '단기적이고 구체적인' 건강 챌린지(퀘스트)를 하나 제안해주세요.
            - 식습관의 약점을 보완하거나, 잘하고 있는 점을 강화하는 방향으로 제안하세요. (예: 아침 거름 -> 아침 챙겨먹기, 단백질 부족 -> 단백질 간식 추가)
            - 지루한 목표가 아닌, 게임 퀘스트처럼 흥미로운 제목과 명확한 행동 지침을 포함해야 합니다.
            - 아래 JSON 스키마에 맞춰 응답해주세요.
            """, mealHistory);
    }
    
    // ===== AI 레시피 =====
    public static String aiRecipe(String mealHistory) {
        return String.format("""
            사용자의 최근 식단은 다음과 같습니다: %s.
            이 식단을 바탕으로, 사용자가 좋아할 만한 건강하고 맛있는 레시피를 하나 추천해주세요. 
            특히 단백질이 부족해 보인다면 단백질이 풍부한 레시피를, 채소가 부족하다면 채소가 많은 레시피를 추천해주세요.
            레시피는 간단하고 따라하기 쉬워야 합니다.
            아래 JSON 스키마에 맞춰 응답해주세요.
            """, mealHistory);
    }
    
    // ===== 냉장고 파먹기 레시피 =====
    public static String fridgeRecipe(String ingredients, int age, String gender, int dailyGoal) {
        return String.format("""
            사용자의 프로필 정보는 다음과 같습니다:
            - 나이: %d세
            - 성별: %s
            - 일일 목표 칼로리: 약 %dkcal

            사용자가 현재 가지고 있는 재료는 다음과 같습니다: "%s"

            위 재료들을 최대한 활용하여, 사용자의 건강 목표에 맞는 건강하고 맛있는 1인분 레시피를 하나 추천해주세요.
            - 만약 재료가 부족하다면, 최소한의 추가 재료(예: 소금, 후추 등 기본 양념)만 사용하도록 제안해주세요.
            - 레시피는 간단하고 따라하기 쉬워야 합니다.
            - 아래 JSON 스키마에 맞춰 응답해주세요. 재료가 너무 부족하여 레시피 생성이 불가능할 경우, name 필드에 "레시피 생성 불가"라고 응답해주세요.
            """, age, gender, dailyGoal, ingredients);
    }
    
    // ===== AI 운동 계획 =====
    public static String workoutPlan(int age, String gender, double weight, int avgKcal) {
        return String.format("""
            사용자의 프로필: %d세 %s, 체중 %.1fkg.
            지난 주 평균 섭취 칼로리: %dkcal.

            이 정보를 바탕으로, 이번 주에 실천할 수 있는 현실적이고 효과적인 주간 운동 계획을 제안해주세요.
            - 전문 트레이너처럼 친근하고 동기를 부여하는 말투로 작성해주세요.
            - 월요일부터 일요일까지의 간단한 계획을 포함해주세요. (예: 월: 가벼운 조깅 30분, 수: 근력 운동, 금: 스트레칭)
            - 전체 답변은 3-4문장으로 간결하게 요약해주세요.
            """, age, gender, weight, avgKcal);
    }
    
    // ===== 단백질 음식 추천 =====
    public static String proteinSuggestions(String todayMeals) {
        return String.format("""
            사용자의 오늘 식단은 다음과 같습니다: %s.
            현재 단백질 섭취가 부족한 상황입니다.
            사용자가 식단에 간단하게 추가할 수 있는, 단백질이 풍부한 음식이나 간식 4가지를 추천해주세요.
            너무 복잡한 요리가 아닌, 편의점이나 마트에서 쉽게 구할 수 있거나 간단히 조리 가능한 것으로 제안해주세요.
            아래 JSON 스키마에 맞춰 응답해주세요.
            """, todayMeals.isEmpty() ? "아직 기록 없음" : todayMeals);
    }
    
    // ===== AI 식단 플래너 =====
    public static String mealPlan(int age, String gender, double height, double weight, int dailyGoal, String preferences) {
        return String.format("""
            사용자의 프로필 정보는 다음과 같습니다:
            - 나이: %d세
            - 성별: %s
            - 키: %.1fcm
            - 체중: %.1fkg
            - 일일 목표 칼로리: 약 %dkcal

            사용자의 추가적인 식단 요구사항: "%s"

            위 정보를 바탕으로, 사용자를 위한 건강하고 균형 잡힌 하루 식단(아침, 점심, 저녁, 그리고 선택적으로 간식 포함)을 계획해주세요.
            각 식사는 구체적인 음식 이름, 예상 칼로리, 간단한 설명을 포함해야 합니다.
            총 칼로리는 사용자의 일일 목표 칼로리에 근접해야 합니다.
            아래 JSON 스키마에 맞춰 정확하게 응답해주세요.
            """, age, gender, height, weight, dailyGoal, 
            preferences.isEmpty() ? "특별한 요구사항 없음" : preferences);
    }
    
    // ===== 챗봇 시스템 지시문 생성 =====
    public static String chatbotSystemInstruction(String mealHistory) {
        return String.format("""
            당신은 사용자의 건강 데이터를 잘 아는 전문 영양사 'HealthMate'입니다.
            친절하고 과학적인 근거를 바탕으로 대화해주세요.
            사용자의 질문에 답변하고, 식단 개선을 위한 구체적이고 실천 가능한 조언을 제공하세요.
            
            아래는 사용자의 최근 식단 기록입니다. 이 정보를 적극적으로 활용하여 개인화된 답변을 해주세요.
            ---
            %s
            ---
            """, mealHistory.isEmpty() ? "아직 식단 기록이 없습니다." : mealHistory);
    }
}
```

### JSON 응답 스키마 정의 (GeminiSchemas)
```java
/**
 * Gemini API JSON 응답 스키마 정의
 */
public class GeminiSchemas {
    
    // 영양소 분석 결과 (단일)
    public static final Map<String, Object> ANALYSIS_ITEM_SCHEMA = Map.of(
        "type", "object",
        "properties", Map.of(
            "foodItem", Map.of("type", "string", "description", "음식의 이름 (예: 김치찌개)"),
            "servingSize", Map.of("type", "number", "description", "1인분 기준 양(g)"),
            "kcal", Map.of("type", "number", "description", "총 칼로리 (kcal)"),
            "macro", Map.of(
                "type", "object",
                "properties", Map.of(
                    "carbs", Map.of("type", "number", "description", "탄수화물 함량 (g)"),
                    "protein", Map.of("type", "number", "description", "단백질 함량 (g)"),
                    "fat", Map.of("type", "number", "description", "지방 함량 (g)")
                ),
                "required", List.of("carbs", "protein", "fat")
            )
        ),
        "required", List.of("foodItem", "servingSize", "kcal", "macro")
    );
    
    // 영양소 분석 결과 (배열)
    public static final Map<String, Object> MULTI_FOOD_SCHEMA = Map.of(
        "type", "array",
        "items", ANALYSIS_ITEM_SCHEMA
    );
    
    // AI 챌린지
    public static final Map<String, Object> AI_CHALLENGE_SCHEMA = Map.of(
        "type", "object",
        "properties", Map.of(
            "title", Map.of("type", "string", "description", "도전 과제의 흥미로운 이름 (예: '수분 보충의 달인')"),
            "description", Map.of("type", "string", "description", "도전 과제에 대한 구체적이고 실행 가능한 설명 (예: '오늘 하루 물 8잔 마시기')"),
            "icon", Map.of("type", "string", "description", "도전 과제의 성격에 맞는 아이콘 이름. 'Zap', 'Target', 'Award' 중 하나를 선택하세요.")
        ),
        "required", List.of("title", "description", "icon")
    );
    
    // 레시피
    public static final Map<String, Object> RECIPE_SCHEMA = Map.of(
        "type", "object",
        "properties", Map.of(
            "name", Map.of("type", "string", "description", "레시피의 이름"),
            "kcal", Map.of("type", "number", "description", "1인분 기준 예상 칼로리"),
            "ingredients", Map.of("type", "array", "items", Map.of("type", "string"), "description", "필요한 재료 목록"),
            "instructions", Map.of("type", "array", "items", Map.of("type", "string"), "description", "조리 방법 단계별 설명")
        ),
        "required", List.of("name", "kcal", "ingredients", "instructions")
    );
    
    // 단백질 추천
    public static final Map<String, Object> PROTEIN_SUGGESTION_SCHEMA = Map.of(
        "type", "array",
        "items", Map.of(
            "type", "object",
            "properties", Map.of(
                "name", Map.of("type", "string", "description", "추천하는 단백질이 풍부한 음식 또는 간식의 이름"),
                "reason", Map.of("type", "string", "description", "이 음식을 추천하는 간략한 이유")
            ),
            "required", List.of("name", "reason")
        )
    );
    
    // 식단 플래너
    public static final Map<String, Object> MEAL_PLAN_SCHEMA = Map.of(
        "type", "object",
        "properties", Map.of(
            "breakfast", Map.of(
                "type", "object",
                "properties", Map.of(
                    "name", Map.of("type", "string"),
                    "kcal", Map.of("type", "number"),
                    "description", Map.of("type", "string")
                )
            ),
            "lunch", Map.of(
                "type", "object",
                "properties", Map.of(
                    "name", Map.of("type", "string"),
                    "kcal", Map.of("type", "number"),
                    "description", Map.of("type", "string")
                )
            ),
            "dinner", Map.of(
                "type", "object",
                "properties", Map.of(
                    "name", Map.of("type", "string"),
                    "kcal", Map.of("type", "number"),
                    "description", Map.of("type", "string")
                )
            ),
            "snacks", Map.of(
                "type", "object",
                "properties", Map.of(
                    "name", Map.of("type", "string"),
                    "kcal", Map.of("type", "number"),
                    "description", Map.of("type", "string")
                )
            ),
            "totalKcal", Map.of("type", "number", "description", "하루 총 예상 칼로리")
        ),
        "required", List.of("breakfast", "lunch", "dinner", "totalKcal")
    );
}
```

### GeminiService (전체 구현 - 13개 AI 함수)
```java
@Service
@Slf4j
public class GeminiService {
    
    private static final String MODEL = "gemini-2.0-flash";
    
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
    
    // ===== 1. 이미지 분석 (음식 인식) =====
    public List<AnalysisResult> analyzeImage(MultipartFile file) throws IOException {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(
                        new TextPart(GeminiPrompts.ANALYZE_IMAGE),
                        new ImagePart(base64Image, file.getContentType())
                    ))
                    .build()
            ))
            .systemInstruction(GeminiPrompts.SYSTEM_NUTRITIONIST)
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(GeminiSchemas.MULTI_FOOD_SCHEMA)
                .build())
            .build();
        
        String jsonText = callGeminiApi(request);
        List<AnalysisResult> results = objectMapper.readValue(
            jsonText, new TypeReference<List<AnalysisResult>>() {}
        );
        
        if (results.isEmpty()) {
            throw new AIAnalysisException("인식할 수 없는 음식입니다.");
        }
        
        return results;
    }
    
    // ===== 2. 텍스트 기반 분석 =====
    public AnalysisResult analyzeText(String foodItem) {
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(GeminiPrompts.analyzeText(foodItem))))
                    .build()
            ))
            .systemInstruction(GeminiPrompts.SYSTEM_NUTRITIONIST)
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(GeminiSchemas.ANALYSIS_ITEM_SCHEMA)
                .build())
            .build();
        
        try {
            String jsonText = callGeminiApi(request);
            AnalysisResult result = objectMapper.readValue(jsonText, AnalysisResult.class);
            
            if ("분석 불가".equals(result.getFoodItem())) {
                throw new AIAnalysisException("'" + foodItem + "'에 대한 정보를 찾을 수 없습니다.");
            }
            
            return result;
        } catch (IOException e) {
            throw new AIAnalysisException("AI 분석 결과 파싱 실패", e);
        }
    }
    
    // ===== 3. 운동 추천 =====
    public String getExerciseSuggestion(double surplusKcal, double userWeight) {
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(
                        GeminiPrompts.exerciseSuggestion(userWeight, surplusKcal)
                    )))
                    .build()
            ))
            .systemInstruction(GeminiPrompts.SYSTEM_PERSONAL_TRAINER)
            .build();
        
        return callGeminiApi(request);
    }
    
    // ===== 4. AI 피드백 =====
    public String getAIFeedback(List<Meal> meals) {
        if (meals.isEmpty()) {
            return "아직 식단 기록이 없어서 분석할 수 없어요. 오늘 식단을 기록하고 다시 확인해주세요!";
        }
        
        String mealHistory = formatMealHistory(meals.subList(0, Math.min(15, meals.size())));
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(GeminiPrompts.aiFeedback(mealHistory))))
                    .build()
            ))
            .build();
        
        return callGeminiApi(request);
    }
    
    // ===== 5. 주간 리포트 =====
    public String getAIWeeklyReport(List<Meal> meals) {
        if (meals.size() < 3) {
            return "지난 한 주도 수고 많으셨습니다! 꾸준히 식단을 기록하며 건강한 습관을 만들어가요. 다음 주도 화이팅!";
        }
        
        String mealHistory = formatMealHistoryWithDay(meals);
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(GeminiPrompts.weeklyReport(mealHistory))))
                    .build()
            ))
            .build();
        
        return callGeminiApi(request);
    }
    
    // ===== 6. 패턴 분석 =====
    public String getAIPatternAnalysis(PatternAnalysisRequest stats) {
        String period = stats.getDateRange() == DateRange.SEVEN_DAYS ? "최근 7일" : "최근 30일";
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(
                        GeminiPrompts.patternAnalysis(
                            stats.getLateNightMeals(),
                            stats.getWeekdayAvgKcal(),
                            stats.getWeekendAvgKcal(),
                            period
                        )
                    )))
                    .build()
            ))
            .systemInstruction(GeminiPrompts.SYSTEM_HEALTH_CONSULTANT)
            .build();
        
        return callGeminiApi(request);
    }
    
    // ===== 7. AI 챌린지 생성 =====
    public AIChallengeResponse getAIChallenge(List<Meal> meals) {
        String mealHistory = formatMealHistorySimple(meals.subList(0, Math.min(10, meals.size())));
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(GeminiPrompts.aiChallenge(mealHistory))))
                    .build()
            ))
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(GeminiSchemas.AI_CHALLENGE_SCHEMA)
                .build())
            .build();
        
        try {
            String jsonText = callGeminiApi(request);
            return objectMapper.readValue(jsonText, AIChallengeResponse.class);
        } catch (IOException e) {
            throw new AIAnalysisException("AI 챌린지 생성 실패", e);
        }
    }
    
    // ===== 8. AI 레시피 추천 =====
    public RecipeResponse getAIRecipe(List<Meal> meals) {
        String mealHistory = meals.subList(0, Math.min(5, meals.size())).stream()
            .map(m -> m.getFoodItem() + " (" + m.getProtein() + "g 단백질)")
            .collect(Collectors.joining(", "));
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(GeminiPrompts.aiRecipe(mealHistory))))
                    .build()
            ))
            .systemInstruction(GeminiPrompts.SYSTEM_CHEF)
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(GeminiSchemas.RECIPE_SCHEMA)
                .build())
            .build();
        
        try {
            String jsonText = callGeminiApi(request);
            return objectMapper.readValue(jsonText, RecipeResponse.class);
        } catch (IOException e) {
            throw new AIAnalysisException("AI 레시피 생성 실패", e);
        }
    }
    
    // ===== 9. 냉장고 파먹기 레시피 =====
    public RecipeResponse getFridgeRecipe(String ingredients, User user) {
        int dailyGoal = CalorieCalculator.calculateDailyGoal(user);
        String gender = user.getGender() == Gender.MALE ? "남성" : "여성";
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(
                        GeminiPrompts.fridgeRecipe(ingredients, user.getAge(), gender, dailyGoal)
                    )))
                    .build()
            ))
            .systemInstruction(GeminiPrompts.SYSTEM_CHEF)
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(GeminiSchemas.RECIPE_SCHEMA)
                .build())
            .build();
        
        try {
            String jsonText = callGeminiApi(request);
            RecipeResponse recipe = objectMapper.readValue(jsonText, RecipeResponse.class);
            
            if ("레시피 생성 불가".equals(recipe.getName())) {
                throw new AIAnalysisException("주어진 재료만으로는 만들 수 있는 레시피를 찾기 어렵습니다. 재료를 추가해보세요.");
            }
            
            return recipe;
        } catch (IOException e) {
            throw new AIAnalysisException("AI 레시피 생성 실패", e);
        }
    }
    
    // ===== 10. AI 운동 계획 =====
    public String getAIWorkoutPlan(List<Meal> weeklyMeals, User user) {
        int avgKcal = (int) weeklyMeals.stream()
            .mapToDouble(Meal::getKcal)
            .average()
            .orElse(0);
        
        String gender = user.getGender() == Gender.MALE ? "남성" : "여성";
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(
                        GeminiPrompts.workoutPlan(user.getAge(), gender, user.getWeight(), avgKcal)
                    )))
                    .build()
            ))
            .build();
        
        return callGeminiApi(request);
    }
    
    // ===== 11. 단백질 음식 추천 =====
    public List<ProteinSuggestionResponse> getProteinFoodSuggestions(List<Meal> todayMeals) {
        String mealNames = todayMeals.stream()
            .map(Meal::getFoodItem)
            .collect(Collectors.joining(", "));
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(GeminiPrompts.proteinSuggestions(mealNames))))
                    .build()
            ))
            .systemInstruction("당신은 사용자의 식단 데이터를 기반으로 개인화된 음식 추천을 제공하는 영양 전문가입니다.")
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(GeminiSchemas.PROTEIN_SUGGESTION_SCHEMA)
                .build())
            .build();
        
        try {
            String jsonText = callGeminiApi(request);
            return objectMapper.readValue(jsonText, 
                new TypeReference<List<ProteinSuggestionResponse>>() {});
        } catch (IOException e) {
            throw new AIAnalysisException("AI 단백질 추천 생성 실패", e);
        }
    }
    
    // ===== 12. AI 식단 플래너 =====
    public MealPlanResponse getAIMealPlan(User user, String preferences) {
        int dailyGoal = CalorieCalculator.calculateDailyGoal(user);
        String gender = user.getGender() == Gender.MALE ? "남성" : "여성";
        
        GeminiRequest request = GeminiRequest.builder()
            .contents(List.of(
                GeminiContent.builder()
                    .parts(List.of(new TextPart(
                        GeminiPrompts.mealPlan(
                            user.getAge(), gender, user.getHeight(), 
                            user.getWeight(), dailyGoal, preferences
                        )
                    )))
                    .build()
            ))
            .systemInstruction(GeminiPrompts.SYSTEM_MEAL_PLANNER)
            .generationConfig(GenerationConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(GeminiSchemas.MEAL_PLAN_SCHEMA)
                .build())
            .build();
        
        try {
            String jsonText = callGeminiApi(request);
            return objectMapper.readValue(jsonText, MealPlanResponse.class);
        } catch (IOException e) {
            throw new AIAnalysisException("AI 식단 계획 생성 실패", e);
        }
    }
    
    // ===== 13. AI 챗봇 =====
    public String chat(String message, List<ChatMessage> history, List<Meal> recentMeals) {
        String mealHistory = formatMealHistoryForChat(recentMeals);
        String systemInstruction = GeminiPrompts.chatbotSystemInstruction(mealHistory);
        
        // 대화 히스토리를 Gemini 형식으로 변환
        List<GeminiContent> contents = new ArrayList<>();
        for (ChatMessage msg : history) {
            contents.add(GeminiContent.builder()
                .role(msg.getRole().equals("user") ? "user" : "model")
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
            .systemInstruction(systemInstruction)
            .build();
        
        return callGeminiApi(request);
    }
    
    // ===== 공통 API 호출 메서드 =====
    private String callGeminiApi(GeminiRequest request) {
        try {
            GeminiResponse response = webClient.post()
                .uri("/v1/models/" + MODEL + ":generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block();
            
            if (response == null || response.getCandidates().isEmpty()) {
                throw new AIAnalysisException("AI 분석에 실패했습니다.");
            }
            
            return response.getCandidates().get(0)
                .getContent().getParts().get(0).getText().trim();
                
        } catch (WebClientResponseException e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new AIAnalysisException("AI 서비스에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    // ===== 유틸리티 메서드 =====
    private String formatMealHistory(List<Meal> meals) {
        return meals.stream()
            .map(m -> String.format("- %s %s: %dkcal (탄수화물 %.0fg, 단백질 %.0fg, 지방 %.0fg)",
                m.getMealTime().getKoreanName(), m.getFoodItem(), 
                (int) m.getKcal().doubleValue(), m.getCarbs(), m.getProtein(), m.getFat()))
            .collect(Collectors.joining("\n"));
    }
    
    private String formatMealHistoryWithDay(List<Meal> meals) {
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        return meals.stream()
            .map(m -> {
                int dayOfWeek = m.getDate().getDayOfWeek().getValue() % 7;
                return String.format("- %s요일 %s: %s (%dkcal)",
                    days[dayOfWeek], m.getMealTime().getKoreanName(), 
                    m.getFoodItem(), (int) m.getKcal().doubleValue());
            })
            .collect(Collectors.joining("\n"));
    }
    
    private String formatMealHistorySimple(List<Meal> meals) {
        return meals.stream()
            .map(m -> String.format("- %s %s: %dkcal (단백질 %.0fg)",
                m.getMealTime().getKoreanName(), m.getFoodItem(), 
                (int) m.getKcal().doubleValue(), m.getProtein()))
            .collect(Collectors.joining("\n"));
    }
    
    private String formatMealHistoryForChat(List<Meal> meals) {
        return meals.subList(0, Math.min(20, meals.size())).stream()
            .map(m -> String.format("- %s %s %s: %dkcal",
                m.getDate().toString(), m.getMealTime().getKoreanName(), 
                m.getFoodItem(), (int) m.getKcal().doubleValue()))
            .collect(Collectors.joining("\n"));
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

### AnalysisResult DTO (목업과 동일한 구조)
```java
/**
 * 음식 분석 결과 DTO
 * 목업(types.ts)의 AnalysisResult와 동일한 구조 (macro 중첩)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String foodItem;
    private Double servingSize;
    private Double kcal;
    private MacroNutrients macro;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MacroNutrients {
        private Double carbs;
        private Double protein;
        private Double fat;
    }
    
    // Entity 변환용
    public Double getCarbs() {
        return macro != null ? macro.getCarbs() : 0.0;
    }
    
    public Double getProtein() {
        return macro != null ? macro.getProtein() : 0.0;
    }
    
    public Double getFat() {
        return macro != null ? macro.getFat() : 0.0;
    }
}
```

### 추가 Response DTO
```java
/**
 * AI 챌린지 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChallengeResponse {
    private String title;
    private String description;
    private String icon; // "Zap", "Target", "Award"
}

/**
 * 레시피 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeResponse {
    private String name;
    private Integer kcal;
    private List<String> ingredients;
    private List<String> instructions;
}

/**
 * 단백질 추천 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProteinSuggestionResponse {
    private String name;
    private String reason;
}

/**
 * 식단 플랜 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {
    private PlannedMeal breakfast;
    private PlannedMeal lunch;
    private PlannedMeal dinner;
    private PlannedMeal snacks;
    private Integer totalKcal;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlannedMeal {
        private String name;
        private Integer kcal;
        private String description;
    }
}

/**
 * 패턴 분석 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternAnalysisRequest {
    private Integer lateNightMeals;
    private Double weekdayAvgKcal;
    private Double weekendAvgKcal;
    private DateRange dateRange;
}

/**
 * 일일 통계 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsResponse {
    private LocalDate date;
    private Integer totalKcal;
    private Integer goalKcal;
    private Double progress; // 0.0 ~ 1.0
    private MacroSummary macro;
    private Integer waterGlasses;
    private List<MealSummary> meals;
    
    @Data
    @Builder
    public static class MacroSummary {
        private Double carbs;
        private Double protein;
        private Double fat;
        private Double carbsPercent;
        private Double proteinPercent;
        private Double fatPercent;
    }
    
    @Data
    @Builder
    public static class MealSummary {
        private MealTime mealTime;
        private Integer kcal;
        private Integer count;
    }
}

/**
 * 주간/월간 통계 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodStatsResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private Integer recordedDays;
    private Double avgKcal;
    private Double avgProtein;
    private Double avgCarbs;
    private Double avgFat;
    private List<DailyKcalData> dailyData; // 차트용 데이터
    private PatternStats pattern;
    
    @Data
    @Builder
    public static class DailyKcalData {
        private LocalDate date;
        private Integer kcal;
    }
    
    @Data
    @Builder
    public static class PatternStats {
        private Integer lateNightMeals;
        private Double weekdayAvgKcal;
        private Double weekendAvgKcal;
    }
}

/**
 * 챌린지 진행 상황 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeProgressResponse {
    private String id;
    private String title;
    private String description;
    private String icon;
    private Integer goal;
    private Integer current;
    private Double progress; // 0.0 ~ 1.0
    private String status; // "진행중", "완료"
}
```

### CalorieCalculator 유틸리티
```java
/**
 * 칼로리 계산 유틸리티
 * Mifflin-St Jeor 공식 사용
 */
public class CalorieCalculator {
    
    /**
     * 기초대사량(BMR) 계산
     */
    public static double calculateBMR(User user) {
        double bmr;
        if (user.getGender() == Gender.MALE) {
            // 남성: BMR = 10 × 체중(kg) + 6.25 × 키(cm) - 5 × 나이 + 5
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            // 여성: BMR = 10 × 체중(kg) + 6.25 × 키(cm) - 5 × 나이 - 161
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }
        return bmr;
    }
    
    /**
     * 일일 목표 칼로리(TDEE) 계산
     */
    public static int calculateDailyGoal(User user) {
        double bmr = calculateBMR(user);
        double tdee = bmr * user.getActivityLevel().getMultiplier();
        return (int) Math.round(tdee);
    }
    
    /**
     * 권장 단백질 섭취량 계산 (g)
     * 체중 1kg당 0.8~1.2g
     */
    public static int calculateProteinGoal(User user) {
        double multiplier = switch (user.getActivityLevel()) {
            case SEDENTARY, LIGHT -> 0.8;
            case MODERATE -> 1.0;
            case ACTIVE, VERY_ACTIVE -> 1.2;
        };
        return (int) Math.round(user.getWeight() * multiplier);
    }
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

## AIController 구현 (전체 13개 AI 기능)

```java
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {
    
    private final GeminiService geminiService;
    private final MealService mealService;
    private final UserService userService;
    private final AnalyticsService analyticsService;
    
    /**
     * 1. AI 챗봇 - 대화
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<Meal> recentMeals = mealService.getRecentMeals(userId, 20);
        
        String reply = geminiService.chat(request.getMessage(), request.getHistory(), recentMeals);
        
        return ResponseEntity.ok(ApiResponse.success(
            ChatResponse.builder()
                .reply(reply)
                .sessionId(request.getSessionId())
                .build()
        ));
    }
    
    /**
     * 2. AI 식단 플래너
     */
    @PostMapping("/meal-plan")
    public ResponseEntity<ApiResponse<MealPlanResponse>> getMealPlan(
            @Valid @RequestBody MealPlanRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.getUser(userId);
        
        MealPlanResponse mealPlan = geminiService.getAIMealPlan(user, request.getPreferences());
        
        return ResponseEntity.ok(ApiResponse.success(mealPlan));
    }
    
    /**
     * 3. 냉장고 파먹기 레시피
     */
    @PostMapping("/fridge-recipe")
    public ResponseEntity<ApiResponse<RecipeResponse>> getFridgeRecipe(
            @Valid @RequestBody FridgeRecipeRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.getUser(userId);
        
        RecipeResponse recipe = geminiService.getFridgeRecipe(request.getIngredients(), user);
        
        return ResponseEntity.ok(ApiResponse.success(recipe));
    }
    
    /**
     * 4. AI 운동 추천 (칼로리 초과 시)
     */
    @GetMapping("/exercise-suggestion")
    public ResponseEntity<ApiResponse<ExerciseSuggestionResponse>> getExerciseSuggestion(
            @RequestParam double surplusKcal,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.getUser(userId);
        
        String suggestion = geminiService.getExerciseSuggestion(surplusKcal, user.getWeight());
        
        return ResponseEntity.ok(ApiResponse.success(
            ExerciseSuggestionResponse.builder()
                .suggestion(suggestion)
                .surplusKcal(surplusKcal)
                .build()
        ));
    }
    
    /**
     * 5. AI 피드백 (식단 분석)
     */
    @GetMapping("/feedback")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getAIFeedback(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<Meal> recentMeals = mealService.getRecentMeals(userId, 15);
        
        String feedback = geminiService.getAIFeedback(recentMeals);
        
        return ResponseEntity.ok(ApiResponse.success(
            FeedbackResponse.builder()
                .feedback(feedback)
                .mealCount(recentMeals.size())
                .build()
        ));
    }
    
    /**
     * 6. AI 주간 리포트
     */
    @GetMapping("/weekly-report")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getWeeklyReport(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        
        List<Meal> weeklyMeals = mealService.getMealsByDateRange(userId, startDate, endDate);
        String report = geminiService.getAIWeeklyReport(weeklyMeals);
        
        return ResponseEntity.ok(ApiResponse.success(
            WeeklyReportResponse.builder()
                .report(report)
                .startDate(startDate)
                .endDate(endDate)
                .mealCount(weeklyMeals.size())
                .build()
        ));
    }
    
    /**
     * 7. AI 패턴 분석
     */
    @PostMapping("/pattern-analysis")
    public ResponseEntity<ApiResponse<PatternAnalysisResponse>> getPatternAnalysis(
            @Valid @RequestBody PatternAnalysisRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        
        // 통계 데이터가 없으면 계산
        if (request.getLateNightMeals() == null) {
            request = analyticsService.calculatePatternStats(userId, request.getDateRange());
        }
        
        String analysis = geminiService.getAIPatternAnalysis(request);
        
        return ResponseEntity.ok(ApiResponse.success(
            PatternAnalysisResponse.builder()
                .analysis(analysis)
                .lateNightMeals(request.getLateNightMeals())
                .weekdayAvgKcal(request.getWeekdayAvgKcal())
                .weekendAvgKcal(request.getWeekendAvgKcal())
                .dateRange(request.getDateRange().getCode())
                .build()
        ));
    }
    
    /**
     * 8. AI 챌린지 생성
     */
    @GetMapping("/challenge")
    public ResponseEntity<ApiResponse<AIChallengeResponse>> getAIChallenge(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<Meal> recentMeals = mealService.getRecentMeals(userId, 10);
        
        AIChallengeResponse challenge = geminiService.getAIChallenge(recentMeals);
        
        return ResponseEntity.ok(ApiResponse.success(challenge));
    }
    
    /**
     * 9. AI 레시피 추천
     */
    @GetMapping("/recipe")
    public ResponseEntity<ApiResponse<RecipeResponse>> getAIRecipe(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<Meal> recentMeals = mealService.getRecentMeals(userId, 5);
        
        RecipeResponse recipe = geminiService.getAIRecipe(recentMeals);
        
        return ResponseEntity.ok(ApiResponse.success(recipe));
    }
    
    /**
     * 10. AI 운동 계획
     */
    @GetMapping("/workout-plan")
    public ResponseEntity<ApiResponse<WorkoutPlanResponse>> getWorkoutPlan(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.getUser(userId);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        List<Meal> weeklyMeals = mealService.getMealsByDateRange(userId, startDate, endDate);
        
        String workoutPlan = geminiService.getAIWorkoutPlan(weeklyMeals, user);
        
        return ResponseEntity.ok(ApiResponse.success(
            WorkoutPlanResponse.builder()
                .plan(workoutPlan)
                .build()
        ));
    }
    
    /**
     * 11. 단백질 음식 추천
     */
    @GetMapping("/protein-suggestions")
    public ResponseEntity<ApiResponse<List<ProteinSuggestionResponse>>> getProteinSuggestions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<Meal> todayMeals = mealService.getTodayMeals(userId);
        
        List<ProteinSuggestionResponse> suggestions = geminiService.getProteinFoodSuggestions(todayMeals);
        
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}

// AI Controller용 Request/Response DTO
@Data
@Builder
public class ChatRequest {
    private String message;
    private String sessionId;
    private List<ChatMessage> history;
}

@Data
@Builder
public class ChatResponse {
    private String reply;
    private String sessionId;
}

@Data
@Builder
public class ChatMessage {
    private String role; // "user" or "model"
    private String text;
}

@Data
@Builder
public class MealPlanRequest {
    private String preferences;
}

@Data
@Builder
public class FridgeRecipeRequest {
    @NotBlank(message = "재료를 입력해주세요")
    private String ingredients;
}

@Data
@Builder
public class ExerciseSuggestionResponse {
    private String suggestion;
    private Double surplusKcal;
}

@Data
@Builder
public class FeedbackResponse {
    private String feedback;
    private Integer mealCount;
}

@Data
@Builder
public class WeeklyReportResponse {
    private String report;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer mealCount;
}

@Data
@Builder
public class PatternAnalysisResponse {
    private String analysis;
    private Integer lateNightMeals;
    private Double weekdayAvgKcal;
    private Double weekendAvgKcal;
    private String dateRange;
}

@Data
@Builder
public class WorkoutPlanResponse {
    private String plan;
}
```

---

## AnalyticsController 구현

```java
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    private final UserService userService;
    
    /**
     * 일일 통계 조회
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyStatsResponse>> getDailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        LocalDate targetDate = date != null ? date : LocalDate.now();
        
        DailyStatsResponse stats = analyticsService.getDailyStats(userId, targetDate);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 기간별 통계 조회 (7일/30일)
     */
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<PeriodStatsResponse>> getPeriodStats(
            @RequestParam(defaultValue = "7d") String range,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        DateRange dateRange = DateRange.fromCode(range);
        
        PeriodStatsResponse stats = analyticsService.getPeriodStats(userId, dateRange);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * 칼로리 트렌드 데이터 (차트용)
     */
    @GetMapping("/calorie-trend")
    public ResponseEntity<ApiResponse<List<DailyKcalData>>> getCalorieTrend(
            @RequestParam(defaultValue = "7d") String range,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        DateRange dateRange = DateRange.fromCode(range);
        
        List<DailyKcalData> trend = analyticsService.getCalorieTrend(userId, dateRange);
        
        return ResponseEntity.ok(ApiResponse.success(trend));
    }
    
    /**
     * 식사 시간대별 히트맵 데이터
     */
    @GetMapping("/meal-time-heatmap")
    public ResponseEntity<ApiResponse<List<MealTimeHeatmapData>>> getMealTimeHeatmap(
            @RequestParam(defaultValue = "7d") String range,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        DateRange dateRange = DateRange.fromCode(range);
        
        List<MealTimeHeatmapData> heatmap = analyticsService.getMealTimeHeatmap(userId, dateRange);
        
        return ResponseEntity.ok(ApiResponse.success(heatmap));
    }
    
    /**
     * 영양소 비율 데이터 (레이더 차트용)
     */
    @GetMapping("/nutrient-balance")
    public ResponseEntity<ApiResponse<NutrientBalanceResponse>> getNutrientBalance(
            @RequestParam(defaultValue = "7d") String range,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        DateRange dateRange = DateRange.fromCode(range);
        
        NutrientBalanceResponse balance = analyticsService.getNutrientBalance(userId, dateRange);
        
        return ResponseEntity.ok(ApiResponse.success(balance));
    }
}

// Analytics용 추가 DTO
@Data
@Builder
public class DailyKcalData {
    private LocalDate date;
    private Integer kcal;
}

@Data
@Builder
public class MealTimeHeatmapData {
    private String dayOfWeek; // "월", "화", ...
    private MealTime mealTime;
    private Integer avgKcal;
    private Integer count;
}

@Data
@Builder
public class NutrientBalanceResponse {
    private Double carbsPercent;
    private Double proteinPercent;
    private Double fatPercent;
    private Double recommendedCarbsPercent; // 50-60%
    private Double recommendedProteinPercent; // 15-20%
    private Double recommendedFatPercent; // 20-30%
}
```

---

## ChallengeController 구현

```java
@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {
    
    private final ChallengeService challengeService;
    private final BadgeService badgeService;
    
    /**
     * 진행 중인 챌린지 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChallengeProgressResponse>>> getChallenges(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<ChallengeProgressResponse> challenges = challengeService.getUserChallenges(userId);
        
        return ResponseEntity.ok(ApiResponse.success(challenges));
    }
    
    /**
     * 챌린지 시작
     */
    @PostMapping("/{challengeId}/start")
    public ResponseEntity<ApiResponse<ChallengeProgressResponse>> startChallenge(
            @PathVariable String challengeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ChallengeProgressResponse progress = challengeService.startChallenge(userId, challengeId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(progress));
    }
    
    /**
     * 챌린지 진행 상황 업데이트
     */
    @PatchMapping("/{challengeId}/progress")
    public ResponseEntity<ApiResponse<ChallengeProgressResponse>> updateChallengeProgress(
            @PathVariable String challengeId,
            @RequestBody ChallengeUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        ChallengeProgressResponse progress = challengeService.updateProgress(
            userId, challengeId, request.getIncrement()
        );
        
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
    
    /**
     * 획득한 배지 목록 조회
     */
    @GetMapping("/badges")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getBadges(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<BadgeResponse> badges = badgeService.getUserBadges(userId);
        
        return ResponseEntity.ok(ApiResponse.success(badges));
    }
    
    /**
     * 모든 배지 목록 조회 (잠금/해제 상태 포함)
     */
    @GetMapping("/badges/all")
    public ResponseEntity<ApiResponse<List<BadgeWithStatusResponse>>> getAllBadges(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<BadgeWithStatusResponse> badges = badgeService.getAllBadgesWithStatus(userId);
        
        return ResponseEntity.ok(ApiResponse.success(badges));
    }
}

// Challenge용 추가 DTO
@Data
@Builder
public class ChallengeUpdateRequest {
    private Integer increment;
}

@Data
@Builder
public class BadgeResponse {
    private String id;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime unlockedAt;
}

@Data
@Builder
public class BadgeWithStatusResponse {
    private String id;
    private String name;
    private String description;
    private String icon;
    private Boolean isUnlocked;
    private LocalDateTime unlockedAt;
    private String condition;
}
```

---

## WaterIntakeController 구현

```java
@RestController
@RequestMapping("/api/water")
@RequiredArgsConstructor
public class WaterIntakeController {
    
    private final WaterIntakeService waterIntakeService;
    
    /**
     * 오늘 수분 섭취량 조회
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> getTodayWaterIntake(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        WaterIntakeResponse response = waterIntakeService.getTodayIntake(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 수분 섭취 추가 (+1잔)
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> addWaterGlass(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        WaterIntakeResponse response = waterIntakeService.addGlass(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 수분 섭취 감소 (-1잔)
     */
    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> removeWaterGlass(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        WaterIntakeResponse response = waterIntakeService.removeGlass(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

@Data
@Builder
public class WaterIntakeResponse {
    private LocalDate date;
    private Integer glasses;
    private Integer goalGlasses; // 보통 8잔
    private Double progress; // 0.0 ~ 1.0
}
```

---

## 구현 단계 (업데이트 - 목업 기반 완전한 계획)

### Phase 1: 기본 인프라 구축 (1주)
- [ ] Supabase 프로젝트 생성 및 설정
- [ ] Supabase PostgreSQL 연결 확인
- [ ] Supabase Storage 버킷 생성 (`meal-images`)
- [ ] Spring Boot 프로젝트 생성 (Spring Initializr)
- [ ] Gradle 빌드 설정 (의존성 추가)
- [ ] application.yml 환경 설정 (Supabase, Gemini API 연결)
- [ ] JPA Entity 설계 및 생성 (User, Meal, WaterIntake, Group, Challenge 등)
- [ ] Enum 정의 (MealTime, Gender, ActivityLevel, DateRange, ChallengeStatus)
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

### Phase 5: AI 서비스 통합 (2주) ⭐ 핵심
- [ ] WebClient 설정 (Gemini API 연결)
- [ ] GeminiPrompts 클래스 (프롬프트 중앙 관리)
- [ ] GeminiSchemas 클래스 (JSON 응답 스키마 정의)
- [ ] GeminiService 구현 (13개 AI 함수):
  - [ ] 1. analyzeImage - 이미지 분석 (다중 음식 인식)
  - [ ] 2. analyzeText - 텍스트 기반 영양 분석
  - [ ] 3. getExerciseSuggestion - 운동 추천
  - [ ] 4. getAIFeedback - 식단 피드백
  - [ ] 5. getAIWeeklyReport - 주간 리포트
  - [ ] 6. getAIPatternAnalysis - 식습관 패턴 분석
  - [ ] 7. getAIChallenge - AI 챌린지 생성
  - [ ] 8. getAIRecipe - AI 레시피 추천
  - [ ] 9. getFridgeRecipe - 냉장고 파먹기 레시피
  - [ ] 10. getAIWorkoutPlan - 주간 운동 계획
  - [ ] 11. getProteinFoodSuggestions - 단백질 음식 추천
  - [ ] 12. getAIMealPlan - AI 식단 플래너
  - [ ] 13. chat - AI 챗봇 (대화 히스토리 포함)
- [ ] AIController 구현 (11개 엔드포인트)
- [ ] AI 예외 처리 (AIAnalysisException)

### Phase 6: 분석 & 통계 (1주)
- [ ] AnalyticsService 구현
- [ ] CalorieCalculator 유틸리티 (BMR/TDEE 계산)
- [ ] AnalyticsController 구현:
  - [ ] GET /api/analytics/daily - 일일 통계 조회
  - [ ] GET /api/analytics/period - 기간별 통계 (7d/30d)
  - [ ] GET /api/analytics/calorie-trend - 칼로리 트렌드 (차트용)
  - [ ] GET /api/analytics/meal-time-heatmap - 식사 시간 히트맵
  - [ ] GET /api/analytics/nutrient-balance - 영양소 비율 (레이더 차트용)
- [ ] 패턴 분석 로직 (야식 횟수, 주중/주말 비교)

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
- User, Meal, WaterIntake, Group, Challenge, Badge Entity 생성
- JPA Repository 인터페이스 생성

### 5. Phase 1 시작: 기본 인프라 구축
- [ ] Spring Security 설정
- [ ] JWT 인증 구현
- [ ] 전역 예외 처리
- [ ] API 문서 (Swagger)

---

## 📊 API 엔드포인트 요약

### 인증 (Auth)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/refresh` | 토큰 갱신 |

### 식단 (Meals)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/meals/analyze` | 이미지 분석 |
| POST | `/api/meals` | 식단 기록 확정 |
| GET | `/api/meals` | 식단 목록 조회 |
| DELETE | `/api/meals/{id}` | 식단 삭제 |
| POST | `/api/meals/manual` | 수동 식단 추가 |

### AI 서비스 (13개 기능)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/ai/chat` | AI 챗봇 대화 |
| POST | `/api/ai/meal-plan` | AI 식단 플래너 |
| POST | `/api/ai/fridge-recipe` | 냉장고 파먹기 레시피 |
| GET | `/api/ai/exercise-suggestion` | 운동 추천 |
| GET | `/api/ai/feedback` | AI 피드백 |
| GET | `/api/ai/weekly-report` | 주간 리포트 |
| POST | `/api/ai/pattern-analysis` | 패턴 분석 |
| GET | `/api/ai/challenge` | AI 챌린지 생성 |
| GET | `/api/ai/recipe` | AI 레시피 추천 |
| GET | `/api/ai/workout-plan` | 운동 계획 |
| GET | `/api/ai/protein-suggestions` | 단백질 추천 |

### 통계 (Analytics)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/analytics/daily` | 일일 통계 |
| GET | `/api/analytics/period` | 기간별 통계 |
| GET | `/api/analytics/calorie-trend` | 칼로리 트렌드 |
| GET | `/api/analytics/meal-time-heatmap` | 식사 시간 히트맵 |
| GET | `/api/analytics/nutrient-balance` | 영양소 비율 |

### 챌린지 (Challenges)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/challenges` | 챌린지 목록 |
| POST | `/api/challenges/{id}/start` | 챌린지 시작 |
| PATCH | `/api/challenges/{id}/progress` | 진행 상황 업데이트 |
| GET | `/api/challenges/badges` | 획득 배지 목록 |
| GET | `/api/challenges/badges/all` | 전체 배지 목록 |

### 수분 섭취 (Water)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/water/today` | 오늘 수분 섭취량 |
| POST | `/api/water/add` | 물 1잔 추가 |
| POST | `/api/water/remove` | 물 1잔 감소 |

### 그룹 (Groups)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/groups` | 그룹 생성 |
| GET | `/api/groups` | 그룹 목록 |
| GET | `/api/groups/{id}` | 그룹 상세 |
| POST | `/api/groups/{id}/join` | 그룹 가입 |
| POST | `/api/groups/{id}/leave` | 그룹 탈퇴 |
| GET | `/api/groups/{id}/feed` | 그룹 피드 |
| POST | `/api/groups/{id}/feed` | 피드 작성 |
| POST | `/api/groups/{id}/feed/{feedId}/like` | 좋아요 |

---

**작성일**: 2024-11-27  
**버전**: 3.0 (목업 기반 완전한 서버 계획)  
**작성자**: AI Assistant

### 변경 이력
| 버전 | 날짜 | 내용 |
|------|------|------|
| 2.1 | 2024-11-26 | 초안 작성 (Spring Boot + Supabase) |
| 3.0 | 2024-11-27 | 목업 기반 완전한 계획으로 업그레이드 |
| | | - 13개 AI 함수 전체 구현 (GeminiService) |
| | | - 프롬프트 관리 클래스 추가 (GeminiPrompts) |
| | | - JSON 스키마 정의 추가 (GeminiSchemas) |
| | | - Challenge, Badge, AIGeneratedChallenge Entity 추가 |
| | | - AIController, AnalyticsController, ChallengeController 추가 |
| | | - MealTime, DateRange Enum 한글 변환 지원 |
| | | - CalorieCalculator 유틸리티 추가 |
| | | - DTO 구조를 목업과 일치하도록 수정 (macro 중첩) |
