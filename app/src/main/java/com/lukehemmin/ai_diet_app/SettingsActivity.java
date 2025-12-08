package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout layoutLogout;
    private TextView txtAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        layoutLogout = findViewById(R.id.layout_logout);
        txtAppVersion = findViewById(R.id.txt_app_version);

        // 앱 버전 표시
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            txtAppVersion.setText("v" + versionName);
        } catch (Exception e) {
            txtAppVersion.setText("v1.0.0");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        layoutLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃 하시겠습니까?")
                .setPositiveButton("로그아웃", (dialog, which) -> performLogout())
                .setNegativeButton("취소", null)
                .show();
    }

    private void performLogout() {
        // SharedPreferences 초기화
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit()
                .remove("is_logged_in")
                .remove("auth_token")
                .apply();

        // 로그인 화면으로 이동
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
