package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class AnalysisActivity extends BaseActivity {

    private TextView tvPeriod7, tvPeriod30, tvPeriod90;
    private View indicator7, indicator30, indicator90;
    private CalorieTrendChartView calorieTrendChart;
    private int selectedPeriod = 30; // 기본값: 30일

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // WindowInsets 설정 - 시스템 바 영역만큼 패딩 추가
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.analysisRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        setupBottomNavigation();
        updatePeriodSelection(30);
    }

    private void initViews() {
        // 기간 선택 탭
        tvPeriod7 = findViewById(R.id.tvPeriod7);
        tvPeriod30 = findViewById(R.id.tvPeriod30);
        tvPeriod90 = findViewById(R.id.tvPeriod90);
        indicator7 = findViewById(R.id.indicator7);
        indicator30 = findViewById(R.id.indicator30);
        indicator90 = findViewById(R.id.indicator90);
        
        // 차트 뷰
        calorieTrendChart = findViewById(R.id.calorieTrendChart);
    }

    private void setupListeners() {
        // 뒤로가기 버튼
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 기간 선택 탭
        CardView period7Card = findViewById(R.id.period7Card);
        CardView period30Card = findViewById(R.id.period30Card);
        CardView period90Card = findViewById(R.id.period90Card);

        period7Card.setOnClickListener(v -> updatePeriodSelection(7));
        period30Card.setOnClickListener(v -> updatePeriodSelection(30));
        period90Card.setOnClickListener(v -> updatePeriodSelection(90));
    }

    private void updatePeriodSelection(int period) {
        selectedPeriod = period;

        // 모든 텍스트와 인디케이터 초기화
        tvPeriod7.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvPeriod30.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvPeriod90.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        indicator7.setVisibility(View.INVISIBLE);
        indicator30.setVisibility(View.INVISIBLE);
        indicator90.setVisibility(View.INVISIBLE);

        // 선택된 항목 강조
        if (period == 7) {
            tvPeriod7.setTextColor(ContextCompat.getColor(this, R.color.white));
            indicator7.setVisibility(View.VISIBLE);
        } else if (period == 30) {
            tvPeriod30.setTextColor(ContextCompat.getColor(this, R.color.white));
            indicator30.setVisibility(View.VISIBLE);
        } else if (period == 90) {
            tvPeriod90.setTextColor(ContextCompat.getColor(this, R.color.white));
            indicator90.setVisibility(View.VISIBLE);
        }

        // 여기서 데이터 다시 로드
        loadDataForPeriod(period);
    }

    private void loadDataForPeriod(int period) {
        // TODO: 실제 데이터 로드 구현
        // 현재는 테스트 데이터 사용
        
        // 테스트 칼로리 데이터 (10일치)
        List<Float> calorieData = new ArrayList<>();
        if (period == 7) {
            // 7일 데이터
            calorieData.add(1800f);
            calorieData.add(1850f);
            calorieData.add(1900f);
            calorieData.add(2000f);
            calorieData.add(1780f);
            calorieData.add(1850f);
            calorieData.add(1900f);
        } else if (period == 30) {
            // 30일 데이터 (최근 10일만 표시)
            calorieData.add(1800f);
            calorieData.add(1850f);
            calorieData.add(1900f);
            calorieData.add(2000f);
            calorieData.add(1780f);
            calorieData.add(1850f);
            calorieData.add(1900f);
            calorieData.add(1950f);
            calorieData.add(1800f);
            calorieData.add(1900f);
        } else {
            // 90일 데이터 (최근 10일만 표시)
            calorieData.add(1750f);
            calorieData.add(1820f);
            calorieData.add(1880f);
            calorieData.add(1950f);
            calorieData.add(1800f);
            calorieData.add(1900f);
            calorieData.add(1850f);
            calorieData.add(1920f);
            calorieData.add(1780f);
            calorieData.add(1860f);
        }
        
        // 차트 업데이트
        if (calorieTrendChart != null) {
            calorieTrendChart.setChartData(calorieData, 1800f);
        }
    }

    private void setupBottomNavigation() {
        // 홈
        LinearLayout navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(v -> {
            finish(); // MainActivity로 돌아가기
        });

        // 챌린지
        LinearLayout navChallenge = findViewById(R.id.navChallenge);
        navChallenge.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChallengeActivity.class);
            startActivity(intent);
        });

        // 분석 (현재 화면)
        LinearLayout navAnalysis = findViewById(R.id.navAnalysis);
        // 현재 화면이므로 클릭 불가

        // 프로필
        LinearLayout navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(v -> {
            // TODO: 프로필 화면으로 이동
        });
    }
}
