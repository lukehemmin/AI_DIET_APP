# Supabase 설정 가이드

## 🚀 Supabase란?

Supabase는 Firebase의 오픈소스 대안으로, PostgreSQL 데이터베이스를 클라우드에서 호스팅하고 추가 기능을 제공합니다.

### 제공 기능
- ✅ **PostgreSQL Database**: 완전 관리형 PostgreSQL
- ✅ **Storage**: 이미지/파일 저장소 (S3 대체)
- ✅ **Authentication**: 사용자 인증 (선택적)
- ✅ **Realtime**: WebSocket 실시간 데이터 (선택적)
- ✅ **무료 플랜**: 500MB DB + 1GB Storage

---

## 📝 Step 1: 프로젝트 생성

### 1.1 회원가입
1. https://supabase.com 접속
2. **Start your project** 클릭
3. GitHub 또는 이메일로 회원가입

### 1.2 Organization 생성
1. Dashboard → **New organization** 클릭
2. Organization name 입력 (예: `healthmate-org`)
3. **Create organization** 클릭

### 1.3 프로젝트 생성
1. **New project** 클릭
2. 다음 정보 입력:
   ```
   Name: healthmate
   Database Password: (강력한 비밀번호 생성 - 저장 필수!)
   Region: Northeast Asia (Seoul)
   Pricing Plan: Free
   ```
3. **Create new project** 클릭
4. 프로젝트 생성 대기 (약 2분)

---

## 🔌 Step 2: 데이터베이스 연결

### 2.1 연결 정보 확인

프로젝트 대시보드에서:

1. **Settings** (왼쪽 하단 톱니바퀴) 클릭
2. **Database** 클릭
3. **Connection string** 섹션에서 확인:

```
Connection pooling (권장):
postgresql://postgres.abcdefghijklmnop:[YOUR-PASSWORD]@aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres
```

**중요 정보**:
- Host: `aws-0-ap-northeast-2.pooler.supabase.com`
- Port: `6543` (Connection pooling 사용시)
- Database: `postgres`
- User: `postgres.abcdefghijklmnop` (프로젝트 Reference)
- Password: 프로젝트 생성시 입력한 비밀번호

### 2.2 Spring Boot 설정

`application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:6543/postgres?pgbouncer=true
    username: postgres.abcdefghijklmnop
    password: your-password-here
    driver-class-name: org.postgresql.Driver
    
    hikari:
      maximum-pool-size: 5
      connection-timeout: 20000
  
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 2.3 연결 테스트

Spring Boot 애플리케이션 실행:
```bash
./gradlew bootRun
```

로그에서 확인:
```
Hikari Connection Pool ... - Start completed.
```

---

## 📦 Step 3: Supabase Storage 설정 (이미지 저장)

### 3.1 Bucket 생성

1. Supabase Dashboard → **Storage** 클릭
2. **Create a new bucket** 클릭
3. 다음 정보 입력:
   ```
   Name: meal-images
   Public bucket: ✅ 체크 (공개 읽기 허용)
   ```
4. **Create bucket** 클릭

### 3.2 정책 설정 (Public 읽기 허용)

1. `meal-images` 버킷 클릭
2. **Policies** 탭 클릭
3. **New policy** 클릭
4. 템플릿에서 선택:
   - **Allow public read access**: `SELECT` 허용
5. **Review** → **Save policy**

### 3.3 업로드 정책 (인증된 사용자만)

1. **New policy** 클릭
2. **Create policy from scratch**
3. 다음 설정:
   ```
   Policy name: Allow authenticated uploads
   Allowed operation: INSERT
   Target roles: authenticated
   ```
4. **Save policy**

### 3.4 API Keys 확인

**Settings** → **API**:
- **Project URL**: `https://abcdefghijklmnop.supabase.co`
- **anon public key**: 클라이언트용 (읽기 전용)
- **service_role key**: 서버용 (모든 권한) ⚠️ 절대 노출 금지!

### 3.5 Spring Boot 설정

