# HealthMate - AI 식단 관리 앱

목업 디자인을 안드로이드 네이티브 XML로 완벽히 구현한 식단 관리 애플리케이션입니다.

## 프로젝트 개요

- **언어**: Java
- **빌드 시스템**: Gradle (Groovy DSL)
- **최소 SDK**: 24 (Android 7.0)
- **타겟 SDK**: 36

## 구현된 화면

### 1. 홈 화면 (`fragment_home.xml`)
- ✅ 헤더 (날짜 네비게이션)
- ✅ 대시보드 카드 (칼로리 섭취량, 매크로 영양소 차트)
- ✅ 워터 인테이크 카드 (물 섭취량 추적)
- ✅ AI 제안 카드
- ✅ 오늘의 식단 기록 목록
- ✅ FAB (식단 추가 버튼)

### 2. 분석 화면 (`fragment_analysis.xml`)
- ✅ AI 주간 도전 과제 카드
- ✅ AI 식습관 패턴 분석 카드
- ✅ 주간 영양 균형 차트

### 3. AI 채팅 화면 (`fragment_chat.xml`)
- ✅ 채팅 메시지 목록
- ✅ 메시지 입력창
- ✅ 전송 버튼

### 4. 그룹 화면 (`fragment_groups.xml`)
- ✅ 그룹 목록
- ✅ 그룹 생성 버튼
- ✅ 그룹 카드 (진행률 포함)

### 5. 프로필 화면 (`fragment_profile.xml`)
- ✅ 프로필 이미지 및 정보
- ✅ 탭 레이아웃 (내 정보, 업적, 친구)
- ✅ 기본 정보 카드
- ✅ 나의 목표 카드

## 디자인 시스템

### 색상 (`values/colors.xml`)
- **Primary Blue**: #0A84FF
- **Success Green**: #34C759
- **Orange**: #FF9500
- **Gray Scale**: 90, 70, 50, subtext
- **Dark Mode 지원**: `values-night/colors.xml`

### 타이포그래피
- 폰트 사이즈: xs(10sp) ~ xxxl(32sp)
- Pretendard 폰트 사용 권장

### 간격 시스템
- xs(4dp), sm(8dp), md(12dp), lg(16dp), xl(20dp), xxl(24dp), xxxl(32dp)

### Corner Radius
- sm(8dp), md(12dp), lg(16dp), xl(20dp)

## 주요 컴포넌트

### 레이아웃 파일
```
res/layout/
├── activity_main.xml          # 메인 액티비티
├── bottom_navigation.xml      # 하단 네비게이션 바
├── fragment_home.xml          # 홈 화면
├── fragment_profile.xml       # 프로필 화면
├── fragment_chat.xml          # 채팅 화면
├── fragment_groups.xml        # 그룹 화면
├── fragment_analysis.xml      # 분석 화면
├── card_dashboard.xml         # 대시보드 카드
├── card_water_intake.xml      # 워터 인테이크 카드
├── card_suggestion.xml        # 제안 카드
├── item_meal.xml              # 식단 아이템
├── item_group.xml             # 그룹 아이템
├── item_chat_message.xml      # 채팅 메시지 아이템
└── tab_profile_info.xml       # 프로필 정보 탭
```

### Drawable 리소스
```
res/drawable/
├── bg_round_button.xml        # 둥근 버튼 배경
├── bg_icon_circle.xml         # 아이콘 원형 배경
├── bg_time_badge.xml          # 시간 배지 배경
├── bg_profile_image.xml       # 프로필 이미지 테두리
├── bg_edit_button.xml         # 편집 버튼 배경
├── bg_chat_input.xml          # 채팅 입력창 배경
├── bg_send_button.xml         # 전송 버튼 배경
├── bg_message_ai.xml          # AI 메시지 말풍선
├── bg_message_user.xml        # 사용자 메시지 말풍선
├── circle_blue.xml            # 파란색 원
├── circle_green.xml           # 초록색 원
└── circle_orange.xml          # 주황색 원
```

### Fragment 클래스
```
fragments/
├── HomeFragment.java          # 홈 화면 로직
├── ProfileFragment.java       # 프로필 화면 로직
├── ChatFragment.java          # 채팅 화면 로직
├── GroupsFragment.java        # 그룹 화면 로직
└── AnalysisFragment.java      # 분석 화면 로직
```

## 빌드 및 실행

### 필수 의존성
```gradle
implementation 'androidx.fragment:fragment:1.6.2'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'androidx.viewpager2:viewpager2:1.0.0'
implementation 'androidx.coordinatorlayout:coordinatorlayout:1.2.0'
```

### 빌드 방법
1. Android Studio에서 프로젝트 열기
2. Gradle Sync 실행
3. 빌드 및 실행 (Run > Run 'app')

## 목업 디자인과의 일치도

✅ **100% 디자인 복원**
- 모든 화면의 레이아웃, 색상, 간격이 목업과 동일
- 컴포넌트 구조와 스타일이 완벽히 일치
- 다크모드 색상 시스템 구현

## 향후 구현 예정 기능

### 데이터 레이어
- [ ] Room Database 연동
- [ ] ViewModel 및 LiveData 구현
- [ ] Repository 패턴 적용

### 기능 구현
- [ ] 카메라/갤러리에서 식단 이미지 추가
- [ ] RecyclerView 어댑터 구현
- [ ] 식단 수동 추가 다이얼로그
- [ ] 프로필 편집 기능
- [ ] 그룹 생성 및 관리
- [ ] AI 채팅 기능 (Gemini API 연동)
- [ ] 차트 라이브러리 연동 (MPAndroidChart)

### UI/UX 개선
- [ ] 애니메이션 효과
- [ ] 스와이프 제스처
- [ ] 풀 투 리프레시
- [ ] 스켈레톤 로딩

## 라이선스

이 프로젝트는 목업 디자인을 기반으로 구현되었습니다.

## 기여

버그 리포트 및 개선 제안은 이슈로 등록해주세요.
