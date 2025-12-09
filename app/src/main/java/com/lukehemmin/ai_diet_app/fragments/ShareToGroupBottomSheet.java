package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.adapters.GroupSelectAdapter;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.GroupResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareToGroupBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_MEAL_ID = "meal_id";

    private RecyclerView rvGroups;
    private LinearLayout llNoGroups;
    private TextInputEditText etMessage;
    private Button btnSkip;
    private Button btnShare;

    private GroupSelectAdapter adapter;
    private ApiService apiService;
    private String mealId;

    private OnShareCompleteListener listener;

    public interface OnShareCompleteListener {
        void onShareComplete(boolean shared);
    }

    public static ShareToGroupBottomSheet newInstance(String mealId) {
        ShareToGroupBottomSheet fragment = new ShareToGroupBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_MEAL_ID, mealId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnShareCompleteListener(OnShareCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        if (getArguments() != null) {
            mealId = getArguments().getString(ARG_MEAL_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_share_to_group, container, false);
        
        initializeViews(view);
        setupListeners();
        loadMyGroups();
        
        return view;
    }

    private void initializeViews(View view) {
        rvGroups = view.findViewById(R.id.rv_groups);
        llNoGroups = view.findViewById(R.id.ll_no_groups);
        etMessage = view.findViewById(R.id.et_message);
        btnSkip = view.findViewById(R.id.btn_skip);
        btnShare = view.findViewById(R.id.btn_share);

        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);

        adapter = new GroupSelectAdapter();
        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGroups.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSkip.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShareComplete(false);
            }
            dismiss();
        });

        btnShare.setOnClickListener(v -> shareToGroups());
    }

    private void loadMyGroups() {
        apiService.getMyGroups().enqueue(new Callback<ApiResponse<Map<String, List<GroupResponse>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, List<GroupResponse>>>> call, 
                                   Response<ApiResponse<Map<String, List<GroupResponse>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<GroupResponse> groups = response.body().getData().get("groups");
                    if (groups != null && !groups.isEmpty()) {
                        adapter.setGroups(groups);
                        rvGroups.setVisibility(View.VISIBLE);
                        llNoGroups.setVisibility(View.GONE);
                    } else {
                        rvGroups.setVisibility(View.GONE);
                        llNoGroups.setVisibility(View.VISIBLE);
                    }
                } else {
                    rvGroups.setVisibility(View.GONE);
                    llNoGroups.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, List<GroupResponse>>>> call, Throwable t) {
                rvGroups.setVisibility(View.GONE);
                llNoGroups.setVisibility(View.VISIBLE);
            }
        });
    }

    private void shareToGroups() {
        if (!adapter.hasSelection()) {
            Toast.makeText(getContext(), "공유할 그룹을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mealId == null || mealId.isEmpty()) {
            Toast.makeText(getContext(), "공유할 식단 정보가 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedGroupIds = adapter.getSelectedGroupIds();
        String message = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";

        // API 호출
        Map<String, Object> request = new java.util.HashMap<>();
        request.put("mealId", mealId);
        request.put("groupIds", selectedGroupIds);
        request.put("message", message);

        apiService.shareMealToGroups(request).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "그룹에 공유되었습니다! 🎉", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onShareComplete(true);
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "공유에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
