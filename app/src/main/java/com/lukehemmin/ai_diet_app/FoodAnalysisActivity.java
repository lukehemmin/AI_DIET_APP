package com.lukehemmin.ai_diet_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FoodAnalysisActivity extends AppCompatActivity {

    private static final int ANALYSIS_DELAY = 1000; // 1초

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_analysis);

        // WindowInsets 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 뒤로가기 버튼
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 1초 후 자동으로 분석 완료 및 화면 종료
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 분석 완료 후 결과 전달
            setResult(RESULT_OK);
            finish();
        }, ANALYSIS_DELAY);
    }
}
