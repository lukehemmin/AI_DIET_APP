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
    private ImageButton btnCalendar;
    private NutritionCircleView nutritionCircle;
    private LinearLayout waterDropsContainer, mealsContainer;

    // 데이터 상태 (테스트용)
    private boolean hasData = false; // 기본값: 빈 상태

    // 날짜 관리
    private Calendar selectedDate;

    // 이미지 선택을 위한 ActivityResultLauncher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // 음식 분석을 위한 ActivityResultLauncher
    private ActivityResultLauncher<Intent> foodAnalysisLauncher;

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

        selectedDate = Calendar.getInstance(); // 오늘 날짜로 초기화

        setupImagePicker();
        setupFoodAnalysisLauncher();
        initViews();
        setupListeners();
        setupDateClickListeners();
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
                            // 이미지 선택 완료 후 음식 분석 화면으로 이동
                            Intent intent = new Intent(this, FoodAnalysisActivity.class);
                            intent.putExtra("image_uri", imageUri.toString());
                            foodAnalysisLauncher.launch(intent);
                        }
                    }
                }
        );
    }

    private void setupFoodAnalysisLauncher() {
        // 음식 분석 결과를 처리하는 ActivityResultLauncher
        foodAnalysisLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // 분석 완료 후 데이터 상태로 전환
                        hasData = true;
                        updateUI();
                        Toast.makeText(this, "음식이 추가되었습니다", Toast.LENGTH_SHORT).show();
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
        btnCalendar = findViewById(R.id.btnCalendar);
        nutritionCircle = findViewById(R.id.nutritionCircle);
        waterDropsContainer = findViewById(R.id.waterDropsContainer);
        mealsContainer = findViewById(R.id.mealsContainer);
    }
    
    private void setupListeners() {
        // 날짜 네비게이션 버튼
        ImageButton btnPrevDay = findViewById(R.id.btnPrevDay);
        ImageButton btnNextDay = findViewById(R.id.btnNextDay);
        
        btnPrevDay.setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_MONTH, -1);
            updateUI();
        });

        btnNextDay.setOnClickListener(v -> {
            selectedDate.add(Calendar.DAY_OF_MONTH, 1);
            updateUI();
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
        Button btnAddText = findViewById(R.id.btnAddText);
        if (btnAddText != null) {
            btnAddText.setOnClickListener(v -> {
                TextInputBottomSheet bottomSheet = new TextInputBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "TextInputBottomSheet");
            });
        }

        // 종합 분석 버튼
        Button btnAnalysis = findViewById(R.id.btnAnalysis);
        if (btnAnalysis != null) {
            btnAnalysis.setOnClickListener(v -> {
                Toast.makeText(this, "종합 분석 기능 준비 중", Toast.LENGTH_SHORT).show();
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
        // 날짜 표시 업데이트
        updateDateDisplay();

        // 데이터 유무에 따라 UI 전환
        if (hasData) {
            // 데이터가 있을 때
            emptyStateLayout.setVisibility(View.GONE);
            dataStateLayout.setVisibility(View.VISIBLE);
            setupDataStateUI();
        } else {
            // 데이터가 없을 때
            emptyStateLayout.setVisibility(View.VISIBLE);
            dataStateLayout.setVisibility(View.GONE);

            // 빈 상태 메시지에 선택된 날짜 표시
            String formattedDate = getFormattedDate(selectedDate);
            String message = getString(R.string.no_record_message, formattedDate);
            tvEmptyMessage.setText(message);
        }
    }

    private void updateDateDisplay() {
        if (isToday(selectedDate)) {
            tvDate.setText("오늘");
        } else {
            tvDate.setText(getFormattedDate(selectedDate));
        }
    }

    private String getFormattedDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M월 d일 EEEE", Locale.KOREAN);
        return dateFormat.format(calendar.getTime());
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

    // TextInputBottomSheet에서 호출하는 public 메소드
    public void startFoodAnalysisWithText(String foodText, String mealTime) {
        Intent intent = new Intent(this, FoodAnalysisActivity.class);
        intent.putExtra("food_text", foodText);
        intent.putExtra("meal_time", mealTime);
        foodAnalysisLauncher.launch(intent);
    }

    private void setupDateClickListeners() {
        // 달력 버튼 클릭 시 달력 표시
        if (btnCalendar != null) {
            btnCalendar.setOnClickListener(v -> showCalendarDialog());
        }

        // 날짜 텍스트 클릭 시 오늘로 이동
        tvDate.setOnClickListener(v -> {
            if (!isToday(selectedDate)) {
                selectedDate = Calendar.getInstance();
                updateUI();
                Toast.makeText(this, "오늘 날짜로 이동했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCalendarDialog() {
        CalendarDialog calendarDialog = new CalendarDialog();
        calendarDialog.setOnDateSelectedListener(selectedDate -> {
            this.selectedDate = (Calendar) selectedDate.clone();
            updateUI();
        });
        calendarDialog.show(getSupportFragmentManager(), "CalendarDialog");
    }

    private boolean isToday(Calendar date) {
        Calendar today = Calendar.getInstance();
        return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    private void setupDataStateUI() {
        if (!hasData || nutritionCircle == null) return;

        // 원형 게이지 설정 (테스트 데이터)
        nutritionCircle.setNutritionData(195f, 95f, 62f);

        // 물방울 생성
        setupWaterDrops(8, 8);

        // 식사 리스트 생성
        setupMealsList();
    }

    private void setupWaterDrops(int current, int total) {
        if (waterDropsContainer == null) return;

        waterDropsContainer.removeAllViews();

        for (int i = 0; i < total; i++) {
            View drop = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    40, // width in dp
                    48  // height in dp
            );
            params.weight = 1;
            params.setMargins(4, 0, 4, 0);
            drop.setLayoutParams(params);

            if (i < current) {
                drop.setBackgroundResource(R.drawable.bg_water_drop);
            } else {
                drop.setBackgroundResource(R.drawable.bg_water_drop_empty);
            }

            waterDropsContainer.addView(drop);
        }
    }

    private void setupMealsList() {
        if (mealsContainer == null) return;

        mealsContainer.removeAllViews();

        // 테스트 데이터
        addMealItem("아침", "09:00", "오트밀, 바나나, 아몬드", 380);
        addMealItem("점심", "13:00", "김치찌개, 현미밥, 두부", 650);
        addMealItem("저녁", "18:30", "닭가슴살 스테이크, 샐러드", 550);
        addMealItem("간식", "21:00", "그릭 요거트", 270);
    }

    private void addMealItem(String mealType, String time, String foods, int calories) {
        View mealView = getLayoutInflater().inflate(R.layout.item_meal, mealsContainer, false);

        TextView tvMealTime = mealView.findViewById(R.id.tvMealTime);
        TextView tvFoodItems = mealView.findViewById(R.id.tvFoodItems);
        TextView tvCalories = mealView.findViewById(R.id.tvCalories);

        tvMealTime.setText(mealType + " " + time);
        tvFoodItems.setText(foods);
        tvCalories.setText(String.valueOf(calories));

        mealsContainer.addView(mealView);
    }
}