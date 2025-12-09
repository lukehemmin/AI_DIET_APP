package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalysisAiRecipeFragment extends Fragment {

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
    private TextView txtFridgeRecipeContent;

    // Calorie Chart
    private BarChart chartCalorieTrend;
    private Chip chip7days, chip30days;

    private Handler cooldownHandler = new Handler(Looper.getMainLooper());
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analysis_ai_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 인증 토큰이 포함된 ApiService 초기화
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        
        initViews(view);
        setupListeners();
        setupChart();
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
        txtFridgeRecipeContent = view.findViewById(R.id.txt_fridge_recipe_content);

        // Chart
        chartCalorieTrend = view.findViewById(R.id.chart_calorie_trend);
        chip7days = view.findViewById(R.id.chip_7days);
        chip30days = view.findViewById(R.id.chip_30days);
    }

    private void setupListeners() {
        btnRefreshExercise.setOnClickListener(v -> refreshExercisePlan());
        btnRefreshRecipe.setOnClickListener(v -> refreshCustomRecipe());
        btnGenerateFridgeRecipe.setOnClickListener(v -> generateFridgeRecipe());

        chip7days.setOnClickListener(v -> updateChart(7));
        chip30days.setOnClickListener(v -> updateChart(30));
    }

    private void setupChart() {
        chartCalorieTrend.getDescription().setEnabled(false);
        chartCalorieTrend.getLegend().setEnabled(false);
        chartCalorieTrend.setDrawValueAboveBar(true);
        chartCalorieTrend.getXAxis().setEnabled(false);
        chartCalorieTrend.getAxisLeft().setEnabled(false);
        chartCalorieTrend.getAxisRight().setEnabled(false);
        chartCalorieTrend.setTouchEnabled(false);
        updateChart(7);
    }

    private void loadData() {
        loadExercisePlan();
        loadCustomRecipe();
    }

    // ===== Exercise Plan =====
    private void loadExercisePlan() {
        setExerciseLoading(true);
        apiService.getExercisePlan()
                .enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
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
        setExerciseLoading(true);
        apiService.refreshExercisePlan(false)
                .enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                        if (!isAdded()) return;
                        setExerciseLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AiAnalysisResponse data = response.body().getData();
                            if (data != null && data.getContent() != null && !data.getContent().isEmpty()) {
                                updateExerciseUI(data);
                                Toast.makeText(getContext(), "운동 플랜이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
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
            txtExerciseContent.setText(data.getContent());
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
        apiService.getCustomRecipe()
                .enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
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
        setRecipeLoading(true);
        apiService.refreshCustomRecipe(false)
                .enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                        if (!isAdded()) return;
                        setRecipeLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AiAnalysisResponse data = response.body().getData();
                            if (data != null && data.getContent() != null && !data.getContent().isEmpty()) {
                                updateRecipeUI(data);
                                Toast.makeText(getContext(), "새 레시피를 추천받았습니다.", Toast.LENGTH_SHORT).show();
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
            txtRecipeContent.setText(data.getContent());
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

        apiService.generateFridgeRecipe(request)
                .enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                        btnGenerateFridgeRecipe.setEnabled(true);
                        btnGenerateFridgeRecipe.setText("나만의 레시피 생성하기");

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AiAnalysisResponse data = response.body().getData();
                            if (data.getContent() != null && !data.getContent().isEmpty()) {
                                txtFridgeRecipeContent.setVisibility(View.VISIBLE);
                                txtFridgeRecipeContent.setText(data.getContent());
                            }
                        } else {
                            Toast.makeText(getContext(), "레시피 생성 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AiAnalysisResponse>> call, Throwable t) {
                        btnGenerateFridgeRecipe.setEnabled(true);
                        btnGenerateFridgeRecipe.setText("나만의 레시피 생성하기");
                        Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ===== Common Helper =====
    private void updateRefreshButton(AiAnalysisResponse data, View btn, ImageView icon, TextView text, TextView cooldownText, String defaultText) {
        if (data.isCanRefresh()) {
            btn.setEnabled(true);
            icon.setAlpha(1.0f);
            text.setAlpha(1.0f);
            cooldownText.setVisibility(View.GONE);

            if (data.isNeedsUpdate()) {
                text.setText("업데이트 필요!");
            } else {
                text.setText(defaultText);
            }
        } else {
            btn.setEnabled(false);
            icon.setAlpha(0.5f);
            text.setAlpha(0.5f);
            text.setText(defaultText);

            String formattedCooldown = data.getFormattedCooldown();
            if (formattedCooldown != null) {
                cooldownText.setVisibility(View.VISIBLE);
                cooldownText.setText(formattedCooldown + " 후 새로고침 가능");

                // 쿨다운 타이머 시작
                startCooldownTimer(data.getRemainingCooldownSeconds(), cooldownText, () -> {
                    btn.setEnabled(true);
                    icon.setAlpha(1.0f);
                    text.setAlpha(1.0f);
                    cooldownText.setVisibility(View.GONE);
                });
            }
        }
    }

    private void startCooldownTimer(long seconds, TextView cooldownText, Runnable onComplete) {
        final long[] remaining = {seconds};

        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (remaining[0] <= 0) {
                    onComplete.run();
                    return;
                }

                long minutes = remaining[0] / 60;
                long secs = remaining[0] % 60;
                String text = minutes > 0 
                    ? String.format("%d분 %d초 후 새로고침 가능", minutes, secs)
                    : String.format("%d초 후 새로고침 가능", secs);
                cooldownText.setText(text);

                remaining[0]--;
                cooldownHandler.postDelayed(this, 1000);
            }
        };

        cooldownHandler.post(updateRunnable);
    }

    // ===== Chart =====
    private void updateChart(int days) {
        // TODO: API에서 실제 데이터 가져오기
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            entries.add(new BarEntry(i, (float) (1500 + Math.random() * 1500)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Calorie");
        dataSet.setColor(getContext().getColor(R.color.primary_blue));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        chartCalorieTrend.setData(barData);
        chartCalorieTrend.invalidate();
    }
}
