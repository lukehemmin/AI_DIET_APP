package com.lukehemmin.ai_diet_app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.EmailVerificationRequest;
import com.lukehemmin.ai_diet_app.data.model.FindIdRequest;
import com.lukehemmin.ai_diet_app.data.model.PasswordResetConfirmRequest;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindAccountActivity extends AppCompatActivity {

    // Views
    private TabLayout tabLayout;
    private LinearLayout layoutFindId, layoutFindPw;

    // Find ID Views
    private TextInputEditText etName, etBirthDate;
    private MaterialButton btnFindId;
    private LinearLayout layoutIdResult;
    private TextView tvIdResult;

    // Find PW Views
    private TextInputEditText etEmail, etCode, etNewPw, etConfirmPw;
    private MaterialButton btnSendCode, btnVerifyCode, btnResetPw;
    private LinearLayout layoutVerification, layoutResetPw;
    private TextInputLayout inputLayoutCode, inputLayoutNewPw, inputLayoutConfirmPw;

    private ApiService apiService;
    private CountDownTimer verificationTimer;
    private String selectedBirthDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_account);

        apiService = RetrofitClient.getApiService();

        initViews();
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
        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tabLayout = findViewById(R.id.tabLayout);
        layoutFindId = findViewById(R.id.layoutFindId);
        layoutFindPw = findViewById(R.id.layoutFindPw);

        // Find ID
        etName = findViewById(R.id.etName);
        etBirthDate = findViewById(R.id.etBirthDate);
        btnFindId = findViewById(R.id.btnFindId);
        layoutIdResult = findViewById(R.id.layoutIdResult);
        tvIdResult = findViewById(R.id.tvIdResult);

        // Find PW
        etEmail = findViewById(R.id.etEmail);
        etCode = findViewById(R.id.etCode);
        etNewPw = findViewById(R.id.etNewPw);
        etConfirmPw = findViewById(R.id.etConfirmPw);
        
        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        btnResetPw = findViewById(R.id.btnResetPw);

        layoutVerification = findViewById(R.id.layoutVerification);
        layoutResetPw = findViewById(R.id.layoutResetPw);
        
        inputLayoutCode = findViewById(R.id.inputLayoutCode);
        inputLayoutNewPw = findViewById(R.id.inputLayoutNewPw);
        inputLayoutConfirmPw = findViewById(R.id.inputLayoutConfirmPw);
    }

    private void setupListeners() {
        // Tab Listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutFindId.setVisibility(View.VISIBLE);
                    layoutFindPw.setVisibility(View.GONE);
                } else {
                    layoutFindId.setVisibility(View.GONE);
                    layoutFindPw.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Find ID Logic
        etBirthDate.setOnClickListener(v -> showDatePicker());
        btnFindId.setOnClickListener(v -> findId());

        // Find PW Logic
        btnSendCode.setOnClickListener(v -> sendCode());
        btnVerifyCode.setOnClickListener(v -> verifyCode());
        btnResetPw.setOnClickListener(v -> resetPassword());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    // Format: yyyy-MM-dd
                    selectedBirthDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
                    etBirthDate.setText(selectedBirthDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void findId() {
        String name = etName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (birthDate.isEmpty()) {
            Toast.makeText(this, "생년월일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.findId(new FindIdRequest(name, birthDate)).enqueue(new Callback<ApiResponse<List<String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<String>>> call, Response<ApiResponse<List<String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> emails = response.body().getData();
                    if (emails != null && !emails.isEmpty()) {
                        layoutIdResult.setVisibility(View.VISIBLE);
                        // Join emails with comma
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            tvIdResult.setText(String.join("\n", emails));
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (String email : emails) {
                                sb.append(email).append("\n");
                            }
                            tvIdResult.setText(sb.toString().trim());
                        }
                    } else {
                        Toast.makeText(FindAccountActivity.this, "해당 정보로 가입된 계정이 없습니다.", Toast.LENGTH_SHORT).show();
                        layoutIdResult.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(FindAccountActivity.this, "요청 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<String>>> call, Throwable t) {
                Toast.makeText(FindAccountActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            }
        }.start();
    }

    private void resetSendButton() {
        btnSendCode.setEnabled(true);
        btnSendCode.setText("인증번호 전송");
    }

    private void sendCode() {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 버튼 비활성화 및 텍스트 변경
        btnSendCode.setEnabled(false);
        btnSendCode.setText("전송 중...");

        apiService.requestPasswordReset(new EmailVerificationRequest(email, null)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(FindAccountActivity.this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                    layoutVerification.setVisibility(View.VISIBLE);
                    // Optionally disable email input
                    etEmail.setEnabled(false);
                    
                    // 30초 쿨다운 시작
                    startVerificationCooldown();
                } else {
                     // Show error message from backend if available
                    String msg = response.body() != null ? response.body().getMessage() : "전송 실패";
                    Toast.makeText(FindAccountActivity.this, msg, Toast.LENGTH_SHORT).show();
                    resetSendButton();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(FindAccountActivity.this, "오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetSendButton();
            }
        });
    }

    private void verifyCode() {
        String email = etEmail.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        
        if (code.isEmpty()) {
             Toast.makeText(this, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
             return;
        }

        apiService.verifyCode(new EmailVerificationRequest(email, code)).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData()) {
                    Toast.makeText(FindAccountActivity.this, "인증되었습니다.", Toast.LENGTH_SHORT).show();
                    
                    // 타이머 종료
                    if (verificationTimer != null) {
                        verificationTimer.cancel();
                    }
                    
                    layoutResetPw.setVisibility(View.VISIBLE);
                    // Disable code input
                    etCode.setEnabled(false);
                    btnVerifyCode.setEnabled(false);
                } else {
                    Toast.makeText(FindAccountActivity.this, "인증번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                 Toast.makeText(FindAccountActivity.this, "오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        String newPw = etNewPw.getText().toString().trim();
        String confirmPw = etConfirmPw.getText().toString().trim();

        if (newPw.isEmpty() || confirmPw.isEmpty()) {
            Toast.makeText(this, "새 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPw.equals(confirmPw)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.confirmPasswordReset(new PasswordResetConfirmRequest(email, code, newPw)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(FindAccountActivity.this, "비밀번호가 재설정되었습니다. 로그인해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(FindAccountActivity.this, "재설정 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(FindAccountActivity.this, "오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}