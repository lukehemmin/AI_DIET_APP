package com.lukehemmin.ai_diet_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etBirthdate, etPassword, etPasswordConfirm;
    private TextInputLayout tilName, tilEmail, tilBirthdate, tilPassword, tilPasswordConfirm;
    private Button btnSignup;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_page);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etBirthdate = findViewById(R.id.et_birthdate);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);

        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilBirthdate = findViewById(R.id.til_birthdate);
        tilPassword = findViewById(R.id.til_password);
        tilPasswordConfirm = findViewById(R.id.til_password_confirm);

        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);
    }

    private void setupListeners() {
        // 생년월일 선택
        etBirthdate.setOnClickListener(v -> showDatePicker());

        // 회원가입 버튼
        btnSignup.setOnClickListener(v -> handleSignup());

        // 로그인 링크
        tvLogin.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%04d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    etBirthdate.setText(date);
                },
                year, month, day
        );

        // 최대 날짜를 오늘로 설정
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // 기본 날짜를 20년 전으로 설정
        calendar.add(Calendar.YEAR, -20);
        datePickerDialog.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void handleSignup() {
        // 입력값 가져오기
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String birthdate = etBirthdate.getText() != null ? etBirthdate.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String passwordConfirm = etPasswordConfirm.getText() != null ? etPasswordConfirm.getText().toString() : "";

        // 유효성 검사
        if (!validateInputs(name, email, birthdate, password, passwordConfirm)) {
            return;
        }

        // TODO: 실제 회원가입 로직 구현
        // 서버에 회원가입 요청을 보내고 응답을 처리

        // 임시로 성공 메시지 표시 후 로그인 화면으로 이동
        Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validateInputs(String name, String email, String birthdate,
                                   String password, String passwordConfirm) {
        // 에러 초기화
        tilName.setError(null);
        tilEmail.setError(null);
        tilBirthdate.setError(null);
        tilPassword.setError(null);
        tilPasswordConfirm.setError(null);

        // 이름 검증
        if (name.isEmpty()) {
            tilName.setError("이름을 입력해주세요");
            etName.requestFocus();
            return false;
        }

        // 이메일 검증
        if (email.isEmpty()) {
            tilEmail.setError("이메일을 입력해주세요");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("올바른 이메일 형식이 아닙니다");
            etEmail.requestFocus();
            return false;
        }

        // 생년월일 검증
        if (birthdate.isEmpty()) {
            tilBirthdate.setError("생년월일을 선택해주세요");
            return false;
        }

        // 비밀번호 검증
        if (password.isEmpty()) {
            tilPassword.setError("비밀번호를 입력해주세요");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            tilPassword.setError("비밀번호는 8자 이상이어야 합니다");
            etPassword.requestFocus();
            return false;
        }

        // 비밀번호 확인 검증
        if (passwordConfirm.isEmpty()) {
            tilPasswordConfirm.setError("비밀번호를 다시 입력해주세요");
            etPasswordConfirm.requestFocus();
            return false;
        }

        if (!password.equals(passwordConfirm)) {
            tilPasswordConfirm.setError("비밀번호가 일치하지 않습니다");
            etPasswordConfirm.requestFocus();
            return false;
        }

        return true;
    }
}
