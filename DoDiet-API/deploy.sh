#!/bin/bash
# DoDiet-API Quick Deploy Script

set -e

echo "🚀 DoDiet-API 배포 시작..."

# Check if .env exists
if [ ! -f .env ]; then
    echo "⚠️  .env 파일이 없습니다. .env.example을 복사합니다..."
    cp .env.example .env
    echo "📝 .env 파일을 수정한 후 다시 실행하세요!"
    exit 1
fi

# Check if firebase config exists
if [ ! -f firebase-service-account.json ]; then
    echo "⚠️  firebase-service-account.json 파일이 필요합니다."
    echo "   Firebase Console에서 다운로드 후 이 폴더에 넣어주세요."
fi

# Build and run
echo "🔨 Docker 이미지 빌드 중..."
docker compose build

echo "🏃 컨테이너 시작 중..."
docker compose up -d

echo ""
echo "✅ 배포 완료!"
echo "📍 API 주소: http://localhost:8080"
echo "📍 Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "📋 로그 확인: docker compose logs -f app"
echo "🛑 중지: docker compose down"
