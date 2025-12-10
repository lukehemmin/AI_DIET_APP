package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.adapters.GroupAdapter;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.GroupResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsFragment extends Fragment {

    private RecyclerView rvGroups;
    private TextView btnCreateGroup;
    private GroupAdapter groupAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        
        initializeViews(view);
        setupListeners();
        loadGroups();
        
        return view;
    }

    private void initializeViews(View view) {
        rvGroups = view.findViewById(R.id.rv_groups);
        btnCreateGroup = view.findViewById(R.id.btn_create_group);

        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter();
        groupAdapter.setOnGroupClickListener(this::openGroupDetail);
        rvGroups.setAdapter(groupAdapter);

        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
    }

    private void setupListeners() {
        btnCreateGroup.setOnClickListener(v -> openCreateGroupDialog());
    }

    private void loadGroups() {
        apiService.getGroups().enqueue(new Callback<ApiResponse<Map<String, List<GroupResponse>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, List<GroupResponse>>>> call, Response<ApiResponse<Map<String, List<GroupResponse>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<GroupResponse> groups = response.body().getData().get("groups");
                    if (groups != null) {
                        groupAdapter.setGroups(groups);
                    }
                } else {
                    Toast.makeText(getContext(), "그룹 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, List<GroupResponse>>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 그룹 만들기 Bottom Sheet 열기
     */
    private void openCreateGroupDialog() {
        CreateGroupBottomSheet bottomSheet = CreateGroupBottomSheet.newInstance();
        bottomSheet.setOnGroupCreateListener((groupName, description, goal) -> {
            // AI 챌린지 추천 다이얼로그 열기
            openChallengeSelectDialog(groupName, description, goal);
        });
        bottomSheet.show(getChildFragmentManager(), "CreateGroupBottomSheet");
    }

    /**
     * AI 챌린지 선택 Bottom Sheet 열기
     */
    private void openChallengeSelectDialog(String groupName, String description, String goal) {
        ChallengeSelectBottomSheet bottomSheet = ChallengeSelectBottomSheet.newInstance(groupName, description, goal);
        bottomSheet.setOnGroupCreatedListener(group -> {
            // 그룹 생성 완료, 목록 새로고침
            loadGroups();
        });
        bottomSheet.show(getChildFragmentManager(), "ChallengeSelectBottomSheet");
    }

    /**
     * 그룹 상세 화면 열기
     */
    private void openGroupDetail(GroupResponse group) {
        GroupDetailFragment detailFragment = GroupDetailFragment.newInstance(group.getId(), group.getName());
        
        getParentFragmentManager().beginTransaction()
                .hide(this)  // 현재 프래그먼트 숨기기 (replace 대신)
                .add(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}
