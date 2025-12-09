package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.adapters.FriendInviteAdapter;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.FriendResponse;
import com.lukehemmin.ai_diet_app.data.model.GroupCreateRequest;
import com.lukehemmin.ai_diet_app.data.model.GroupResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChallengeSelectBottomSheet extends BottomSheetDialogFragment {

    private RadioGroup rgChallenges;
    private RadioButton rbChallenge1, rbChallenge2, rbChallenge3, rbChallengeCustom;
    private EditText etCustomChallenge;
    private RecyclerView rvFriends;
    private TextView tvNoFriends;
    private AppCompatButton btnCreate;
    private ImageView btnClose;

    private FriendInviteAdapter friendAdapter;
    private ApiService apiService;

    private String groupName;
    private String groupDescription;
    private String groupGoal;

    private OnGroupCreatedListener listener;

    public interface OnGroupCreatedListener {
        void onGroupCreated(GroupResponse group);
    }

    public void setOnGroupCreatedListener(OnGroupCreatedListener listener) {
        this.listener = listener;
    }

    public static ChallengeSelectBottomSheet newInstance(String groupName, String description, String goal) {
        ChallengeSelectBottomSheet fragment = new ChallengeSelectBottomSheet();
        Bundle args = new Bundle();
        args.putString("groupName", groupName);
        args.putString("description", description);
        args.putString("goal", goal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        
        if (getArguments() != null) {
            groupName = getArguments().getString("groupName", "");
            groupDescription = getArguments().getString("description", "");
            groupGoal = getArguments().getString("goal", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_challenge_select, container, false);
        
        initializeViews(view);
        setupListeners();
        loadFriends();
        
        return view;
    }

    private void initializeViews(View view) {
        rgChallenges = view.findViewById(R.id.rg_challenges);
        rbChallenge1 = view.findViewById(R.id.rb_challenge_1);
        rbChallenge2 = view.findViewById(R.id.rb_challenge_2);
        rbChallenge3 = view.findViewById(R.id.rb_challenge_3);
        rbChallengeCustom = view.findViewById(R.id.rb_challenge_custom);
        etCustomChallenge = view.findViewById(R.id.et_custom_challenge);
        rvFriends = view.findViewById(R.id.rv_friends);
        tvNoFriends = view.findViewById(R.id.tv_no_friends);
        btnCreate = view.findViewById(R.id.btn_create);
        btnClose = view.findViewById(R.id.btn_close);

        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        friendAdapter = new FriendInviteAdapter();
        rvFriends.setAdapter(friendAdapter);

        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        rgChallenges.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_challenge_custom) {
                etCustomChallenge.setVisibility(View.VISIBLE);
            } else {
                etCustomChallenge.setVisibility(View.GONE);
            }
        });

        btnCreate.setOnClickListener(v -> createGroup());
    }

    private void loadFriends() {
        apiService.getFriends().enqueue(new Callback<ApiResponse<Map<String, List<FriendResponse>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, List<FriendResponse>>>> call, 
                                   Response<ApiResponse<Map<String, List<FriendResponse>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<FriendResponse> friends = response.body().getData().get("friends");
                    if (friends != null && !friends.isEmpty()) {
                        friendAdapter.setFriends(friends);
                        rvFriends.setVisibility(View.VISIBLE);
                        tvNoFriends.setVisibility(View.GONE);
                    } else {
                        rvFriends.setVisibility(View.GONE);
                        tvNoFriends.setVisibility(View.VISIBLE);
                    }
                } else {
                    rvFriends.setVisibility(View.GONE);
                    tvNoFriends.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, List<FriendResponse>>>> call, Throwable t) {
                rvFriends.setVisibility(View.GONE);
                tvNoFriends.setVisibility(View.VISIBLE);
            }
        });
    }

    private void createGroup() {
        String challenge = getSelectedChallenge();
        
        if (challenge.isEmpty()) {
            Toast.makeText(getContext(), "챌린지 목표를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> invitedFriendIds = friendAdapter.getSelectedFriendIds();

        GroupCreateRequest request = new GroupCreateRequest(groupName, groupDescription, challenge);
        request.setInvitedFriendIds(invitedFriendIds);

        btnCreate.setEnabled(false);
        btnCreate.setText("생성 중...");

        apiService.createGroup(request).enqueue(new Callback<ApiResponse<Map<String, GroupResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, GroupResponse>>> call, 
                                   Response<ApiResponse<Map<String, GroupResponse>>> response) {
                btnCreate.setEnabled(true);
                btnCreate.setText("+ 생성하기");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "그룹이 생성되었습니다!", Toast.LENGTH_SHORT).show();
                    GroupResponse group = response.body().getData().get("group");
                    if (listener != null && group != null) {
                        listener.onGroupCreated(group);
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "그룹 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, GroupResponse>>> call, Throwable t) {
                btnCreate.setEnabled(true);
                btnCreate.setText("+ 생성하기");
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedChallenge() {
        int selectedId = rgChallenges.getCheckedRadioButtonId();
        
        if (selectedId == R.id.rb_challenge_1) {
            return "그룹 총 5kg 감량 챌린지 (1주)";
        } else if (selectedId == R.id.rb_challenge_2) {
            return "일주일간 설탕 음료 끊기 챌린지";
        } else if (selectedId == R.id.rb_challenge_3) {
            return "매일 1만보 걷고 건강 식단 인증 (10일)";
        } else if (selectedId == R.id.rb_challenge_custom) {
            return etCustomChallenge.getText().toString().trim();
        }
        
        return "";
    }
}
