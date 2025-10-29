package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChallengeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge);

        // WindowInsets 설정 - 시스템 바 영역만큼 패딩 추가
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.challengeRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // 홈
        LinearLayout navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        // 챌린지 (현재 화면)
        LinearLayout navChallenge = findViewById(R.id.navChallenge);
        // 현재 화면이므로 클릭 불가

        // 분석
        LinearLayout navAnalysis = findViewById(R.id.navAnalysis);
        if (navAnalysis != null) {
            navAnalysis.setOnClickListener(v -> {
                Intent intent = new Intent(this, AnalysisActivity.class);
                startActivity(intent);
            });
        }

        // 프로필
        LinearLayout navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // TODO: 프로필 화면으로 이동
            });
        }
    }
}
