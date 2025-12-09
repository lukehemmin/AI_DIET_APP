package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleLogin;
    private TextView tvFindAccount, tvSignUp;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 자동 로그인 체크
        if (checkAutoLogin()) {
            return; // 자동 로그인 성공 시 여기서 종료
        }
        
        setContentView(R.layout.activity_login);

        initViews();
        setupGoogleSignIn();
        setupListeners();
    }
    
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            
            if (idToken != null) {
                sendGoogleTokenToBackend(idToken);
            } else {
                Toast.makeText(this, "Google Sign-In failed: No ID Token", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            android.util.Log.w("LoginActivity", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendGoogleTokenToBackend(String idToken) {
        // Show loading (Optional: add ProgressBar)
        btnLogin.setEnabled(false);
        btnGoogleLogin.setEnabled(false);
        
        com.lukehemmin.ai_diet_app.network.ApiService apiService = 
                com.lukehemmin.ai_diet_app.network.RetrofitClient.getApiService();
        
        com.lukehemmin.ai_diet_app.data.model.GoogleLoginRequest request = 
                new com.lukehemmin.ai_diet_app.data.model.GoogleLoginRequest(idToken);

        apiService.googleLogin(request).enqueue(new retrofit2.Callback<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.AuthResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.AuthResponse>> call, 
                                   retrofit2.Response<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.AuthResponse>> response) {
                btnLogin.setEnabled(true);
                btnGoogleLogin.setEnabled(true);
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
                    Toast.makeText(LoginActivity.this, "로그인 실패: " + (response.body() != null ? response.body().getMessage() : "서버 오류"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.AuthResponse>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnGoogleLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                android.util.Log.e("LoginActivity", "Login error", t);
            }
        });
    }
    
    /**
     * 저장된 토큰이 있으면 서버에 검증 후 자동 로그인
     * @return true if attempting auto login (async verification)
     */
    private boolean checkAutoLogin() {
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        String token = prefs.getString("auth_token", null);
        
        if (isLoggedIn && token != null && !token.isEmpty()) {
            // 서버에 토큰 유효성 검증
            verifyTokenAndProceed();
            return true;
        }
        return false;
    }
    
    /**
     * 서버에 토큰 검증 요청
     */
    private void verifyTokenAndProceed() {
        // 로딩 화면 표시 (간단히 처리)
        setContentView(R.layout.activity_splash_loading);
        
        com.lukehemmin.ai_diet_app.network.ApiService apiService = 
                com.lukehemmin.ai_diet_app.network.RetrofitClient.getClient(this).create(
                        com.lukehemmin.ai_diet_app.network.ApiService.class);
        
        // 프로필 API로 토큰 유효성 검증
        apiService.getProfile().enqueue(new retrofit2.Callback<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.UserProfile>>() {
            @Override
            public void onResponse(retrofit2.Call<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.UserProfile>> call, 
                                   retrofit2.Response<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // 토큰 유효 - 홈으로 이동
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (response.code() == 401 || response.code() == 403) {
                    // 토큰 만료 또는 인증 실패 - 로그인 화면 표시
                    clearTokenAndShowLogin("세션이 만료되었습니다. 다시 로그인해주세요.");
                } else {
                    // 기타 오류 - 로그인 화면 표시 (안전하게 처리)
                    clearTokenAndShowLogin("인증에 실패했습니다. 다시 로그인해주세요.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.lukehemmin.ai_diet_app.data.model.ApiResponse<com.lukehemmin.ai_diet_app.data.model.UserProfile>> call, Throwable t) {
                android.util.Log.e("LoginActivity", "Token verification failed", t);
                // 네트워크 오류 - 서버 연결 불가
                clearTokenAndShowLogin("서버에 연결할 수 없습니다. 다시 로그인해주세요.");
            }
        });
    }
    
    /**
     * 토큰 삭제 및 로그인 화면 재시작
     */
    private void clearTokenAndShowLogin(String message) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("auth_token")
            .apply();
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // Activity 재시작 (registerForActivityResult는 onCreate에서만 가능)
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Find Account
        tvFindAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FindAccountActivity.class);
            startActivity(intent);
        });

        // Sign Up
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
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
