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
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
                    Uri selectedImage = result.getData().getData();
                    try {
                        File file = FileUtils.getFileFromUri(requireContext(), selectedImage);
                        File compressedFile = FileUtils.compressImage(requireContext(), file);
                        analyzeImage(compressedFile);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
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

    private void showAnalysisResultDialog(File imageFile, MealAnalysisResponse data) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_meal_analysis);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView ivAnalyzed = dialog.findViewById(R.id.iv_analyzed_image);
        ivAnalyzed.setImageURI(Uri.fromFile(imageFile));

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
        final String[] mealTimeValues = {"BREAKFAST", "LUNCH", "DINNER", "SNACK", "MIDNIGHT_SNACK"};

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mealTimeLabels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealTime.setAdapter(spinnerAdapter);

        int defaultIndex = 3; 
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour >= 6 && hour < 11) defaultIndex = 0; 
        else if (hour >= 11 && hour < 17) defaultIndex = 1; 
        else if (hour >= 17 && hour < 22) defaultIndex = 2; 
        else if (hour >= 22 || hour < 6) defaultIndex = 4; 
        spinnerMealTime.setSelection(defaultIndex);

        dialog.findViewById(R.id.btn_add_to_diet).setOnClickListener(v -> {
            List<MealAnalysisResult> currentItems = adapterRef[0].getItems();
            if (currentItems.isEmpty()) {
                Toast.makeText(getContext(), "저장할 음식이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedTimeIndex = spinnerMealTime.getSelectedItemPosition();
            String selectedMealTime = mealTimeValues[selectedTimeIndex];
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
            String serverImagePath = data.getImageUrl();

            List<MealCreateRequest.MealItemRequest> mealRequests = new ArrayList<>();
            for (MealAnalysisResult item : currentItems) {
                mealRequests.add(new MealCreateRequest.MealItemRequest(
                        item.getFoodItem(),
                        item.getServingSize() != null ? item.getServingSize() : 1.0,
                        item.getKcal(),
                        item.getCarbs(),
                        item.getProtein(),
                        item.getFat(),
                        selectedMealTime,
                        date,
                        serverImagePath
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
                    // TODO: Refresh meal list
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
        
        // Show empty state
        showEmptyState(true);
        updateCalorieProgress(0, 2662);
    }
    private void updateWaterIntake(int delta) {
        int current = Integer.parseInt(txtWaterCount.getText().toString());
        int newValue = Math.max(0, Math.min(8, current + delta));
        txtWaterCount.setText(String.valueOf(newValue));
        updateWaterGlasses(newValue);
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
                entries.add(new PieEntry(protein, "단백질"));
                colors.add(getContext().getColor(R.color.primary_green));
            }
            if (fat > 0) {
                entries.add(new PieEntry(fat, "지방"));
                colors.add(getContext().getColor(R.color.primary_orange));
            }
            if (carbs > 0) {
                entries.add(new PieEntry(carbs, "탄수화물"));
                colors.add(getContext().getColor(R.color.primary_blue));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Nutrients");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(0f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(0f);

        pieChart.setData(data);
        pieChart.invalidate();
    }
}
