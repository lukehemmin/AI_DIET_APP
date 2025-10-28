package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etId, etPassword;
    private Button btnLogin, btnGoogle;
    private TextView tvSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google);
        tvSignup = findViewById(R.id.tv_signup);
    }

    private void setupListeners() {
        // 로그인 버튼
        btnLogin.setOnClickListener(v -> handleLogin());

        // 구글 로그인 버튼
        btnGoogle.setOnClickListener(v -> handleGoogleLogin());

        // 회원가입 링크
        tvSignup.setOnClickListener(v -> handleSignup());
    }

    private void handleLogin() {
        String id = etId.getText() != null ? etId.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // 입력 유효성 검사
        if (id.isEmpty()) {
            Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: 실제 로그인 로직 구현
        // 임시로 바로 메인 화면으로 이동
        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    private void handleGoogleLogin() {
        // TODO: 구글 로그인 구현
        Toast.makeText(this, "구글 로그인 기능 준비 중", Toast.LENGTH_SHORT).show();
    }

    private void handleSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
