package com.lukehemmin.ai_diet_app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.adapters.FriendAdapter;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.FriendRequest;
import com.lukehemmin.ai_diet_app.data.model.FriendResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFriendsFragment extends Fragment {

    private RecyclerView rvFriends;
    private RecyclerView rvFriendRequests;
    private TextView tvRequestsHeader;
    private TextView txtEmptyFriends;
    private TextView btnAddFriend;

    private FriendAdapter friendAdapter;
    private FriendAdapter requestAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_friends, container, false);

        // Fix: Pass context to getClient()
        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);

        initializeViews(view);
        setupAdapters();
        loadData();

        return view;
    }

    private void initializeViews(View view) {
        rvFriends = view.findViewById(R.id.rv_friends);
        rvFriendRequests = view.findViewById(R.id.rv_friend_requests);
        tvRequestsHeader = view.findViewById(R.id.tv_requests_header);
        txtEmptyFriends = view.findViewById(R.id.txt_empty_friends);
        btnAddFriend = view.findViewById(R.id.btn_add_friend);

        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAddFriend.setOnClickListener(v -> showAddFriendDialog());
    }

    private void setupAdapters() {
        friendAdapter = new FriendAdapter(false, null);
        rvFriends.setAdapter(friendAdapter);

        requestAdapter = new FriendAdapter(true, this::acceptFriendRequest);
        rvFriendRequests.setAdapter(requestAdapter);
    }

    private void loadData() {
        loadFriends();
        loadFriendRequests();
    }

    private void loadFriends() {
        // Fix: Update Callback type to match ApiService (Map<String, List<FriendResponse>>)
        apiService.getFriends().enqueue(new Callback<ApiResponse<Map<String, List<FriendResponse>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, List<FriendResponse>>>> call, Response<ApiResponse<Map<String, List<FriendResponse>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Fix: Extract "friends" list from the map
                    Map<String, List<FriendResponse>> data = response.body().getData();
                    List<FriendResponse> friends = data.get("friends");
                    
                    if (friends == null) friends = new ArrayList<>();

                    friendAdapter.setFriends(friends);
                    
                    if (friends.isEmpty()) {
                        txtEmptyFriends.setVisibility(View.VISIBLE);
                    } else {
                        txtEmptyFriends.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, List<FriendResponse>>>> call, Throwable t) {
                Toast.makeText(getContext(), "친구 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFriendRequests() {
        // Fix: Update Callback type to match ApiService (Map<String, List<FriendResponse>>)
        apiService.getFriendRequests().enqueue(new Callback<ApiResponse<Map<String, List<FriendResponse>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, List<FriendResponse>>>> call, Response<ApiResponse<Map<String, List<FriendResponse>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Fix: Extract "requests" list from the map
                    Map<String, List<FriendResponse>> data = response.body().getData();
                    List<FriendResponse> requests = data.get("requests");
                    
                    if (requests == null) requests = new ArrayList<>();

                    requestAdapter.setFriends(requests);

                    if (!requests.isEmpty()) {
                        tvRequestsHeader.setVisibility(View.VISIBLE);
                        rvFriendRequests.setVisibility(View.VISIBLE);
                    } else {
                        tvRequestsHeader.setVisibility(View.GONE);
                        rvFriendRequests.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, List<FriendResponse>>>> call, Throwable t) {
                // Ignore error for requests
            }
        });
    }

    private void acceptFriendRequest(String friendshipId) {
        apiService.acceptFriendRequest(friendshipId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "친구 요청을 수락했습니다.", Toast.LENGTH_SHORT).show();
                    loadData(); // Refresh both lists
                } else {
                    Toast.makeText(getContext(), "요청 수락 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("친구 추가");

        final EditText input = new EditText(getContext());
        input.setHint("친구 이메일 입력");
        builder.setView(input);

        builder.setPositiveButton("요청 보내기", (dialog, which) -> {
            String email = input.getText().toString();
            if (!email.isEmpty()) {
                sendFriendRequest(email);
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void sendFriendRequest(String email) {
        // Fix: Use constructor with email argument
        FriendRequest request = new FriendRequest(email);

        apiService.sendFriendRequest(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "요청 전송 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
