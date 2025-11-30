package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleLogin;
    private TextView tvFindAccount, tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvFindAccount = findViewById(R.id.tvFindAccount);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    private void setupListeners() {
        // Text Change Listeners for Validation
        etEmail.addTextChangedListener(new ValidationTextWatcher(etEmail));
        etPassword.addTextChangedListener(new ValidationTextWatcher(etPassword));

        // Login Button
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Google Login
        btnGoogleLogin.setOnClickListener(v -> {
            Toast.makeText(this, "구글 로그인은 추후 지원 예정입니다.", Toast.LENGTH_SHORT).show();
        });

        // Find Account
        tvFindAccount.setOnClickListener(v -> {
            Toast.makeText(this, "아이디/비밀번호 찾기 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show();
        });

        // Sign Up
        tvSignUp.setOnClickListener(v -> {
            Toast.makeText(this, "회원가입 기능은 준비 중입니다.", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        if (!validateEmail() | !validatePassword()) {
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Show loading (Optional: add ProgressBar)
        btnLogin.setEnabled(false);

        com.lukehemmin.ai_diet_app.network.ApiService apiService = 
                com.lukehemmin.ai_diet_app.network.RetrofitClient.getApiService();
        
        com.lukehemmin.ai_diet_app.data.model.LoginRequest request = 
                new com.lukehemmin.ai_diet_app.data.model.LoginRequest(email, password);

        apiService.login(request).enqueue(new retrofit2.Callback<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.AuthResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.AuthResponse>> call, 
                                   retrofit2.Response<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.AuthResponse>> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String token = response.body().getData().getAccessToken();
                    
                    // Save token
                    android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("is_logged_in", true);
                    editor.putString("auth_token", token);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "로그인 성공!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "로그인 실패: 이메일 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.AuthResponse>> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("LoginActivity", "Login error", t);
            }
        });
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            inputLayoutEmail.setError("이메일을 입력해주세요.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayoutEmail.setError("올바른 이메일 형식이 아닙니다.");
            return false;
        } else {
            inputLayoutEmail.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            inputLayoutPassword.setError("비밀번호를 입력해주세요.");
            return false;
        } else if (password.length() < 6) {
            inputLayoutPassword.setError("비밀번호는 6자 이상이어야 합니다.");
            return false;
        } else {
            inputLayoutPassword.setError(null);
            return true;
        }
    }

    private class ValidationTextWatcher implements TextWatcher {
        private View view;

        private ValidationTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            int id = view.getId();
            if (id == R.id.etEmail) {
                validateEmail();
            } else if (id == R.id.etPassword) {
                validatePassword();
            }
        }
    }
}
