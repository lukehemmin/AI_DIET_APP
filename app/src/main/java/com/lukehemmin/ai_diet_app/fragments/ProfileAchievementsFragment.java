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
import com.lukehemmin.ai_diet_app.adapters.BadgeAdapter;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.BadgeResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileAchievementsFragment extends Fragment {

    private RecyclerView rvBadges;
    private BadgeAdapter badgeAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_achievements, container, false);

        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);

        initializeViews(view);
        loadBadges();

        return view;
    }

    private void initializeViews(View view) {
        rvBadges = view.findViewById(R.id.rv_badges);
        rvBadges.setLayoutManager(new LinearLayoutManager(getContext()));
        
        badgeAdapter = new BadgeAdapter();
        rvBadges.setAdapter(badgeAdapter);
    }

    private void loadBadges() {
        apiService.getMyBadges().enqueue(new Callback<ApiResponse<List<BadgeResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BadgeResponse>>> call, Response<ApiResponse<List<BadgeResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<BadgeResponse> badges = response.body().getData();
                    badgeAdapter.setBadges(badges);
                } else {
                    // If fails or empty, maybe show empty state or toast
                    // For now just empty list
                    badgeAdapter.setBadges(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BadgeResponse>>> call, Throwable t) {
                Toast.makeText(getContext(), "배지 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
