package com.lukehemmin.ai_diet_app;

import android.app.Application;

import com.lukehemmin.ai_diet_app.utils.AuthManager;

public class DoDietApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // AuthManager 초기화 (401 에러 시 로그인 화면으로 이동하기 위해)
        AuthManager.init(this);
    }
}
