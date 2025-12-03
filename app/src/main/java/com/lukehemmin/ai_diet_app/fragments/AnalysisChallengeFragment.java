package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.adapters.ChallengeAdapter;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.ChallengeResponse;
import com.lukehemmin.ai_diet_app.data.model.UserChallengeResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalysisChallengeFragment extends Fragment {

    private RecyclerView rvChallenges;
    private ChallengeAdapter adapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis_challenge, container, false);
        
        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        
        initializeViews(view);
        loadChallenges();
        
        return view;
    }

    private void initializeViews(View view) {
        rvChallenges = view.findViewById(R.id.rv_challenges);
        rvChallenges.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ChallengeAdapter();
        adapter.setOnChallengeActionListener(this::joinChallenge);
        rvChallenges.setAdapter(adapter);
    }

    private void loadChallenges() {
        apiService.getAllChallenges().enqueue(new Callback<ApiResponse<Map<String, List<ChallengeResponse>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, List<ChallengeResponse>>>> call, Response<ApiResponse<Map<String, List<ChallengeResponse>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Map<String, List<ChallengeResponse>> data = response.body().getData();
                    List<ChallengeResponse> allChallenges = new ArrayList<>();
                    
                    // Flatten the map (e.g., "daily", "weekly") into a single list for now
                    // or we could use sections. For simplicity, just add all.
                    if (data.containsKey("daily")) {
                        allChallenges.addAll(data.get("daily"));
                    }
                    if (data.containsKey("weekly")) {
                        allChallenges.addAll(data.get("weekly"));
                    }
                    // Add other keys if exists
                    for (String key : data.keySet()) {
                        if (!key.equals("daily") && !key.equals("weekly")) {
                            allChallenges.addAll(data.get(key));
                        }
                    }

                    adapter.setChallenges(allChallenges);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, List<ChallengeResponse>>>> call, Throwable t) {
                Toast.makeText(getContext(), "챌린지 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinChallenge(String challengeId) {
        apiService.joinChallenge(challengeId).enqueue(new Callback<ApiResponse<Map<String, UserChallengeResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, UserChallengeResponse>>> call, Response<ApiResponse<Map<String, UserChallengeResponse>>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "챌린지에 참여했습니다!", Toast.LENGTH_SHORT).show();
                    // Optionally refresh list or update UI
                } else {
                    Toast.makeText(getContext(), "참여 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, UserChallengeResponse>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
