package com.lukehemmin.ai_diet_app.utils;

/**
 * URL 관련 유틸리티 클래스
 */
public class UrlUtils {
    
    /**
     * HTTP URL을 HTTPS로 변환합니다.
     * Android 보안 정책으로 인해 cleartext HTTP 통신이 차단되므로
     * 모든 이미지 URL은 HTTPS를 사용해야 합니다.
     */
    public static String ensureHttps(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        if (url.startsWith("http://")) {
            return url.replace("http://", "https://");
        }
        return url;
    }
}
