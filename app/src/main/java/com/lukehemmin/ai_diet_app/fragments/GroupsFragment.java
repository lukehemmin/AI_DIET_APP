package com.lukehemmin.ai_diet_app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.lukehemmin.ai_diet_app.data.model.GroupCreateRequest;
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

    private void openCreateGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("새 그룹 만들기");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText inputName = new EditText(getContext());
        inputName.setHint("그룹 이름");
        layout.addView(inputName);

        final EditText inputDesc = new EditText(getContext());
        inputDesc.setHint("그룹 설명");
        layout.addView(inputDesc);

        final EditText inputChallenge = new EditText(getContext());
        inputChallenge.setHint("챌린지 목표 (예: 50,000kcal 소모)");
        layout.addView(inputChallenge);

        builder.setView(layout);

        builder.setPositiveButton("생성", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            String challenge = inputChallenge.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "그룹 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            createGroup(name, desc, challenge);
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createGroup(String name, String description, String challenge) {
        GroupCreateRequest request = new GroupCreateRequest(name, description, challenge);
        apiService.createGroup(request).enqueue(new Callback<ApiResponse<Map<String, GroupResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, GroupResponse>>> call, Response<ApiResponse<Map<String, GroupResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "그룹이 생성되었습니다.", Toast.LENGTH_SHORT).show();
                    loadGroups(); // Reload list
                } else {
                    Toast.makeText(getContext(), "그룹 생성 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, GroupResponse>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
