package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {

    // UI 요소
    private ImageButton btnBack, btnEdit;
    private TextView tvUserName, tvUserLevel, tvLevelPercent;
    private TextView tvConsecutiveDays, tvWeightChange, tvBadgeCount;
    private TextView tvWeeklyDays, tvAvgCalories, tvWaterIntake, tvGoalAchievement;
    private TextView btnViewAllBadges;
    private ProgressBar progressLevel;
    private RecyclerView profileMenuRecyclerView;
    private ProfileMenuAdapter menuAdapter;
    private LinearLayout navHome, navChallenge, navAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupListeners();
        loadProfileData();
    }

    private void initViews() {
        // 헤더 버튼
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);

        // 프로필 정보
        tvUserName = findViewById(R.id.tvUserName);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        tvLevelPercent = findViewById(R.id.tvLevelPercent);
        progressLevel = findViewById(R.id.progressLevel);

        // 통계 정보
        tvConsecutiveDays = findViewById(R.id.tvConsecutiveDays);
        tvWeightChange = findViewById(R.id.tvWeightChange);
        tvBadgeCount = findViewById(R.id.tvBadgeCount);

        // 주간 활동
        tvWeeklyDays = findViewById(R.id.tvWeeklyDays);
        tvAvgCalories = findViewById(R.id.tvAvgCalories);
        tvWaterIntake = findViewById(R.id.tvWaterIntake);
        tvGoalAchievement = findViewById(R.id.tvGoalAchievement);

        // 배지
        btnViewAllBadges = findViewById(R.id.btnViewAllBadges);

        // 메뉴 RecyclerView
        profileMenuRecyclerView = findViewById(R.id.profileMenuRecyclerView);
        profileMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupMenuItems();

        // 하단 네비게이션
        navHome = findViewById(R.id.navHome);
        navChallenge = findViewById(R.id.navChallenge);
        navAnalysis = findViewById(R.id.navAnalysis);
    }

    private void setupListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        // 편집 버튼
        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "프로필 편집 기능은 준비 중입니다", Toast.LENGTH_SHORT).show();
        });

        // 배지 모두 보기
        btnViewAllBadges.setOnClickListener(v -> {
            Toast.makeText(this, "배지 목록은 준비 중입니다", Toast.LENGTH_SHORT).show();
        });

        // 하단 네비게이션
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        navChallenge.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChallengeActivity.class);
            startActivity(intent);
            finish();
        });

        navAnalysis.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnalysisActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupMenuItems() {
        List<ProfileMenuItem> menuItems = new ArrayList<>();

        // 일반 메뉴 아이템
        menuItems.add(new ProfileMenuItem(
                "settings",
                R.drawable.ic_settings,
                getString(R.string.settings),
                getString(R.string.settings_desc)
        ));

        menuItems.add(new ProfileMenuItem(
                "notifications",
                R.drawable.ic_notifications,
                getString(R.string.notifications),
                getString(R.string.notifications_desc)
        ));

        menuItems.add(new ProfileMenuItem(
                "help",
                R.drawable.ic_help,
                getString(R.string.help),
                getString(R.string.help_desc)
        ));

        menuItems.add(new ProfileMenuItem(
                "health_data",
                R.drawable.ic_heart,
                getString(R.string.health_data),
                getString(R.string.health_data_desc)
        ));

        menuItems.add(new ProfileMenuItem(
                "privacy",
                R.drawable.ic_privacy,
                getString(R.string.privacy),
                getString(R.string.privacy_desc)
        ));

        // 목표 설정 아이템 (특별 타입)
        menuItems.add(new ProfileMenuItem(
                "set_goal",
                getString(R.string.set_goal),
                getString(R.string.set_goal_desc)
        ));

        // 어댑터 설정
        menuAdapter = new ProfileMenuAdapter(menuItems, menuId -> {
            handleMenuClick(menuId);
        });

        profileMenuRecyclerView.setAdapter(menuAdapter);
    }

    private void handleMenuClick(String menuId) {
        switch (menuId) {
            case "settings":
                Toast.makeText(this, "설정 화면은 준비 중입니다", Toast.LENGTH_SHORT).show();
                break;
            case "notifications":
                Toast.makeText(this, "알림 설정은 준비 중입니다", Toast.LENGTH_SHORT).show();
                break;
            case "help":
                Toast.makeText(this, "도움말은 준비 중입니다", Toast.LENGTH_SHORT).show();
                break;
            case "health_data":
                Toast.makeText(this, "건강 데이터 연동은 준비 중입니다", Toast.LENGTH_SHORT).show();
                break;
            case "privacy":
                Toast.makeText(this, "개인정보 보호 설정은 준비 중입니다", Toast.LENGTH_SHORT).show();
                break;
            case "set_goal":
                Toast.makeText(this, "목표 설정 기능은 준비 중입니다", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void loadProfileData() {
        // 테스트 데이터 설정
        tvUserName.setText("김다이어트");
        tvUserLevel.setText("Level 12 · 다이어트 마스터");
        tvLevelPercent.setText("75%");
        progressLevel.setProgress(75);

        tvConsecutiveDays.setText("42일");
        tvWeightChange.setText("-5.2kg");
        tvBadgeCount.setText("12개");

        tvWeeklyDays.setText("7일");
        tvAvgCalories.setText("1,925");
        tvWaterIntake.setText("87%");
        tvGoalAchievement.setText("6/7");
    }
}
