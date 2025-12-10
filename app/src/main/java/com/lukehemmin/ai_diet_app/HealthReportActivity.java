package com.lukehemmin.ai_diet_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.WeeklyReportResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthReportActivity extends AppCompatActivity {
    
    private TextView tvAiSummary;
    private TextView tvAvgCalories;
    private TextView tvTopFoods;
    private TextView tvCompletedChallenges;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_report);
        
        initializeViews();
        loadWeeklyReport();

        findViewById(R.id.btn_close_report).setOnClickListener(v -> finish());
    }
    
    private void initializeViews() {
        tvAiSummary = findViewById(R.id.tv_ai_summary);
        tvAvgCalories = findViewById(R.id.tv_avg_calories);
        tvTopFoods = findViewById(R.id.tv_top_foods);
        tvCompletedChallenges = findViewById(R.id.tv_completed_challenges);
        
        apiService = RetrofitClient.getClient(this).create(ApiService.class);
    }
    
    private void loadWeeklyReport() {
        apiService.getWeeklyReport().enqueue(new Callback<ApiResponse<WeeklyReportResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<WeeklyReportResponse>> call, 
                                   Response<ApiResponse<WeeklyReportResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateUI(response.body().getData());
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<WeeklyReportResponse>> call, Throwable t) {
                showError();
            }
        });
    }
    
    private void updateUI(WeeklyReportResponse report) {
        if (report == null) {
            showError();
            return;
        }
        
        // AI 주간 총평
        if (report.getAiSummary() != null && !report.getAiSummary().isEmpty()) {
            tvAiSummary.setText(report.getAiSummary());
        } else {
            tvAiSummary.setText("이번 주 식단 기록을 분석 중이에요.");
        }
        
        // 주간 평균 칼로리
        tvAvgCalories.setText(String.format(Locale.US, "%,.0f", report.getAverageCalories()));
        
        // 가장 많이 먹은 음식
        List<WeeklyReportResponse.TopFood> topFoods = report.getTopFoods();
        if (topFoods != null && !topFoods.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < topFoods.size(); i++) {
                WeeklyReportResponse.TopFood food = topFoods.get(i);
                sb.append(i + 1).append(". ")
                  .append(food.getFoodName())
                  .append(" (").append(food.getCount()).append("회)");
                if (i < topFoods.size() - 1) {
                    sb.append("\n");
                }
            }
            tvTopFoods.setText(sb.toString());
        } else {
            tvTopFoods.setText("이번 주 기록이 없어요.");
        }
        
        // 완료한 챌린지
        List<WeeklyReportResponse.CompletedChallenge> challenges = report.getCompletedChallenges();
        if (challenges != null && !challenges.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < challenges.size(); i++) {
                WeeklyReportResponse.CompletedChallenge challenge = challenges.get(i);
                sb.append("🏆 ").append(challenge.getName());
                if (challenge.getCompletedAt() != null) {
                    sb.append(" (").append(challenge.getCompletedAt()).append(")");
                }
                if (i < challenges.size() - 1) {
                    sb.append("\n");
                }
            }
            tvCompletedChallenges.setText(sb.toString());
        } else {
            tvCompletedChallenges.setText("이번 주 완료한 챌린지가 없어요.");
        }
    }
    
    private void showError() {
        tvAiSummary.setText("데이터를 불러오는데 실패했습니다.");
        tvAvgCalories.setText("0");
        tvTopFoods.setText("데이터를 불러올 수 없어요.");
        tvCompletedChallenges.setText("데이터를 불러올 수 없어요.");
    }
}
