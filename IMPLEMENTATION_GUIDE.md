# 구현 가이드

## 📱 완성된 화면 목록

### ✅ 구현 완료
1. **홈 화면** - 대시보드, 워터 인테이크, AI 제안, 식단 기록
2. **분석 화면** - AI 도전과제, 패턴 분석, 주간 리포트
3. **AI 채팅 화면** - 메시지 목록, 입력창
4. **그룹 화면** - 그룹 목록, 진행률 표시
5. **프로필 화면** - 프로필 정보, 탭 (내 정보/업적/친구)

### 🎨 디자인 시스템 구현

#### 색상 시스템
```xml
<!-- Light Mode -->
<color name="primary_blue">#0A84FF</color>
<color name="state_success">#34C759</color>
<color name="orange_500">#FF9500</color>
<color name="gray_90">#F7F8FA</color>
<color name="gray_100">#FFFFFF</color>

<!-- Dark Mode (values-night/) -->
<color name="gray_90">#0f172a</color>
<color name="gray_100">#1e293b</color>
```

#### 간격 시스템
```xml
<dimen name="spacing_xs">4dp</dimen>
<dimen name="spacing_sm">8dp</dimen>
<dimen name="spacing_md">12dp</dimen>
<dimen name="spacing_lg">16dp</dimen>
<dimen name="spacing_xl">20dp</dimen>
<dimen name="spacing_xxl">24dp</dimen>
<dimen name="spacing_xxxl">32dp</dimen>
```

## 📂 파일 구조

```
app/src/main/
├── java/com/lukehemmin/ai_diet_app/
│   ├── MainActivity.java               # 메인 액티비티
│   └── fragments/
│       ├── HomeFragment.java           # 홈 화면
│       ├── AnalysisFragment.java       # 분석 화면
│       ├── ChatFragment.java           # 채팅 화면
│       ├── GroupsFragment.java         # 그룹 화면
│       └── ProfileFragment.java        # 프로필 화면
│
├── res/
│   ├── layout/
│   │   ├── activity_main.xml          # 메인 레이아웃
│   │   ├── bottom_navigation.xml      # 하단 네비게이션
│   │   ├── fragment_home.xml          # 홈 화면 레이아웃
│   │   ├── fragment_analysis.xml      # 분석 화면 레이아웃
│   │   ├── fragment_chat.xml          # 채팅 화면 레이아웃
│   │   ├── fragment_groups.xml        # 그룹 화면 레이아웃
│   │   ├── fragment_profile.xml       # 프로필 화면 레이아웃
│   │   ├── card_dashboard.xml         # 대시보드 카드
│   │   ├── card_water_intake.xml      # 워터 인테이크 카드
│   │   ├── card_suggestion.xml        # 제안 카드
│   │   ├── item_meal.xml              # 식단 아이템
│   │   ├── item_group.xml             # 그룹 아이템
│   │   ├── item_chat_message.xml      # 채팅 메시지
│   │   ├── item_friend.xml            # 친구 아이템
│   │   ├── tab_profile_info.xml       # 프로필 정보 탭
│   │   ├── tab_profile_achievements.xml # 업적 탭
│   │   └── tab_profile_friends.xml    # 친구 탭
│   │
│   ├── drawable/
│   │   ├── bg_round_button.xml        # 둥근 버튼
│   │   ├── bg_icon_circle.xml         # 아이콘 원형
│   │   ├── bg_time_badge.xml          # 시간 배지
│   │   ├── bg_profile_image.xml       # 프로필 이미지
│   │   ├── bg_edit_button.xml         # 편집 버튼
│   │   ├── bg_chat_input.xml          # 채팅 입력창
│   │   ├── bg_send_button.xml         # 전송 버튼
│   │   ├── bg_message_ai.xml          # AI 메시지
│   │   ├── bg_message_user.xml        # 사용자 메시지
│   │   ├── bg_badge_item.xml          # 배지 아이템
│   │   ├── circle_blue.xml            # 파란색 원
│   │   ├── circle_green.xml           # 초록색 원
│   │   └── circle_orange.xml          # 주황색 원
│   │
│   ├── values/
│   │   ├── colors.xml                 # 색상 정의
│   │   ├── strings.xml                # 문자열 리소스
│   │   └── dimens.xml                 # 크기 정의
│   │
│   └── values-night/
│       └── colors.xml                 # 다크모드 색상
```

