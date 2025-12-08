package com.lukehemmin.ai_diet_app.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.MealAnalysisResponse;
import com.lukehemmin.ai_diet_app.data.model.MealAnalysisResult;
import com.lukehemmin.ai_diet_app.data.model.MealCreateRequest;
import com.lukehemmin.ai_diet_app.data.model.MealResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;
import com.lukehemmin.ai_diet_app.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.widget.FrameLayout;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.lukehemmin.ai_diet_app.adapters.AnalysisResultAdapter;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class HomeFragment extends Fragment {

    interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }

    private ApiService apiService;
    private TextView txtCurrentKcal, txtGoalKcal, txtCurrentDate;
    private TextView txtCarbsLegend, txtProteinLegend, txtFatLegend;

    private TextView txtWaterCount, txtEmptyMeals, btnAddManual, btnHealthReport;
    private ProgressBar progressCalorie;
    private RecyclerView rvMeals;
    private FloatingActionButton fabAddMeal, fabCamera, fabGallery;
    private ImageView btnPrevDate, btnNextDate;
    private LinearLayout waterGlassesContainer;
    private PieChart pieChart;
    private boolean isFabOpen = false;
    private Calendar currentSelectedDate = Calendar.getInstance();

    // Permission Launchers
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> requestGalleryPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(getContext(), "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Camera & Gallery Launchers
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    try {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        if (bitmap != null) {
                            File file = FileUtils.getFileFromBitmap(requireContext(), bitmap);
                            File compressedFile = FileUtils.compressImage(requireContext(), file);
                            analyzeImage(compressedFile);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    List<Uri> selectedImages = new ArrayList<>();
                    
                    // Check for multiple images
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedImages.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        // Single image
                        selectedImages.add(result.getData().getData());
                    }
                    
                    if (!selectedImages.isEmpty()) {
                        analyzeMultipleImages(selectedImages);
                    }
                }
            }
    );

    // Water glass indicators
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        initializeViews(view);
        setupListeners(view);
        loadInitialData();
        setupPieChart();
        updatePieChart(0, 0, 0);
        
        return view;
    }

    private void initializeViews(View view) {
        // Dashboard
        txtCurrentKcal = view.findViewById(R.id.txt_current_kcal);
        txtGoalKcal = view.findViewById(R.id.txt_goal_kcal);
        progressCalorie = view.findViewById(R.id.progress_calorie);
        txtCarbsLegend = view.findViewById(R.id.txt_carbs_legend);
        txtProteinLegend = view.findViewById(R.id.txt_protein_legend);
        txtFatLegend = view.findViewById(R.id.txt_fat_legend);
        pieChart = view.findViewById(R.id.pie_chart_nutrients);

        // Header
        txtCurrentDate = view.findViewById(R.id.txt_current_date);
        btnPrevDate = view.findViewById(R.id.btn_prev_date);
        btnNextDate = view.findViewById(R.id.btn_next_date);

        // Water intake
        txtWaterCount = view.findViewById(R.id.txt_water_count);
        waterGlassesContainer = view.findViewById(R.id.water_glasses_container);

        // Meal Log
        rvMeals = view.findViewById(R.id.rv_meals);
        txtEmptyMeals = view.findViewById(R.id.txt_empty_meals);
        btnAddManual = view.findViewById(R.id.btn_add_manual);
        fabAddMeal = view.findViewById(R.id.fab_main);
        fabCamera = view.findViewById(R.id.fab_camera);
        fabGallery = view.findViewById(R.id.fab_gallery);
        btnHealthReport = view.findViewById(R.id.suggestion_cta);

        // Setup RecyclerView
        rvMeals.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initial FAB state
        closeFabMenu();
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.btn_water_minus).setOnClickListener(v -> updateWaterIntake(-1));
        view.findViewById(R.id.btn_water_plus).setOnClickListener(v -> updateWaterIntake(1));
        
        btnAddManual.setOnClickListener(v -> openManualAddModal());
        fabAddMeal.setOnClickListener(v -> {
            if (isFabOpen) {
                closeFabMenu();
            } else {
                showFabMenu();
            }
        });
        
        fabCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
            closeFabMenu();
        });

        fabGallery.setOnClickListener(v -> {
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestGalleryPermissionLauncher.launch(permission);
            }
            closeFabMenu();
        });

        if (btnHealthReport != null) {
            btnHealthReport.setOnClickListener(v -> openHealthReport());
        }

        btnPrevDate.setOnClickListener(v -> navigateDate(-1));
        btnNextDate.setOnClickListener(v -> navigateDate(1));
        
        txtCurrentDate.setOnClickListener(v -> {
            if (isSameDay(currentSelectedDate, Calendar.getInstance())) {
                showCalendarDialog();
            } else {
                updateDateDisplay(Calendar.getInstance());
                // TODO: Load data for today
            }
        });

        txtCurrentDate.setOnLongClickListener(v -> {
            showCalendarDialog();
            return true;
        });
    }

    private void updateDateDisplay(Calendar date) {
        currentSelectedDate = (Calendar) date.clone();
        Calendar today = Calendar.getInstance();
        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        if (isSameDay(date, today)) {
            txtCurrentDate.setText("오늘");
            btnNextDate.setVisibility(View.INVISIBLE);
        } else if (isSameDay(date, yesterday)) {
            txtCurrentDate.setText("어제");
            btnNextDate.setVisibility(View.VISIBLE);
        } else {
            txtCurrentDate.setText(new SimpleDateFormat("M월 d일", Locale.KOREA).format(date.getTime()));
            btnNextDate.setVisibility(View.VISIBLE);
        }
        
        // Load meals for the selected date
        loadMealsForDate(currentSelectedDate);
        // Load water intake for the selected date
        loadWaterIntakeForDate(currentSelectedDate);
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
               c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    private void openHealthReport() {
        Intent intent = new Intent(getContext(), com.lukehemmin.ai_diet_app.HealthReportActivity.class);
        startActivity(intent);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "카메라를 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryLauncher.launch(intent);
    }

    private void showCalendarDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_calendar);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        Calendar currentCalendar = (Calendar) currentSelectedDate.clone();
        
        TextView txtMonthYear = dialog.findViewById(R.id.txt_cal_month_year);
        ImageView btnPrev = dialog.findViewById(R.id.btn_cal_prev);
        ImageView btnNext = dialog.findViewById(R.id.btn_cal_next);
        ImageView btnClose = dialog.findViewById(R.id.btn_cal_close);
        RecyclerView rvDays = dialog.findViewById(R.id.rv_calendar_days);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월", Locale.KOREA);
        txtMonthYear.setText(sdf.format(currentCalendar.getTime()));

        CalendarAdapter adapter = new CalendarAdapter(currentCalendar, currentSelectedDate, date -> {
            updateDateDisplay(date);
            dialog.dismiss();
        });
        rvDays.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvDays.setAdapter(adapter);

        btnPrev.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            txtMonthYear.setText(sdf.format(currentCalendar.getTime()));
            adapter.updateMonth(currentCalendar);
        });

        btnNext.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            txtMonthYear.setText(sdf.format(currentCalendar.getTime()));
            adapter.updateMonth(currentCalendar);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private AlertDialog loadingDialog;

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setCancelable(false);
            builder.setView(LayoutInflater.from(requireContext()).inflate(R.layout.dialog_analyzing, null));
            loadingDialog = builder.create();
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void analyzeImage(File file) {
        showLoadingDialog();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        apiService.analyzeMeal(body).enqueue(new Callback<ApiResponse<MealAnalysisResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MealAnalysisResponse>> call, Response<ApiResponse<MealAnalysisResponse>> response) {
                dismissLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    MealAnalysisResponse data = response.body().getData();
                    // 단일 이미지 분석 결과에도 imageUrl 설정
                    if (data.getAnalysisResults() != null && data.getImageUrl() != null) {
                        for (MealAnalysisResult result : data.getAnalysisResults()) {
                            result.setImageUrl(data.getImageUrl());
                        }
                    }
                    showAnalysisResultDialog(file, data);
                } else {
                    String errorMessage = "오류";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            // Extract message manually since we don't have a JSON parser handy for errorBody
                            int msgStart = errorJson.indexOf("\"message\":\"");
                            if (msgStart != -1) {
                                int msgEnd = errorJson.indexOf("\"", msgStart + 11);
                                if (msgEnd != -1) {
                                    errorMessage = errorJson.substring(msgStart + 11, msgEnd);
                                }
                            } else {
                                errorMessage = "서버 응답 오류 (" + response.code() + ")";
                            }
                        } catch (Exception e) {
                            errorMessage = "서버 통신 오류 (" + response.code() + ")";
                        }
                    }
                    Toast.makeText(getContext(), "분석 실패: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MealAnalysisResponse>> call, Throwable t) {
                dismissLoadingDialog();
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void analyzeMultipleImages(List<Uri> imageUris) {
        showLoadingDialog();
        
        List<MealAnalysisResult> allResults = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();
        final int[] processedCount = {0};
        final int totalCount = imageUris.size();
        
        for (Uri uri : imageUris) {
            try {
                File file = FileUtils.getFileFromUri(requireContext(), uri);
                File compressedFile = FileUtils.compressImage(requireContext(), file);
                
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), compressedFile);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", compressedFile.getName(), requestFile);
                
                apiService.analyzeMeal(body).enqueue(new Callback<ApiResponse<MealAnalysisResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MealAnalysisResponse>> call, Response<ApiResponse<MealAnalysisResponse>> response) {
                        processedCount[0]++;
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            MealAnalysisResponse data = response.body().getData();
                            String currentImageUrl = data.getImageUrl();
                            if (data.getAnalysisResults() != null) {
                                // 각 분석 결과에 해당 이미지 URL 설정
                                for (MealAnalysisResult result : data.getAnalysisResults()) {
                                    result.setImageUrl(currentImageUrl);
                                }
                                allResults.addAll(data.getAnalysisResults());
                            }
                            if (currentImageUrl != null) {
                                imageUrls.add(currentImageUrl);
                            }
                        }
                        
                        // All images processed
                        if (processedCount[0] >= totalCount) {
                            dismissLoadingDialog();
                            if (!allResults.isEmpty()) {
                                MealAnalysisResponse combinedResponse = new MealAnalysisResponse();
                                combinedResponse.setAnalysisResults(allResults);
                                combinedResponse.setImageUrl(imageUrls.isEmpty() ? null : imageUrls.get(0));
                                combinedResponse.setImageUrls(new ArrayList<>(imageUrls));
                                showAnalysisResultDialog(null, combinedResponse);
                            } else {
                                Toast.makeText(getContext(), "분석 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<MealAnalysisResponse>> call, Throwable t) {
                        processedCount[0]++;
                        if (processedCount[0] >= totalCount) {
                            dismissLoadingDialog();
                            if (!allResults.isEmpty()) {
                                MealAnalysisResponse combinedResponse = new MealAnalysisResponse();
                                combinedResponse.setAnalysisResults(allResults);
                                combinedResponse.setImageUrl(imageUrls.isEmpty() ? null : imageUrls.get(0));
                                combinedResponse.setImageUrls(new ArrayList<>(imageUrls));
                                showAnalysisResultDialog(null, combinedResponse);
                            } else {
                                Toast.makeText(getContext(), "모든 이미지 분석에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            } catch (Exception e) {
                processedCount[0]++;
                if (processedCount[0] >= totalCount) {
                    dismissLoadingDialog();
                    if (!allResults.isEmpty()) {
                        MealAnalysisResponse combinedResponse = new MealAnalysisResponse();
                        combinedResponse.setAnalysisResults(allResults);
                        combinedResponse.setImageUrl(imageUrls.isEmpty() ? null : imageUrls.get(0));
                        combinedResponse.setImageUrls(new ArrayList<>(imageUrls));
                        showAnalysisResultDialog(null, combinedResponse);
                    } else {
                        Toast.makeText(getContext(), "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showAnalysisResultDialog(File imageFile, MealAnalysisResponse data) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_meal_analysis);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Setup ViewPager2 for image carousel
        androidx.viewpager2.widget.ViewPager2 vpImages = dialog.findViewById(R.id.vp_analyzed_images);
        LinearLayout indicatorContainer = dialog.findViewById(R.id.indicator_container);
        
        List<String> imageUrls = data.getImageUrls();
        if (imageFile != null) {
            // Single file - add to list
            imageUrls = new ArrayList<>();
            imageUrls.add(Uri.fromFile(imageFile).toString());
        }
        
        if (!imageUrls.isEmpty()) {
            ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(imageUrls);
            vpImages.setAdapter(carouselAdapter);
            
            // Setup page indicator if more than 1 image
            if (imageUrls.size() > 1) {
                indicatorContainer.setVisibility(View.VISIBLE);
                setupPageIndicator(indicatorContainer, imageUrls.size());
                vpImages.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        updatePageIndicator(indicatorContainer, position);
                    }
                });
            }
        }

        RecyclerView rvAnalysisResults = dialog.findViewById(R.id.rv_analysis_results);
        TextView txtTotalCalories = dialog.findViewById(R.id.txt_total_calories);
        Spinner spinnerMealTime = dialog.findViewById(R.id.spinner_meal_time);

        List<MealAnalysisResult> results = data.getAnalysisResults();
        if (results == null) results = new ArrayList<>();

        final AnalysisResultAdapter[] adapterRef = new AnalysisResultAdapter[1];
        adapterRef[0] = new AnalysisResultAdapter(results, position -> {
            adapterRef[0].removeItem(position);
            updateTotalCalories(adapterRef[0].getItems(), txtTotalCalories);
        });

        rvAnalysisResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAnalysisResults.setAdapter(adapterRef[0]);

        updateTotalCalories(results, txtTotalCalories);

        String[] mealTimeLabels = {"아침", "점심", "저녁", "간식", "야식"};
        final String[] mealTimeValues = {"BREAKFAST", "LUNCH", "DINNER", "SNACK", "LATE_NIGHT"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mealTimeLabels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealTime.setAdapter(spinnerAdapter);

        // Determine meal time: check if all items are snacks
        boolean allSnacks = true;
        for (MealAnalysisResult item : results) {
            if (!item.getIsSnack()) {
                allSnacks = false;
                break;
            }
        }

        int defaultIndex;
        if (allSnacks && !results.isEmpty()) {
            // All items are snacks → select "간식"
            defaultIndex = 3;
        } else {
            // At least one regular meal item → select by time
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            if (hour >= 6 && hour < 11) defaultIndex = 0;       // 아침
            else if (hour >= 11 && hour < 17) defaultIndex = 1; // 점심
            else if (hour >= 17 && hour < 22) defaultIndex = 2; // 저녁
            else defaultIndex = 4;                               // 야식
        }
        spinnerMealTime.setSelection(defaultIndex);

        dialog.findViewById(R.id.btn_add_to_diet).setOnClickListener(v -> {
            List<MealAnalysisResult> currentItems = adapterRef[0].getItems();
            if (currentItems.isEmpty()) {
                Toast.makeText(getContext(), "저장할 음식이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedTimeIndex = spinnerMealTime.getSelectedItemPosition();
            String selectedMealTime = mealTimeValues[selectedTimeIndex];
            // Use selected date instead of current date
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentSelectedDate.getTime());
            String fallbackImageUrl = data.getImageUrl();  // 개별 imageUrl이 없는 경우 대비

            List<MealCreateRequest.MealItemRequest> mealRequests = new ArrayList<>();
            for (MealAnalysisResult item : currentItems) {
                // 각 음식 항목에 해당하는 이미지 URL 사용 (없으면 fallback)
                String itemImageUrl = item.getImageUrl() != null ? item.getImageUrl() : fallbackImageUrl;
                mealRequests.add(new MealCreateRequest.MealItemRequest(
                        item.getFoodItem(),
                        item.getServingSize() != null ? item.getServingSize() : 1.0,
                        item.getKcal(),
                        item.getCarbs(),
                        item.getProtein(),
                        item.getFat(),
                        selectedMealTime,
                        date,
                        itemImageUrl
                ));
            }

            saveMeal(mealRequests, dialog);
        });

        dialog.show();
    }

    private void updateTotalCalories(List<MealAnalysisResult> items, TextView view) {
        double total = 0;
        for (MealAnalysisResult item : items) {
            if (item.getKcal() != null) {
                total += item.getKcal();
            }
        }
        view.setText(String.format(Locale.US, "%,.0f kcal", total));
    }

    private void saveMeal(List<MealCreateRequest.MealItemRequest> meals, Dialog dialog) {
        apiService.createMeals(new MealCreateRequest(meals)).enqueue(new Callback<ApiResponse<Map<String, List<MealResponse>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, List<MealResponse>>>> call, Response<ApiResponse<Map<String, List<MealResponse>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "식단이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Refresh meal list
                    loadMealsForDate(currentSelectedDate);
                } else {
                    Toast.makeText(getContext(), "저장 실패: " + (response.body() != null ? response.body().getMessage() : "오류"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, List<MealResponse>>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
        private Calendar calendar;
        private final OnDateSelectedListener listener;
        private final List<Calendar> days = new ArrayList<>();
        private Calendar selectedDate;

        public CalendarAdapter(Calendar calendar, Calendar selectedDate, OnDateSelectedListener listener) {
            this.calendar = (Calendar) calendar.clone();
            this.selectedDate = (Calendar) selectedDate.clone();
            this.listener = listener;
            generateDays();
        }

        public void updateMonth(Calendar calendar) {
            this.calendar = (Calendar) calendar.clone();
            generateDays();
            notifyDataSetChanged();
        }

        private void generateDays() {
            days.clear();
            Calendar temp = (Calendar) calendar.clone();
            temp.set(Calendar.DAY_OF_MONTH, 1);
            
            int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK); // 1 (Sun) to 7 (Sat)
            int maxDays = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            // Add empty days for offset
            for (int i = 1; i < firstDayOfWeek; i++) {
                days.add(null);
            }
            
            // Add days of the month
            for (int i = 1; i <= maxDays; i++) {
                Calendar day = (Calendar) temp.clone();
                day.set(Calendar.DAY_OF_MONTH, i);
                days.add(day);
            }
        }

        @NonNull
        @Override
        public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
            return new CalendarViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
            Calendar day = days.get(position);
            if (day == null) {
                holder.txtDay.setText("");
                holder.txtDay.setOnClickListener(null);
                holder.txtDay.setSelected(false);
            } else {
                int dayOfMonth = day.get(Calendar.DAY_OF_MONTH);
                holder.txtDay.setText(String.valueOf(dayOfMonth));

                boolean isFuture = isFutureDate(day);
                boolean isSelected = isSameDay(day, selectedDate);

                if (isFuture) {
                    holder.txtDay.setTextColor(getContext().getColor(R.color.gray_subtext));
                    holder.txtDay.setTypeface(null, Typeface.NORMAL);
                    holder.txtDay.setOnClickListener(null);
                    holder.txtDay.setBackgroundResource(0);
                    holder.txtDay.setSelected(false);
                } else {
                    // Check if selected
                    holder.txtDay.setSelected(isSelected);
                    if (isSelected) {
                        holder.txtDay.setTextColor(Color.WHITE);
                        holder.txtDay.setTypeface(null, Typeface.BOLD);
                    } else {
                        holder.txtDay.setTextColor(getContext().getColor(R.color.gray_text));
                        holder.txtDay.setTypeface(null, Typeface.NORMAL);
                    }

                    holder.txtDay.setOnClickListener(v -> {
                        selectedDate = day;
                        notifyDataSetChanged(); // Refresh UI to update selection
                        listener.onDateSelected(day);
                    });
                }
            }
        }

        private boolean isSameDay(Calendar c1, Calendar c2) {
            return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                   c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                   c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        class CalendarViewHolder extends RecyclerView.ViewHolder {
            TextView txtDay;

            CalendarViewHolder(View itemView) {
                super(itemView);
                txtDay = itemView.findViewById(R.id.txt_day);
            }
        }
    }

    private void loadInitialData() {
        // Initialize with default values
        txtCurrentKcal.setText("0");
        txtGoalKcal.setText("/ 2,662 kcal");
        progressCalorie.setProgress(0);
        
        txtCarbsLegend.setText("탄수화물 (0%)");
        txtProteinLegend.setText("단백질 (0%)");
        txtFatLegend.setText("지방 (0%)");
        
        txtWaterCount.setText("0");
        updateWaterGlasses(0);
        
        txtCurrentDate.setText("오늘");
        // Hide next button for today (can't go to future)
        btnNextDate.setVisibility(View.INVISIBLE);
        
        // Show empty state initially
        showEmptyState(true);
        updateCalorieProgress(0, 2662);
        
        // Load meals from server
        loadMealsForDate(currentSelectedDate);
        // Load water intake from server
        loadWaterIntakeForDate(currentSelectedDate);
    }

    private void loadWaterIntakeForDate(Calendar date) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.getTime());
        
        apiService.getWaterIntake(dateStr).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    int glasses = 0;
                    if (data.get("glasses") != null) {
                        glasses = ((Number) data.get("glasses")).intValue();
                    }
                    txtWaterCount.setText(String.valueOf(glasses));
                    updateWaterGlasses(glasses);
                } else {
                    txtWaterCount.setText("0");
                    updateWaterGlasses(0);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                txtWaterCount.setText("0");
                updateWaterGlasses(0);
            }
        });
    }

    private void updateWaterIntake(int delta) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentSelectedDate.getTime());
        
        Call<ApiResponse<Map<String, Object>>> call;
        if (delta > 0) {
            call = apiService.addWaterGlass(dateStr);
        } else {
            call = apiService.removeWaterGlass(dateStr);
        }
        
        call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    int glasses = 0;
                    if (data.get("glasses") != null) {
                        glasses = ((Number) data.get("glasses")).intValue();
                    }
                    txtWaterCount.setText(String.valueOf(glasses));
                    updateWaterGlasses(glasses);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                // Ignore failure
            }
        });
    }

    private void updateWaterGlasses(int count) {
        for (int i = 0; i < waterGlassesContainer.getChildCount(); i++) {
            ImageView glass = (ImageView) waterGlassesContainer.getChildAt(i);
            if (i < count) {
                glass.setColorFilter(getContext().getColor(R.color.primary_blue), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                glass.setColorFilter(getContext().getColor(R.color.gray_70), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }


    private void showEmptyState(boolean show) {
        txtEmptyMeals.setVisibility(show ? View.VISIBLE : View.GONE);
        rvMeals.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void openManualAddModal() {
        // TODO: Implement manual add modal
    }

    private void navigateDate(int delta) {
        Calendar nextDate = (Calendar) currentSelectedDate.clone();
        nextDate.add(Calendar.DAY_OF_MONTH, delta);
        
        if (isFutureDate(nextDate)) {
            Toast.makeText(getContext(), "미래의 날짜는 선택할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSelectedDate.add(Calendar.DAY_OF_MONTH, delta);
        updateDateDisplay(currentSelectedDate);
        // TODO: Load data for the new date
    }

    private boolean isFutureDate(Calendar date) {
        Calendar today = Calendar.getInstance();
        if (date.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return true;
        if (date.get(Calendar.YEAR) < today.get(Calendar.YEAR)) return false;
        
        if (date.get(Calendar.MONTH) > today.get(Calendar.MONTH)) return true;
        if (date.get(Calendar.MONTH) < today.get(Calendar.MONTH)) return false;
        
        return date.get(Calendar.DAY_OF_MONTH) > today.get(Calendar.DAY_OF_MONTH);
    }

    private void showFabMenu() {
        isFabOpen = true;
        fabAddMeal.setImageResource(R.drawable.ic_plus_circle); // Change to close icon

        fabCamera.setVisibility(View.VISIBLE);
        fabGallery.setVisibility(View.VISIBLE);

        fabCamera.animate().translationY(-getResources().getDimension(R.dimen.fab_margin_1));
        
        fabGallery.animate().translationY(-getResources().getDimension(R.dimen.fab_margin_2));
    }

    private void closeFabMenu() {
        isFabOpen = false;
        fabAddMeal.setImageResource(R.drawable.ic_plus);

        fabCamera.animate().translationY(0).withEndAction(() -> fabCamera.setVisibility(View.GONE));

        fabGallery.animate().translationY(0).withEndAction(() -> fabGallery.setVisibility(View.GONE));
    }

    private void updateCalorieProgress(int currentKcal, int goalKcal) {
        if (goalKcal > 0) {
            int progress = (int) ((currentKcal / (float) goalKcal) * 100);
            progressCalorie.setProgress(progress);
        } else {
            progressCalorie.setProgress(0);
        }
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setHoleRadius(58f);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
    }

    private void updatePieChart(float protein, float fat, float carbs) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (protein == 0 && fat == 0 && carbs == 0) {
            entries.add(new PieEntry(1f));
            colors.add(getContext().getColor(R.color.gray_70));
        } else {
            if (protein > 0) {
                entries.add(new PieEntry(protein));
                colors.add(getContext().getColor(R.color.primary_green));
            }
            if (fat > 0) {
                entries.add(new PieEntry(fat));
                colors.add(getContext().getColor(R.color.primary_orange));
            }
            if (carbs > 0) {
                entries.add(new PieEntry(carbs));
                colors.add(getContext().getColor(R.color.primary_blue));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(0f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChart.setDrawEntryLabels(false);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void loadMealsForDate(Calendar date) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.getTime());
        
        apiService.getMeals(dateStr).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    
                    // Parse meals list
                    List<Map<String, Object>> mealsList = (List<Map<String, Object>>) data.get("meals");
                    
                    if (mealsList == null || mealsList.isEmpty()) {
                        showEmptyState(true);
                        updateCalorieProgress(0, 2662);
                        txtCurrentKcal.setText("0");
                        updatePieChart(0, 0, 0);
                        txtCarbsLegend.setText("탄수화물 (0%)");
                        txtProteinLegend.setText("단백질 (0%)");
                        txtFatLegend.setText("지방 (0%)");
                    } else {
                        showEmptyState(false);
                        
                        // Calculate totals
                        double totalKcal = 0;
                        double totalCarbs = 0;
                        double totalProtein = 0;
                        double totalFat = 0;
                        
                        // Parse all meals first
                        List<MealResponse> allMeals = new ArrayList<>();
                        for (Map<String, Object> mealMap : mealsList) {
                            MealResponse meal = new MealResponse();
                            meal.setId(mealMap.get("id") != null ? String.valueOf(mealMap.get("id")) : null);
                            meal.setFoodItem((String) mealMap.get("foodItem"));
                            meal.setKcal(mealMap.get("kcal") != null ? ((Number) mealMap.get("kcal")).doubleValue() : 0);
                            meal.setCarbs(mealMap.get("carbs") != null ? ((Number) mealMap.get("carbs")).doubleValue() : 0);
                            meal.setProtein(mealMap.get("protein") != null ? ((Number) mealMap.get("protein")).doubleValue() : 0);
                            meal.setFat(mealMap.get("fat") != null ? ((Number) mealMap.get("fat")).doubleValue() : 0);
                            meal.setMealTime((String) mealMap.get("mealTime"));
                            meal.setImageUrl((String) mealMap.get("imageUrl"));
                            meal.setCreatedAt((String) mealMap.get("createdAt"));
                            allMeals.add(meal);
                            
                            totalKcal += meal.getKcal();
                            totalCarbs += meal.getCarbs();
                            totalProtein += meal.getProtein();
                            totalFat += meal.getFat();
                        }
                        
                        // Sort by createdAt
                        Collections.sort(allMeals, (a, b) -> {
                            String aTime = a.getCreatedAt();
                            String bTime = b.getCreatedAt();
                            if (aTime == null) return 1;
                            if (bTime == null) return -1;
                            return aTime.compareTo(bTime);
                        });
                        
                        // Group meals - main meals (BREAKFAST, LUNCH, DINNER) are single groups
                        // SNACK, LATE_NIGHT are grouped by their order
                        Map<String, List<MealResponse>> mainMealGroups = new LinkedHashMap<>();
                        Map<String, String> firstCreatedAt = new LinkedHashMap<>();
                        
                        for (MealResponse meal : allMeals) {
                            String mealTime = meal.getMealTime() != null ? meal.getMealTime() : "OTHER";
                            
                            if (!mainMealGroups.containsKey(mealTime)) {
                                mainMealGroups.put(mealTime, new ArrayList<>());
                                firstCreatedAt.put(mealTime, meal.getCreatedAt());
                            }
                            mainMealGroups.get(mealTime).add(meal);
                        }
                        
                        // Convert to MealGroup list and sort by first createdAt
                        List<MealGroup> groups = new ArrayList<>();
                        for (Map.Entry<String, List<MealResponse>> entry : mainMealGroups.entrySet()) {
                            MealGroup group = new MealGroup(entry.getKey(), entry.getValue());
                            group.setFirstCreatedAt(firstCreatedAt.get(entry.getKey()));
                            groups.add(group);
                        }
                        
                        // Sort groups by firstCreatedAt
                        Collections.sort(groups, (a, b) -> {
                            String aTime = a.getFirstCreatedAt();
                            String bTime = b.getFirstCreatedAt();
                            if (aTime == null) return 1;
                            if (bTime == null) return -1;
                            return aTime.compareTo(bTime);
                        });
                        
                        // Update UI
                        txtCurrentKcal.setText(String.format(Locale.US, "%,.0f", totalKcal));
                        updateCalorieProgress((int) totalKcal, 2662);
                        
                        // Update pie chart
                        double totalMacro = totalCarbs + totalProtein + totalFat;
                        if (totalMacro > 0) {
                            float carbsPercent = (float) (totalCarbs / totalMacro * 100);
                            float proteinPercent = (float) (totalProtein / totalMacro * 100);
                            float fatPercent = (float) (totalFat / totalMacro * 100);
                            
                            updatePieChart(proteinPercent, fatPercent, carbsPercent);
                            txtCarbsLegend.setText(String.format(Locale.US, "탄수화물 (%.0f%%)", carbsPercent));
                            txtProteinLegend.setText(String.format(Locale.US, "단백질 (%.0f%%)", proteinPercent));
                            txtFatLegend.setText(String.format(Locale.US, "지방 (%.0f%%)", fatPercent));
                        }
                        
                        // Setup RecyclerView adapter with grouped meals
                        MealGroupAdapter adapter = new MealGroupAdapter(groups);
                        rvMeals.setAdapter(adapter);
                    }
                } else {
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showEmptyState(true);
            }
        });
    }

    // MealGroup class for grouping meals by mealTime
    private static class MealGroup {
        private final String mealTime;
        private final List<MealResponse> meals;
        private String firstCreatedAt;
        
        MealGroup(String mealTime, List<MealResponse> meals) {
            this.mealTime = mealTime;
            this.meals = meals;
        }
        
        String getMealTime() { return mealTime; }
        List<MealResponse> getMeals() { return meals; }
        String getFirstCreatedAt() { return firstCreatedAt; }
        void setFirstCreatedAt(String firstCreatedAt) { this.firstCreatedAt = firstCreatedAt; }
        
        double getTotalKcal() {
            double total = 0;
            for (MealResponse m : meals) total += m.getKcal();
            return total;
        }
        
        double getTotalCarbs() {
            double total = 0;
            for (MealResponse m : meals) total += m.getCarbs();
            return total;
        }
        
        double getTotalProtein() {
            double total = 0;
            for (MealResponse m : meals) total += m.getProtein();
            return total;
        }
        
        double getTotalFat() {
            double total = 0;
            for (MealResponse m : meals) total += m.getFat();
            return total;
        }
        
        // Get unique image URLs
        List<String> getImageUrls() {
            List<String> urls = new ArrayList<>();
            for (MealResponse m : meals) {
                if (m.getImageUrl() != null && !m.getImageUrl().isEmpty() && !urls.contains(m.getImageUrl())) {
                    urls.add(m.getImageUrl());
                }
            }
            return urls;
        }
        
        // Format food names like "떡볶이, 어묵 외 4개"
        String getFormattedFoodNames() {
            if (meals.isEmpty()) return "";
            if (meals.size() == 1) return meals.get(0).getFoodItem();
            if (meals.size() == 2) return meals.get(0).getFoodItem() + ", " + meals.get(1).getFoodItem();
            
            // 3개 이상: "첫번째, 두번째 외 N개"
            return meals.get(0).getFoodItem() + ", " + meals.get(1).getFoodItem() + " 외 " + (meals.size() - 2) + "개";
        }
    }

    // Adapter for grouped meals
    private class MealGroupAdapter extends RecyclerView.Adapter<MealGroupAdapter.MealGroupViewHolder> {
        private final List<MealGroup> groups;

        MealGroupAdapter(List<MealGroup> groups) {
            this.groups = groups;
        }

        @NonNull
        @Override
        public MealGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_group, parent, false);
            return new MealGroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MealGroupViewHolder holder, int position) {
            MealGroup group = groups.get(position);
            holder.bind(group);
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }

        class MealGroupViewHolder extends RecyclerView.ViewHolder {
            private final TextView txtMealTime;
            private final TextView txtFoodNames;
            private final TextView txtMealSummary;
            private final ImageView imgMealFront;
            private final ImageView imgMealBack;
            private final androidx.cardview.widget.CardView cardImageBack;
            private final FrameLayout imagesContainer;

            MealGroupViewHolder(View itemView) {
                super(itemView);
                txtMealTime = itemView.findViewById(R.id.txt_meal_time);
                txtFoodNames = itemView.findViewById(R.id.txt_food_names);
                txtMealSummary = itemView.findViewById(R.id.txt_meal_summary);
                imgMealFront = itemView.findViewById(R.id.img_meal_front);
                imgMealBack = itemView.findViewById(R.id.img_meal_back);
                cardImageBack = itemView.findViewById(R.id.card_image_back);
                imagesContainer = itemView.findViewById(R.id.images_container);
            }

            void bind(MealGroup group) {
                // Meal time label
                String mealTimeLabel = getMealTimeLabel(group.getMealTime());
                txtMealTime.setText(mealTimeLabel);
                
                // Food names (e.g., "떡볶이, 어묵 외 4개")
                txtFoodNames.setText(group.getFormattedFoodNames());
                
                // Summary (total calories and macros)
                String summary = String.format(Locale.US, "%,.0f kcal • 탄 %.0fg • 단 %.0fg • 지 %.0fg",
                        group.getTotalKcal(), group.getTotalCarbs(), group.getTotalProtein(), group.getTotalFat());
                txtMealSummary.setText(summary);
                
                // Load images
                List<String> imageUrls = group.getImageUrls();
                android.util.Log.d("MealGroup", "Image URLs: " + imageUrls.toString());
                if (!imageUrls.isEmpty()) {
                    imagesContainer.setVisibility(View.VISIBLE);
                    android.util.Log.d("MealGroup", "Loading image: " + imageUrls.get(0));
                    // Load front image
                    com.bumptech.glide.Glide.with(itemView.getContext())
                            .load(imageUrls.get(0))
                            .centerCrop()
                            .placeholder(R.color.gray_70)
                            .error(R.color.gray_70)
                            .into(imgMealFront);
                    
                    // Load back image if multiple images
                    if (imageUrls.size() > 1) {
                        cardImageBack.setVisibility(View.VISIBLE);
                        com.bumptech.glide.Glide.with(itemView.getContext())
                                .load(imageUrls.get(1))
                                .centerCrop()
                                .placeholder(R.color.gray_70)
                                .error(R.color.gray_70)
                                .into(imgMealBack);
                    } else {
                        cardImageBack.setVisibility(View.GONE);
                    }
                } else {
                    // No images - show placeholder
                    imgMealFront.setImageResource(R.color.gray_70);
                    cardImageBack.setVisibility(View.GONE);
                }
                
                // Click listener to show detail dialog
                itemView.setOnClickListener(v -> showMealDetailDialog(group));
            }
            
            private String getMealTimeLabel(String mealTime) {
                if (mealTime == null) return "기타";
                switch (mealTime) {
                    case "BREAKFAST": return "아침";
                    case "LUNCH": return "점심";
                    case "DINNER": return "저녁";
                    case "SNACK": return "간식";
                    case "LATE_NIGHT": return "야식";
                    default: return "기타";
                }
            }
        }
    }

    private void showMealDetailDialog(MealGroup group) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_meal_detail);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Find views
        TextView txtMealTime = dialog.findViewById(R.id.txt_meal_time);
        androidx.viewpager2.widget.ViewPager2 vpImages = dialog.findViewById(R.id.vp_meal_images);
        LinearLayout indicatorContainer = dialog.findViewById(R.id.indicator_container);
        TextView txtTotalSummary = dialog.findViewById(R.id.txt_total_summary);
        RecyclerView rvFoodItems = dialog.findViewById(R.id.rv_food_items);
        ImageView btnClose = dialog.findViewById(R.id.btn_close);

        // Set meal time
        String mealTimeLabel = getMealTimeLabelStatic(group.getMealTime());
        txtMealTime.setText(mealTimeLabel);

        // Load images with carousel
        List<String> imageUrls = group.getImageUrls();
        if (!imageUrls.isEmpty()) {
            ImageCarouselAdapter carouselAdapter = new ImageCarouselAdapter(imageUrls);
            vpImages.setAdapter(carouselAdapter);
            
            // Setup page indicator if more than 1 image
            if (imageUrls.size() > 1) {
                indicatorContainer.setVisibility(View.VISIBLE);
                setupPageIndicator(indicatorContainer, imageUrls.size());
                vpImages.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        updatePageIndicator(indicatorContainer, position);
                    }
                });
            }
        }

        // Set total summary
        txtTotalSummary.setText(String.format(Locale.US, "총 %,.0f kcal", group.getTotalKcal()));

        // Setup food items list
        rvFoodItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFoodItems.setAdapter(new FoodDetailAdapter(group.getMeals()));

        // Close button
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private static String getMealTimeLabelStatic(String mealTime) {
        if (mealTime == null) return "기타";
        switch (mealTime) {
            case "BREAKFAST": return "아침";
            case "LUNCH": return "점심";
            case "DINNER": return "저녁";
            case "SNACK": return "간식";
            case "LATE_NIGHT": return "야식";
            default: return "기타";
        }
    }

    // Adapter for food detail list in dialog
    private class FoodDetailAdapter extends RecyclerView.Adapter<FoodDetailAdapter.FoodDetailViewHolder> {
        private final List<MealResponse> foods;

        FoodDetailAdapter(List<MealResponse> foods) {
            this.foods = foods;
        }

        @NonNull
        @Override
        public FoodDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_detail, parent, false);
            return new FoodDetailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FoodDetailViewHolder holder, int position) {
            MealResponse food = foods.get(position);
            holder.bind(food);
        }

        @Override
        public int getItemCount() {
            return foods.size();
        }

        class FoodDetailViewHolder extends RecyclerView.ViewHolder {
            private final TextView txtFoodName;
            private final TextView txtCalories;

            FoodDetailViewHolder(View itemView) {
                super(itemView);
                txtFoodName = itemView.findViewById(R.id.txt_food_name);
                txtCalories = itemView.findViewById(R.id.txt_calories);
            }

            void bind(MealResponse food) {
                txtFoodName.setText(food.getFoodItem());
                txtCalories.setText(String.format(Locale.US, "%.0f kcal", food.getKcal()));
            }
        }
    }

    // Image Carousel Adapter for ViewPager2
    private class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ImageViewHolder> {
        private final List<String> imageUrls;

        ImageCarouselAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String url = imageUrls.get(position);
            com.bumptech.glide.Glide.with(holder.imageView.getContext())
                    .load(url)
                    .centerCrop()
                    .placeholder(R.color.gray_70)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(ImageView itemView) {
                super(itemView);
                this.imageView = itemView;
            }
        }
    }

    private void setupPageIndicator(LinearLayout container, int count) {
        container.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.indicator_dot);
            dot.setSelected(i == 0);
            container.addView(dot);
        }
    }

    private void updatePageIndicator(LinearLayout container, int position) {
        for (int i = 0; i < container.getChildCount(); i++) {
            container.getChildAt(i).setSelected(i == position);
        }
    }
}
