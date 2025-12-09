package com.lukehemmin.ai_diet_app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.adapter.RecipeHistoryAdapter;
import com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse;
import com.lukehemmin.ai_diet_app.data.model.DietAnalyticsResponse;
import com.lukehemmin.ai_diet_app.data.model.FridgeRecipeHistoryResponse;

import io.noties.markwon.Markwon;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalysisDietFragment extends Fragment {

    // Exercise Plan
    private View btnRefreshExercise;
    private ProgressBar progressExercise;
    private ImageView icRefreshExercise;
    private TextView txtRefreshExercise;
    private TextView txtExerciseContent;
    private TextView txtExerciseCooldown;

    // Custom Recipe
    private View btnRefreshRecipe;
    private ProgressBar progressRecipe;
    private ImageView icRefreshRecipe;
    private TextView txtRefreshRecipe;
    private TextView txtRecipeContent;
    private TextView txtRecipeCooldown;

    // Fridge Recipe
    private EditText etIngredients;
    private MaterialButton btnGenerateFridgeRecipe;
    private MaterialButton btnRecipeHistory;
    private TextView txtFridgeRecipeContent;

    // Charts
    private BarChart chartCalorieTrend;
    private RadarChart chartWeeklyNutrition;
    private Chip chip7days, chip30days;
    
    // Analytics UI
    private TextView txtEatingHabits;
    private TextView txtBadHabitsPercent;
    private TextView txtGoodHabitsPercent;
    private TextView txtImprovePercent;
    private TextView txtPatternAnalysis;
    private LinearLayout containerMealHeatmap;

    private Handler cooldownHandler = new Handler(Looper.getMainLooper());
    private ApiService apiService;
    private Markwon markwon;
    private int currentAnalyticsDays = 7;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analysis_diet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        markwon = Markwon.create(requireContext());
        
        initViews(view);
        setupListeners();
        setupCharts();
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cooldownHandler.removeCallbacksAndMessages(null);
    }

    private void initViews(View view) {
        // Exercise Plan
        btnRefreshExercise = view.findViewById(R.id.btn_refresh_exercise);
        progressExercise = view.findViewById(R.id.progress_exercise);
        icRefreshExercise = view.findViewById(R.id.ic_refresh_exercise);
        txtRefreshExercise = view.findViewById(R.id.txt_refresh_exercise);
        txtExerciseContent = view.findViewById(R.id.txt_exercise_content);
        txtExerciseCooldown = view.findViewById(R.id.txt_exercise_cooldown);

        // Custom Recipe
        btnRefreshRecipe = view.findViewById(R.id.btn_refresh_recipe);
        progressRecipe = view.findViewById(R.id.progress_recipe);
        icRefreshRecipe = view.findViewById(R.id.ic_refresh_recipe);
        txtRefreshRecipe = view.findViewById(R.id.txt_refresh_recipe);
        txtRecipeContent = view.findViewById(R.id.txt_recipe_content);
        txtRecipeCooldown = view.findViewById(R.id.txt_recipe_cooldown);

        // Fridge Recipe
        etIngredients = view.findViewById(R.id.et_ingredients);
        btnGenerateFridgeRecipe = view.findViewById(R.id.btn_generate_fridge_recipe);
        btnRecipeHistory = view.findViewById(R.id.btn_recipe_history);
        txtFridgeRecipeContent = view.findViewById(R.id.txt_fridge_recipe_content);

        // Charts
        chartCalorieTrend = view.findViewById(R.id.chart_calorie_trend);
        chartWeeklyNutrition = view.findViewById(R.id.chart_weekly_nutrition);
        chip7days = view.findViewById(R.id.chip_7days);
        chip30days = view.findViewById(R.id.chip_30days);
        
        // Analytics
        txtEatingHabits = view.findViewById(R.id.txt_eating_habits);
        txtBadHabitsPercent = view.findViewById(R.id.txt_bad_habits_percent);
        txtGoodHabitsPercent = view.findViewById(R.id.txt_good_habits_percent);
        txtImprovePercent = view.findViewById(R.id.txt_improve_percent);
        txtPatternAnalysis = view.findViewById(R.id.txt_pattern_analysis);
        containerMealHeatmap = view.findViewById(R.id.container_meal_heatmap);
    }

    private void setupListeners() {
        btnRefreshExercise.setOnClickListener(v -> refreshExercisePlan());
        btnRefreshRecipe.setOnClickListener(v -> refreshCustomRecipe());
        btnGenerateFridgeRecipe.setOnClickListener(v -> generateFridgeRecipe());
        btnRecipeHistory.setOnClickListener(v -> showRecipeHistoryDialog());

        chip7days.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentAnalyticsDays = 7;
                loadDietAnalytics(7);
            }
        });
        chip30days.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentAnalyticsDays = 30;
                loadDietAnalytics(30);
            }
        });
    }

    private void setupCharts() {
        // Bar Chart
        chartCalorieTrend.getDescription().setEnabled(false);
        chartCalorieTrend.getLegend().setEnabled(false);
        chartCalorieTrend.setDrawValueAboveBar(true);
        chartCalorieTrend.getXAxis().setEnabled(false);
        chartCalorieTrend.getAxisLeft().setEnabled(false);
        chartCalorieTrend.getAxisRight().setEnabled(false);
        chartCalorieTrend.setTouchEnabled(false);

        // Radar Chart
        chartWeeklyNutrition.getDescription().setEnabled(false);
        chartWeeklyNutrition.getLegend().setEnabled(true);
        chartWeeklyNutrition.getYAxis().setEnabled(false);
        chartWeeklyNutrition.getYAxis().setAxisMinimum(0f);
        chartWeeklyNutrition.getYAxis().setAxisMaximum(150f);
        chartWeeklyNutrition.setWebLineWidth(1f);
        chartWeeklyNutrition.setWebColor(requireContext().getColor(R.color.gray_70));
        chartWeeklyNutrition.setWebLineWidthInner(1f);
        chartWeeklyNutrition.setWebColorInner(requireContext().getColor(R.color.gray_70));
    }

    private void loadData() {
        loadExercisePlan();
        loadCustomRecipe();
        loadLatestFridgeRecipe();
        loadDietAnalytics(currentAnalyticsDays);
    }

    // ===== Exercise Plan =====
    private void loadExercisePlan() {
        setExerciseLoading(true);
        apiService.getExercisePlan().enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                if (!isAdded()) return;
                setExerciseLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateExerciseUI(response.body().getData());
                } else {
                    txtExerciseContent.setText("운동 플랜을 불러오는 중 오류가 발생했습니다.\n새로고침을 눌러주세요.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiAnalysisResponse>> call, Throwable t) {
                if (!isAdded()) return;
                setExerciseLoading(false);
                txtExerciseContent.setText("운동 플랜을 불러오지 못했습니다.\n새로고침을 눌러주세요.");
            }
        });
    }

    private void refreshExercisePlan() {
        // 버튼이 비활성화되어 있으면 쿨다운 중
        if (!btnRefreshExercise.isEnabled()) {
            Toast.makeText(getContext(), "쿨다운 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setExerciseLoading(true);
        Toast.makeText(getContext(), "운동 플랜 생성 중...", Toast.LENGTH_SHORT).show();
        
        apiService.refreshExercisePlan(false).enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                if (!isAdded()) return;
                setExerciseLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AiAnalysisResponse data = response.body().getData();
                    if (data != null && data.getContent() != null && !data.getContent().isEmpty()) {
                        updateExerciseUI(data);
                        if (data.isCanRefresh()) {
                            Toast.makeText(getContext(), "운동 플랜이 업데이트되었습니다!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "최신 운동 플랜입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        txtExerciseContent.setText("AI 분석 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.");
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "서버 오류 (" + response.code() + ")";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiAnalysisResponse>> call, Throwable t) {
                if (!isAdded()) return;
                setExerciseLoading(false);
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExerciseUI(AiAnalysisResponse data) {
        if (data.getContent() != null && !data.getContent().isEmpty()) {
            markwon.setMarkdown(txtExerciseContent, data.getContent());
        } else {
            txtExerciseContent.setText("아직 운동 플랜이 없습니다.\n새로고침을 눌러 AI가 추천하는 운동 플랜을 받아보세요.");
        }
        updateRefreshButton(data, btnRefreshExercise, icRefreshExercise, txtRefreshExercise, txtExerciseCooldown, "새로고침");
    }

    private void setExerciseLoading(boolean loading) {
        progressExercise.setVisibility(loading ? View.VISIBLE : View.GONE);
        icRefreshExercise.setVisibility(loading ? View.GONE : View.VISIBLE);
        btnRefreshExercise.setEnabled(!loading);
    }

    // ===== Custom Recipe =====
    private void loadCustomRecipe() {
        setRecipeLoading(true);
        apiService.getCustomRecipe().enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                if (!isAdded()) return;
                setRecipeLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateRecipeUI(response.body().getData());
                } else {
                    txtRecipeContent.setText("레시피를 불러오는 중 오류가 발생했습니다.\n다른 레시피 추천을 눌러주세요.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiAnalysisResponse>> call, Throwable t) {
                if (!isAdded()) return;
                setRecipeLoading(false);
                txtRecipeContent.setText("레시피를 불러오지 못했습니다.\n다른 레시피 추천을 눌러주세요.");
            }
        });
    }

    private void refreshCustomRecipe() {
        if (!btnRefreshRecipe.isEnabled()) {
            Toast.makeText(getContext(), "쿨다운 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setRecipeLoading(true);
        Toast.makeText(getContext(), "레시피 생성 중...", Toast.LENGTH_SHORT).show();
        
        apiService.refreshCustomRecipe(false).enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                if (!isAdded()) return;
                setRecipeLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AiAnalysisResponse data = response.body().getData();
                    if (data != null && data.getContent() != null && !data.getContent().isEmpty()) {
                        updateRecipeUI(data);
                        if (data.isCanRefresh()) {
                            Toast.makeText(getContext(), "새 레시피를 추천받았습니다!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "최신 레시피입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        txtRecipeContent.setText("AI 분석 중 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.");
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "서버 오류 (" + response.code() + ")";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiAnalysisResponse>> call, Throwable t) {
                if (!isAdded()) return;
                setRecipeLoading(false);
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecipeUI(AiAnalysisResponse data) {
        if (data.getContent() != null && !data.getContent().isEmpty()) {
            markwon.setMarkdown(txtRecipeContent, data.getContent());
        } else {
            txtRecipeContent.setText("레시피를 불러오지 못했습니다.\n다른 레시피 추천을 눌러 AI가 추천하는 레시피를 받아보세요.");
        }
        updateRefreshButton(data, btnRefreshRecipe, icRefreshRecipe, txtRefreshRecipe, txtRecipeCooldown, "다른 레시피 추천");
    }

    private void setRecipeLoading(boolean loading) {
        progressRecipe.setVisibility(loading ? View.VISIBLE : View.GONE);
        icRefreshRecipe.setVisibility(loading ? View.GONE : View.VISIBLE);
        btnRefreshRecipe.setEnabled(!loading);
    }

    // ===== Fridge Recipe =====
    private void generateFridgeRecipe() {
        String ingredients = etIngredients.getText().toString().trim();
        if (ingredients.isEmpty()) {
            Toast.makeText(getContext(), "재료를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGenerateFridgeRecipe.setEnabled(false);
        btnGenerateFridgeRecipe.setText("생성 중...");

        Map<String, String> request = new HashMap<>();
        request.put("ingredients", ingredients);

        apiService.generateFridgeRecipe(request).enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                if (!isAdded()) return;
                btnGenerateFridgeRecipe.setEnabled(true);
                btnGenerateFridgeRecipe.setText("나만의 레시피 생성하기");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AiAnalysisResponse data = response.body().getData();
                    if (data.getContent() != null && !data.getContent().isEmpty()) {
                        txtFridgeRecipeContent.setVisibility(View.VISIBLE);
                        markwon.setMarkdown(txtFridgeRecipeContent, data.getContent());
                    }
                } else {
                    Toast.makeText(getContext(), "레시피 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiAnalysisResponse>> call, Throwable t) {
                if (!isAdded()) return;
                btnGenerateFridgeRecipe.setEnabled(true);
                btnGenerateFridgeRecipe.setText("나만의 레시피 생성하기");
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== Fridge Recipe History =====
    private void loadLatestFridgeRecipe() {
        apiService.getLatestFridgeRecipe().enqueue(new Callback<ApiResponse<FridgeRecipeHistoryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FridgeRecipeHistoryResponse>> call, Response<ApiResponse<FridgeRecipeHistoryResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    FridgeRecipeHistoryResponse data = response.body().getData();
                    if (data != null && data.getContent() != null && !data.getContent().isEmpty()) {
                        txtFridgeRecipeContent.setVisibility(View.VISIBLE);
                        markwon.setMarkdown(txtFridgeRecipeContent, data.getContent());
                        etIngredients.setText(data.getIngredients());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FridgeRecipeHistoryResponse>> call, Throwable t) {
                // 실패해도 무시 (최초 사용자는 히스토리 없음)
            }
        });
    }
    
    private void showRecipeHistoryDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_recipe_history, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_history);
        View layoutEmpty = dialogView.findViewById(R.id.layout_empty);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close);
        
        RecipeHistoryAdapter adapter = new RecipeHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        // 커스텀 다이얼로그 생성
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.TransparentDialog)
                .setView(dialogView)
                .create();
        
        // 배경 투명하게
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        // 닫기 버튼
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        adapter.setOnItemClickListener(item -> {
            // 선택한 레시피 표시
            txtFridgeRecipeContent.setVisibility(View.VISIBLE);
            markwon.setMarkdown(txtFridgeRecipeContent, item.getContent());
            etIngredients.setText(item.getIngredients());
            dialog.dismiss();
            Toast.makeText(getContext(), item.getFormattedDate() + " 레시피를 불러왔습니다.", Toast.LENGTH_SHORT).show();
        });
        
        // 히스토리 로드
        apiService.getFridgeRecipeHistory().enqueue(new Callback<ApiResponse<List<FridgeRecipeHistoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FridgeRecipeHistoryResponse>>> call, Response<ApiResponse<List<FridgeRecipeHistoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<FridgeRecipeHistoryResponse> history = response.body().getData();
                    if (history != null && !history.isEmpty()) {
                        adapter.setItems(history);
                        recyclerView.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FridgeRecipeHistoryResponse>>> call, Throwable t) {
                recyclerView.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
        
        dialog.show();
    }

    // ===== Diet Analytics =====
    private void loadDietAnalytics(int days) {
        apiService.getDietAnalytics(days).enqueue(new Callback<ApiResponse<DietAnalyticsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DietAnalyticsResponse>> call, Response<ApiResponse<DietAnalyticsResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    DietAnalyticsResponse data = response.body().getData();
                    if (data != null) {
                        updateCalorieTrendChart(data.getCalorieTrend());
                        updateNutritionBalanceChart(data.getNutritionBalance());
                        updateEatingHabitsAnalysis(data.getEatingHabitsAnalysis());
                        updatePatternAnalysis(data.getPatternAnalysis());
                        updateMealTimeHeatmap(data.getMealTimePattern());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DietAnalyticsResponse>> call, Throwable t) {
                // 실패 시 무시
            }
        });
    }
    
    private void updateCalorieTrendChart(List<DietAnalyticsResponse.CalorieTrendItem> trend) {
        if (trend == null || trend.isEmpty()) return;
        
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        
        for (int i = 0; i < trend.size(); i++) {
            DietAnalyticsResponse.CalorieTrendItem item = trend.get(i);
            entries.add(new BarEntry(i, (float) item.getCalories()));
            labels.add(item.getDayOfWeek());
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "칼로리");
        dataSet.setColor(requireContext().getColor(R.color.primary_blue));
        dataSet.setValueTextColor(requireContext().getColor(R.color.gray_text));
        dataSet.setValueTextSize(10f);
        
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        
        chartCalorieTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartCalorieTrend.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        chartCalorieTrend.getXAxis().setGranularity(1f);
        chartCalorieTrend.getXAxis().setEnabled(true);
        chartCalorieTrend.getXAxis().setDrawGridLines(false);
        chartCalorieTrend.getXAxis().setTextColor(requireContext().getColor(R.color.gray_50));
        
        chartCalorieTrend.setData(barData);
        chartCalorieTrend.animateY(500);
        chartCalorieTrend.invalidate();
    }
    
    private void updateNutritionBalanceChart(DietAnalyticsResponse.NutritionBalance balance) {
        if (balance == null) return;
        
        // 내 섭취량
        ArrayList<RadarEntry> myEntries = new ArrayList<>();
        myEntries.add(new RadarEntry((float) balance.getMyProtein()));
        myEntries.add(new RadarEntry((float) balance.getMyCarbs()));
        myEntries.add(new RadarEntry((float) balance.getMyFat()));
        myEntries.add(new RadarEntry((float) balance.getMyFiber()));
        myEntries.add(new RadarEntry((float) balance.getMyWater()));
        
        RadarDataSet myDataSet = new RadarDataSet(myEntries, "내 섭취량");
        myDataSet.setColor(requireContext().getColor(R.color.primary_green));
        myDataSet.setFillColor(requireContext().getColor(R.color.primary_green));
        myDataSet.setDrawFilled(true);
        myDataSet.setFillAlpha(100);
        myDataSet.setLineWidth(2f);
        
        // 권장 섭취량
        ArrayList<RadarEntry> recEntries = new ArrayList<>();
        recEntries.add(new RadarEntry((float) balance.getRecommendedProtein()));
        recEntries.add(new RadarEntry((float) balance.getRecommendedCarbs()));
        recEntries.add(new RadarEntry((float) balance.getRecommendedFat()));
        recEntries.add(new RadarEntry((float) balance.getRecommendedFiber()));
        recEntries.add(new RadarEntry((float) balance.getRecommendedWater()));
        
        RadarDataSet recDataSet = new RadarDataSet(recEntries, "권장량");
        recDataSet.setColor(requireContext().getColor(R.color.primary_blue));
        recDataSet.setFillColor(requireContext().getColor(R.color.primary_blue));
        recDataSet.setDrawFilled(true);
        recDataSet.setFillAlpha(50);
        recDataSet.setLineWidth(2f);
        
        RadarData radarData = new RadarData(recDataSet, myDataSet);
        radarData.setDrawValues(false);
        
        // 라벨 설정
        String[] labels = {"단백질", "탄수화물", "지방", "식이섬유", "수분"};
        chartWeeklyNutrition.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        
        chartWeeklyNutrition.setData(radarData);
        chartWeeklyNutrition.animateXY(500, 500);
        chartWeeklyNutrition.invalidate();
    }
    
    private void updateEatingHabitsAnalysis(String analysis) {
        if (txtEatingHabits != null && analysis != null) {
            txtEatingHabits.setText(analysis);
        }
    }
    
    private void updatePatternAnalysis(DietAnalyticsResponse.PatternAnalysis pattern) {
        if (pattern == null) return;
        
        if (txtBadHabitsPercent != null) {
            txtBadHabitsPercent.setText(pattern.getBadHabitsPercent() + "%");
        }
        if (txtGoodHabitsPercent != null) {
            txtGoodHabitsPercent.setText(pattern.getGoodHabitsPercent() + "%");
        }
        if (txtImprovePercent != null) {
            txtImprovePercent.setText(pattern.getImprovePercent() + "%");
        }
        if (txtPatternAnalysis != null && pattern.getAnalysisText() != null) {
            txtPatternAnalysis.setText(pattern.getAnalysisText());
        }
    }
    
    private void updateMealTimeHeatmap(List<DietAnalyticsResponse.MealTimePattern> patterns) {
        if (containerMealHeatmap == null || patterns == null || patterns.size() < 7) return;
        
        containerMealHeatmap.removeAllViews();
        
        String[] mealLabels = {"아침", "점심", "저녁"};
        int[] heatmapColors = {
                R.drawable.bg_heatmap_level0,
                R.drawable.bg_heatmap_level1,
                R.drawable.bg_heatmap_level2,
                R.drawable.bg_heatmap_level3
        };
        
        for (int row = 0; row < 3; row++) {
            LinearLayout rowLayout = new LinearLayout(requireContext());
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            rowLayout.setPadding(0, 8, 0, 8);
            
            // 식사 타입 레이블
            TextView label = new TextView(requireContext());
            label.setText(mealLabels[row]);
            label.setTextColor(requireContext().getColor(R.color.gray_subtext));
            label.setTextSize(12);
            label.setWidth((int) (40 * getResources().getDisplayMetrics().density));
            label.setGravity(android.view.Gravity.CENTER_VERTICAL);
            rowLayout.addView(label);
            
            // 각 요일별 셀
            for (int col = 0; col < 7; col++) {
                DietAnalyticsResponse.MealTimePattern pattern = patterns.get(col);
                int count;
                if (row == 0) {
                    count = pattern.getBreakfastCount();
                } else if (row == 1) {
                    count = pattern.getLunchCount();
                } else {
                    count = pattern.getDinnerCount();
                }
                
                // 레벨 결정 (0-4 -> 0-3)
                int level = Math.min(count, 3);
                
                View cell = new View(requireContext());
                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                        0, (int) (32 * getResources().getDisplayMetrics().density), 1f);
                cellParams.setMargins(4, 0, 4, 0);
                cell.setLayoutParams(cellParams);
                cell.setBackgroundResource(heatmapColors[level]);
                
                rowLayout.addView(cell);
            }
            
            containerMealHeatmap.addView(rowLayout);
        }
    }

    // ===== Common =====
    private void updateRefreshButton(AiAnalysisResponse data, View btnRefresh, ImageView icRefresh,
                                      TextView txtRefresh, TextView txtCooldown, String defaultText) {
        if (data.isCanRefresh()) {
            btnRefresh.setEnabled(true);
            txtRefresh.setText(defaultText);
            txtCooldown.setVisibility(View.GONE);
        } else {
            btnRefresh.setEnabled(false);
            long seconds = data.getRemainingCooldownSeconds();
            String cooldownText = data.getFormattedCooldown() + " 후 새로고침 가능";
            txtCooldown.setText(cooldownText);
            txtCooldown.setVisibility(View.VISIBLE);

            cooldownHandler.postDelayed(() -> {
                if (isAdded()) {
                    btnRefresh.setEnabled(true);
                    txtCooldown.setVisibility(View.GONE);
                }
            }, seconds * 1000);
        }
    }
}
