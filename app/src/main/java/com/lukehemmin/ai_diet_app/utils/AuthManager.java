package com.lukehemmin.ai_diet_app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.lukehemmin.ai_diet_app.LoginActivity;

/**
 * 인증 관리 유틸리티 클래스
 * - 토큰 저장/삭제
 * - 로그아웃 처리
 * - 401 에러 시 로그인 화면 이동
 */
public class AuthManager {
    
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    
    private static Context applicationContext;
    
    /**
     * Application에서 초기화 호출
     */
    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }
    
    /**
     * 토큰 저장
     */
    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_AUTH_TOKEN, token)
            .apply();
    }
    
    /**
     * 토큰 가져오기
     */
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }
    
    /**
     * 로그인 상태 확인
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        String token = prefs.getString(KEY_AUTH_TOKEN, null);
        return isLoggedIn && token != null && !token.isEmpty();
    }
    
    /**
     * 로그아웃 - 토큰 삭제
     */
    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_AUTH_TOKEN)
            .apply();
    }
    
    /**
     * 401 에러 등으로 인한 강제 로그아웃 및 로그인 화면 이동
     */
    public static void forceLogout(String message) {
        if (applicationContext == null) return;
        
        // 토큰 삭제
        clearToken(applicationContext);
        
        // 메인 스레드에서 실행
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show();
            
            // 로그인 화면으로 이동
            Intent intent = new Intent(applicationContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            applicationContext.startActivity(intent);
        });
    }
    
    /**
     * Context를 사용한 강제 로그아웃
     */
    public static void forceLogout(Context context, String message) {
        // 토큰 삭제
        clearToken(context);
        
        // 메인 스레드에서 실행
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            
            // 로그인 화면으로 이동
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}
