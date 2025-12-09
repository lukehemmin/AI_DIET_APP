package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse;

import io.noties.markwon.Markwon;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalysisAiRecipeFragment extends Fragment {

    // AI 하루 식단 계획
    private EditText etMealPreference;
    private MaterialButton btnGenerateMealPlan;
    private ProgressBar progressMealPlan;
    private TextView txtMealPlanContent;

    private ApiService apiService;
    private Markwon markwon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analysis_ai_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiService = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        markwon = Markwon.create(requireContext());
        
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        etMealPreference = view.findViewById(R.id.et_meal_preference);
        btnGenerateMealPlan = view.findViewById(R.id.btn_generate_meal_plan);
        progressMealPlan = view.findViewById(R.id.progress_meal_plan);
        txtMealPlanContent = view.findViewById(R.id.txt_meal_plan_content);
    }

    private void setupListeners() {
        btnGenerateMealPlan.setOnClickListener(v -> generateMealPlan());
    }

    private void generateMealPlan() {
        String preference = etMealPreference.getText().toString().trim();
        
        // 로딩 시작
        btnGenerateMealPlan.setEnabled(false);
        btnGenerateMealPlan.setText("생성 중...");
        progressMealPlan.setVisibility(View.VISIBLE);
        txtMealPlanContent.setVisibility(View.GONE);

        Map<String, String> request = new HashMap<>();
        request.put("preference", preference.isEmpty() ? "균형 잡힌 하루 식단" : preference);

        apiService.generateDailyMealPlan(request).enqueue(new Callback<ApiResponse<AiAnalysisResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AiAnalysisResponse>> call, Response<ApiResponse<AiAnalysisResponse>> response) {
                if (!isAdded()) return;
                
                btnGenerateMealPlan.setEnabled(true);
                btnGenerateMealPlan.setText("🏠 AI 플랜 생성하기");
                progressMealPlan.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AiAnalysisResponse data = response.body().getData();
                    if (data != null && data.getContent() != null && !data.getContent().isEmpty()) {
                        txtMealPlanContent.setVisibility(View.VISIBLE);
                        markwon.setMarkdown(txtMealPlanContent, data.getContent());
                    } else {
                        Toast.makeText(getContext(), "식단 계획 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "서버 오류";
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AiAnalysisResponse>> call, Throwable t) {
                if (!isAdded()) return;
                
                btnGenerateMealPlan.setEnabled(true);
                btnGenerateMealPlan.setText("🏠 AI 플랜 생성하기");
                progressMealPlan.setVisibility(View.GONE);
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
