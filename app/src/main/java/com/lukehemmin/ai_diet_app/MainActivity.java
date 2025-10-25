package com.lukehemmin.ai_diet_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI 요소
    private TextView tvDate, tvEmptyMessage;
    private CardView emptyStateLayout;
    private LinearLayout dataStateLayout;
    private Button btnAddPhotoEmpty, btnAddTextEmpty, btnAddPhoto;

    // 데이터 상태 (테스트용)
    private boolean hasData = false; // 기본값: 빈 상태

    // 이미지 선택을 위한 ActivityResultLauncher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // WindowInsets 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupImagePicker();
        initViews();
        setupListeners();
        updateUI();
    }

    private void setupImagePicker() {
        // 이미지 선택 결과를 처리하는 ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // TODO: 실제로 이미지 처리 로직 구현
                            Toast.makeText(this, "이미지 선택됨: " + imageUri.toString(), Toast.LENGTH_SHORT).show();
                            // 테스트: 데이터 상태로 전환
                            hasData = true;
                            updateUI();
                        }
                    }
                }
        );
    }
    
    private void initViews() {
        // 뷰 초기화
        tvDate = findViewById(R.id.tvDate);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        dataStateLayout = findViewById(R.id.dataStateLayout);
        btnAddPhotoEmpty = findViewById(R.id.btnAddPhotoEmpty);
        btnAddTextEmpty = findViewById(R.id.btnAddTextEmpty);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
    }
    
    private void setupListeners() {
        // 날짜 네비게이션 버튼
        ImageButton btnPrevDay = findViewById(R.id.btnPrevDay);
        ImageButton btnNextDay = findViewById(R.id.btnNextDay);
        
        btnPrevDay.setOnClickListener(v -> {
            Toast.makeText(this, "이전 날짜", Toast.LENGTH_SHORT).show();
        });
        
        btnNextDay.setOnClickListener(v -> {
            Toast.makeText(this, "다음 날짜", Toast.LENGTH_SHORT).show();
        });
        
        // 사진 추가 버튼 (빈 상태)
        btnAddPhotoEmpty.setOnClickListener(v -> openImagePicker());

        // 텍스트 추가 버튼 (빈 상태)
        btnAddTextEmpty.setOnClickListener(v -> {
            TextInputBottomSheet bottomSheet = new TextInputBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "TextInputBottomSheet");
        });

        // 사진 추가 버튼 (데이터 상태)
        if (btnAddPhoto != null) {
            btnAddPhoto.setOnClickListener(v -> openImagePicker());
        }
        
        // 텍스트 입력 버튼 (데이터 상태)
        Button btnTextInput = findViewById(R.id.btnTextInput);
        if (btnTextInput != null) {
            btnTextInput.setOnClickListener(v -> {
                TextInputBottomSheet bottomSheet = new TextInputBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "TextInputBottomSheet");
            });
        }
        
        // 종합 분석 버튼
        Button btnAnalysis = findViewById(R.id.btnAnalysis);
        if (btnAnalysis != null) {
            btnAnalysis.setOnClickListener(v -> {
                Toast.makeText(this, "종합 분석", Toast.LENGTH_SHORT).show();
            });
        }
        
        // 물 섭취량 버튼
        ImageButton btnWaterMinus = findViewById(R.id.btnWaterMinus);
        ImageButton btnWaterPlus = findViewById(R.id.btnWaterPlus);
        
        if (btnWaterMinus != null) {
            btnWaterMinus.setOnClickListener(v -> {
                Toast.makeText(this, "물 -1잔", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (btnWaterPlus != null) {
            btnWaterPlus.setOnClickListener(v -> {
                Toast.makeText(this, "물 +1잔", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void updateUI() {
        // 데이터 유무에 따라 UI 전환
        if (hasData) {
            // 데이터가 있을 때
            emptyStateLayout.setVisibility(View.GONE);
            dataStateLayout.setVisibility(View.VISIBLE);
            tvDate.setText("10월 2일 목요일");
        } else {
            // 데이터가 없을 때
            emptyStateLayout.setVisibility(View.VISIBLE);
            dataStateLayout.setVisibility(View.GONE);
            tvDate.setText("오늘");

            // 빈 상태 메시지에 오늘 날짜 표시
            String formattedDate = getCurrentDateString();
            String message = getString(R.string.no_record_message, formattedDate);
            tvEmptyMessage.setText(message);
        }
    }

    private String getCurrentDateString() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN);
        return dateFormat.format(calendar.getTime());
    }

    private void openImagePicker() {
        // 이미지 선택 인텐트 생성 (갤러리, 카메라 등 선택 가능)
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // 안드로이드 시스템이 자동으로 앱 선택기를 보여줌
        // (갤러리, 카메라, 파일 매니저 등)
        imagePickerLauncher.launch(Intent.createChooser(intent, "사진 선택"));
    }
}