`application.yml`에 추가:
```yaml
supabase:
  url: https://abcdefghijklmnop.supabase.co
  key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...  # service_role key
  bucket: meal-images
```

---

## 🧪 Step 4: 테스트

### 4.1 데이터베이스 테스트

Supabase **SQL Editor**에서:

```sql
-- 테이블 생성 테스트
CREATE TABLE test (
  id SERIAL PRIMARY KEY,
  name TEXT
);

-- 데이터 삽입
INSERT INTO test (name) VALUES ('Hello Supabase');

-- 조회
SELECT * FROM test;

-- 삭제
DROP TABLE test;
```

### 4.2 Storage 테스트

**Storage** → `meal-images` → **Upload file**로 이미지 업로드 후:

Public URL 확인:
```
https://abcdefghijklmnop.supabase.co/storage/v1/object/public/meal-images/test.jpg
```

브라우저에서 URL 접속하여 이미지 확인

---

## 📊 Step 5: 모니터링

### 5.1 Dashboard 확인

**Home** 화면에서 확인 가능:
- **Database Size**: 현재 DB 용량
- **Storage Size**: 현재 파일 저장 용량
- **API Requests**: API 호출 수

### 5.2 무료 플랜 제한

```
Database:
- 500MB 스토리지
- 무제한 API 요청
- 2개 동시 연결

Storage:
- 1GB 파일 저장
- 2GB 대역폭/월

Row Level Security:
- 무제한
```

### 5.3 Pro 플랜 ($25/월)

```
Database:
- 8GB 스토리지 (확장 가능)
- 무제한 API 요청
- 60개 동시 연결
- 자동 백업 7일

Storage:
- 100GB 파일 저장
- 200GB 대역폭/월
```

---

## 🔒 보안 주의사항

### 1. API Keys 관리

❌ **절대 하지 말 것**:
```java
// 코드에 직접 하드코딩 금지!
String apiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
```

✅ **올바른 방법**:
```java
@Value("${supabase.key}")
private String apiKey;
```

### 2. Git 커밋 주의

`.gitignore`에 추가:
```
application-dev.yml
application-prod.yml
.env
```

### 3. 환경 변수 사용

프로덕션 환경:
```bash
export SUPABASE_DB_URL=jdbc:postgresql://...
export SUPABASE_DB_PASSWORD=...
export SUPABASE_SERVICE_KEY=...
```

---

## 🛠️ 문제 해결

### 연결 실패

**에러**: `Connection refused`

**해결**:
1. 방화벽 확인 (Port 6543 허용)
2. Connection string 재확인
3. 비밀번호 특수문자 URL 인코딩

### 느린 응답

**원인**: Direct connection 사용

**해결**: Connection pooling 사용
```
:6543/postgres?pgbouncer=true
```

### Storage 업로드 실패

**원인**: 정책 설정 오류

**해결**:
1. Bucket이 Public인지 확인
2. INSERT 정책 추가
3. service_role key 사용 확인

---

## 📚 추가 자료

- **Supabase 공식 문서**: https://supabase.com/docs
- **Storage API**: https://supabase.com/docs/guides/storage
- **Connection Pooling**: https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooler
- **RLS (Row Level Security)**: https://supabase.com/docs/guides/auth/row-level-security

---

## ✅ 체크리스트

### 기본 설정
- [ ] Supabase 프로젝트 생성
- [ ] Database 연결 문자열 복사
- [ ] Spring Boot application.yml 설정
- [ ] 연결 테스트 성공

### Storage 설정 (선택)
- [ ] `meal-images` 버킷 생성
- [ ] Public 읽기 정책 설정
- [ ] 인증된 업로드 정책 설정
- [ ] API Keys 저장
- [ ] 이미지 업로드 테스트

### 보안
- [ ] API Keys 환경 변수로 관리
- [ ] .gitignore 설정
- [ ] 비밀번호 안전하게 저장

---

**작성일**: 2024-11-26  
**버전**: 1.0
