package com.lukehemmin.ai_diet_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FoodAnalysisActivity extends BaseActivity {

    private static final int ANALYSIS_DELAY = 2000; // 2초

    private ViewFlipper viewFlipper;
    private Button btnRetake;
    private Button btnAddToMeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_analysis);

        // WindowInsets 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();

        // 2초 후 자동으로 결과 화면으로 전환
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showResultScreen();
        }, ANALYSIS_DELAY);
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        btnRetake = findViewById(R.id.btnRetake);
        btnAddToMeal = findViewById(R.id.btnAddToMeal);
    }

    private void setupListeners() {
        // 뒤로가기 버튼
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 다시 촬영 버튼
        btnRetake.setOnClickListener(v -> finish());

        // 식단에 추가 버튼
        btnAddToMeal.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    private void showResultScreen() {
        // 페이드 인 애니메이션으로 결과 화면 표시
        viewFlipper.setInAnimation(this, android.R.anim.fade_in);
        viewFlipper.setOutAnimation(this, android.R.anim.fade_out);
        viewFlipper.showNext();
    }
}