## 🔧 다음 구현 단계

### 1단계: 어댑터 구현
```java
// 식단 목록 어댑터
MealAdapter.java
GroupAdapter.java
ChatAdapter.java
FriendAdapter.java
```

### 2단계: 데이터 모델
```java
// 모델 클래스
models/
├── Meal.java
├── Group.java
├── Friend.java
├── ChatMessage.java
└── UserProfile.java
```

### 3단계: ViewModel 및 Repository
```java
// ViewModel
viewmodels/
├── HomeViewModel.java
├── ProfileViewModel.java
└── ChatViewModel.java

// Repository
repository/
├── MealRepository.java
└── UserRepository.java
```

### 4단계: Database
```java
// Room Database
database/
├── AppDatabase.java
├── dao/
│   ├── MealDao.java
│   └── UserDao.java
└── entities/
    ├── MealEntity.java
    └── UserEntity.java
```

## 🎯 주요 기능 구현 가이드

### 식단 추가 기능
1. FAB 클릭 → 갤러리/카메라 선택
2. 이미지 선택 → Gemini API로 분석
3. 결과 표시 → 확인 → RecyclerView에 추가

### 워터 인테이크
```java
// HomeFragment.java에서
private void updateWaterIntake(int delta) {
    int current = Integer.parseInt(txtWaterCount.getText().toString());
    int newValue = Math.max(0, Math.min(8, current + delta));
    txtWaterCount.setText(String.valueOf(newValue));
    updateWaterGlasses(newValue);
}
```

### 네비게이션
```java
// MainActivity.java에서
private void loadFragment(Fragment fragment) {
    getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
}
```

## 📊 차트 구현 (향후)

### MPAndroidChart 사용
```gradle
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
```

```java
// 도넛 차트 (대시보드)
PieChart pieChart = view.findViewById(R.id.pie_chart);
// 라인 차트 (주간 분석)
LineChart lineChart = view.findViewById(R.id.line_chart);
```

## 🌐 API 연동 (향후)

### Gemini API
```java
// 식단 이미지 분석
implementation 'com.google.ai.client.generativeai:generativeai:0.1.2'
```

### Retrofit
```java
// REST API 통신
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
```

## 📱 테스트 방법

1. **빌드**: `./gradlew build`
2. **실행**: Run > Run 'app'
3. **테스트**: 각 화면 네비게이션 확인
4. **UI 검증**: 목업 디자인과 비교

## ✨ 목업과의 차이점

- **완벽히 일치**: 색상, 간격, 레이아웃 구조
- **차이점**: 
  - 차트는 Placeholder로 표시 (라이브러리 연동 필요)
  - 이미지는 기본 아이콘 사용 (실제 데이터 로드 필요)
  - 동적 데이터는 하드코딩 (ViewModel 연동 필요)

## 🚀 빠른 시작

```bash
# 1. 프로젝트 열기
Android Studio > Open > AI_DIET_APP

# 2. Gradle Sync
File > Sync Project with Gradle Files

# 3. 빌드 및 실행
Run > Run 'app'
```

## 📝 체크리스트

### ✅ 완료된 작업
- [x] 모든 화면 레이아웃 XML 작성
- [x] Fragment 클래스 생성
- [x] MainActivity 네비게이션 구현
- [x] 색상 시스템 구현
- [x] Drawable 리소스 생성
- [x] 문자열 리소스 작성
- [x] 다크모드 색상 정의

### ⏳ 진행 예정
- [ ] RecyclerView 어댑터 구현
- [ ] ViewModel 구현
- [ ] Room Database 연동
- [ ] 차트 라이브러리 연동
- [ ] Gemini API 연동
- [ ] 이미지 로딩 (Glide/Coil)
- [ ] 애니메이션 추가
