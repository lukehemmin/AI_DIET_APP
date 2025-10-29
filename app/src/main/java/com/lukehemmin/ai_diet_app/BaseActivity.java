package com.lukehemmin.ai_diet_app;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 모든 Activity의 베이스 클래스
 * EdgeToEdge를 공통으로 적용하여 일관된 시스템 바(상태바, 네비게이션바)를 제공합니다.
 * 
 * EdgeToEdge 효과:
 * - 시스템 바가 투명해지고 콘텐츠가 전체 화면으로 확장됩니다
 * - 모든 Activity에서 동일한 시스템 바 색상과 스타일을 가집니다
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // EdgeToEdge 활성화 - 모든 Activity에서 일관된 시스템 바 제공
        EdgeToEdge.enable(this);
    }
}
