package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lukehemmin.ai_diet_app.data.model.ActivityLevel;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.AuthResponse;
import com.lukehemmin.ai_diet_app.data.model.EmailVerificationRequest;
import com.lukehemmin.ai_diet_app.data.model.SignupRequest;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private View layoutStep1, layoutCodeVerification, layoutStep2, layoutStep3;
    private EditText etEmail, etCode, etName, etVerifiedEmail, etPassword, etConfirmPassword, etAge, etHeight, etWeight;
    private Button btnSendCode, btnVerifyCode, btnNextToStep3, btnSignup;
    private RadioGroup rgGender;
    private Spinner spinnerActivityLevel;
    private TextView tvResendGuide;

    private String verifiedEmail;
    private CountDownTimer verificationTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        setupActivityLevelSpinner();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (verificationTimer != null) {
            verificationTimer.cancel();
        }
    }

    private void initViews() {
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutCodeVerification = findViewById(R.id.layoutCodeVerification);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);

        etEmail = findViewById(R.id.etEmail);
        etCode = findViewById(R.id.etCode);
        etName = findViewById(R.id.etName);
        etVerifiedEmail = findViewById(R.id.etVerifiedEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);

        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        btnNextToStep3 = findViewById(R.id.btnNextToStep3);
        btnSignup = findViewById(R.id.btnSignup);
        
        tvResendGuide = findViewById(R.id.tvResendGuide);

        rgGender = findViewById(R.id.rgGender);
        spinnerActivityLevel = findViewById(R.id.spinnerActivityLevel);
    }

    private void setupActivityLevelSpinner() {
        ArrayAdapter<ActivityLevel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ActivityLevel.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityLevel.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnVerifyCode.setOnClickListener(v -> verifyCode());
        btnNextToStep3.setOnClickListener(v -> validateAndMoveToStep3());
        btnSignup.setOnClickListener(v -> performSignup());
    }

    private void sendVerificationCode() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 버튼 비활성화 및 텍스트 변경
        btnSendCode.setEnabled(false);
        btnSendCode.setText("전송 중...");
        tvResendGuide.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.sendVerificationCode(new EmailVerificationRequest(email, null)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SignupActivity.this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                    layoutCodeVerification.setVisibility(View.VISIBLE);
                    etEmail.setEnabled(false);
                    
                    // 30초 쿨다운 시작
                    startVerificationCooldown();
                } else {
                    Toast.makeText(SignupActivity.this, "전송 실패: " + (response.body() != null ? response.body().getMessage() : "오류"), Toast.LENGTH_SHORT).show();
                    resetSendButton();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetSendButton();
            }
        });
    }

    private void startVerificationCooldown() {
        if (verificationTimer != null) {
            verificationTimer.cancel();
        }

        // 30초 카운트다운
        verificationTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnSendCode.setText(String.format("재전송 (%d초)", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("인증번호 재전송");

                // 아직 인증 완료되지 않았다면 안내 문구 표시
                if (layoutStep2.getVisibility() != View.VISIBLE) {
                    tvResendGuide.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    private void resetSendButton() {
        btnSendCode.setEnabled(true);
        btnSendCode.setText("인증번호 전송");
    }

    private void verifyCode() {
        String email = etEmail.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        apiService.verifyCode(new EmailVerificationRequest(email, code)).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData()) {
                    Toast.makeText(SignupActivity.this, "인증되었습니다.", Toast.LENGTH_SHORT).show();
                    
                    // 타이머 종료
                    if (verificationTimer != null) {
                        verificationTimer.cancel();
                    }

                    verifiedEmail = email;
                    etVerifiedEmail.setText(verifiedEmail);
                    layoutStep1.setVisibility(View.GONE);
                    layoutStep2.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(SignupActivity.this, "인증 실패: 코드를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndMoveToStep3() {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        layoutStep2.setVisibility(View.GONE);
        layoutStep3.setVisibility(View.VISIBLE);
    }

    private void performSignup() {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        String gender = ((RadioButton) findViewById(rgGender.getCheckedRadioButtonId())).getText().equals("남성") ? "MALE" : "FEMALE";
        Integer age = Integer.parseInt(etAge.getText().toString());
        Double height = Double.parseDouble(etHeight.getText().toString());
        Double weight = Double.parseDouble(etWeight.getText().toString());
        String activityLevel = ((ActivityLevel) spinnerActivityLevel.getSelectedItem()).getServerValue();

        SignupRequest.ProfileRequest profile = new SignupRequest.ProfileRequest(gender, age, height, weight, activityLevel);
        SignupRequest request = new SignupRequest(verifiedEmail, password, name, profile);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.signup(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SignupActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                    
                    // Go to Login or Main Activity? Login is better to verify flow, or auto-login.
                    // Backend does auto-login logic (returns token), so we can go to Main.
                    // But for clarity, let's go to Login screen and ask to login, or just auto login.
                    // Let's auto login as token is returned.
                    
                    String token = response.body().getData().getAccessToken();
                    getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                            .putBoolean("is_logged_in", true)
                            .putString("auth_token", token)
                            .apply();

                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "회원가입 실패: " + (response.body() != null ? response.body().getMessage() : "오류"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
