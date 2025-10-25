package com.lukehemmin.ai_diet_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
        
        initViews();
        setupListeners();
        updateUI();
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
        btnAddPhotoEmpty.setOnClickListener(v -> {
            Toast.makeText(this, "사진 추가", Toast.LENGTH_SHORT).show();
            // 테스트: 데이터 상태로 전환
            hasData = true;
            updateUI();
        });

        // 텍스트 추가 버튼 (빈 상태)
        btnAddTextEmpty.setOnClickListener(v -> {
            Toast.makeText(this, "텍스트로 추가하기", Toast.LENGTH_SHORT).show();
            // 테스트: 데이터 상태로 전환
            hasData = true;
            updateUI();
        });

        // 사진 추가 버튼 (데이터 상태)
        if (btnAddPhoto != null) {
            btnAddPhoto.setOnClickListener(v -> {
                Toast.makeText(this, "사진 추가", Toast.LENGTH_SHORT).show();
            });
        }
        
        // 텍스트 입력 버튼
        Button btnTextInput = findViewById(R.id.btnTextInput);
        if (btnTextInput != null) {
            btnTextInput.setOnClickListener(v -> {
                Toast.makeText(this, "텍스트 입력", Toast.LENGTH_SHORT).show();
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
}