package com.lukehemmin.ai_diet_app.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.adapters.FriendAdapter;
import com.lukehemmin.ai_diet_app.ble.NearbyFriendService;
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
    private View btnAddFriend;

    private FriendAdapter friendAdapter;
    private FriendAdapter requestAdapter;
    private ApiService apiService;
    private NearbyFriendService nearbyFriendService;
    private Dialog currentDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_friends, container, false);

        // Fix: Pass context to getClient()
        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
        
        // Initialize BLE service
        nearbyFriendService = new NearbyFriendService(requireContext());

        initializeViews(view);
        setupAdapters();
        loadData();

        return view;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop BLE discovery when leaving
        if (nearbyFriendService != null && nearbyFriendService.isDiscovering()) {
            nearbyFriendService.stopDiscovery();
        }
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
        Dialog dialog = new Dialog(requireContext());
        currentDialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_friend);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Close button
        ImageView btnClose = dialog.findViewById(R.id.btn_close);
        TextView tvScanningStatus = dialog.findViewById(R.id.tv_scanning_status);
        
        btnClose.setOnClickListener(v -> {
            nearbyFriendService.stopDiscovery();
            dialog.dismiss();
        });

        // Start radar animations
        View ring1 = dialog.findViewById(R.id.radar_ring_1);
        View ring2 = dialog.findViewById(R.id.radar_ring_2);
        View ring3 = dialog.findViewById(R.id.radar_ring_3);

        Animation pulse1 = AnimationUtils.loadAnimation(getContext(), R.anim.radar_pulse);
        Animation pulse2 = AnimationUtils.loadAnimation(getContext(), R.anim.radar_pulse_delayed);
        Animation pulse3 = AnimationUtils.loadAnimation(getContext(), R.anim.radar_pulse_delayed2);

        ring1.startAnimation(pulse1);
        ring2.startAnimation(pulse2);
        ring3.startAnimation(pulse3);
        
        // Setup BLE callback
        nearbyFriendService.setCallback(new NearbyFriendService.NearbyFriendCallback() {
            @Override
            public void onFriendFound(NearbyFriendService.NearbyFriend friend) {
                // 주변에서 친구 발견!
                tvScanningStatus.setText("주변에서 " + friend.name + "님을 발견했습니다!");
                // TODO: 발견된 친구 목록에 추가하고 클릭 시 친구 요청 보내기
            }
            
            @Override
            public void onScanStarted() {
                tvScanningStatus.setText("주변에서 친구를 찾고 있습니다...");
            }
            
            @Override
            public void onScanStopped() {
                tvScanningStatus.setText("검색이 완료되었습니다.");
            }
            
            @Override
            public void onError(String message) {
                tvScanningStatus.setText(message);
            }
        });
        
        // Start BLE discovery
        if (nearbyFriendService.isBluetoothSupported() && nearbyFriendService.hasRequiredPermissions()) {
            nearbyFriendService.startDiscovery();
        } else if (!nearbyFriendService.isBluetoothSupported()) {
            tvScanningStatus.setText("블루투스를 지원하지 않는 기기입니다.\n아래 버튼으로 친구를 추가해주세요.");
        } else {
            tvScanningStatus.setText("블루투스 권한이 필요합니다.\n아래 버튼으로 친구를 추가해주세요.");
        }

        // Email search button
        LinearLayout btnEmailSearch = dialog.findViewById(R.id.btn_email_search);
        btnEmailSearch.setOnClickListener(v -> {
            nearbyFriendService.stopDiscovery();
            dialog.dismiss();
            showEmailSearchDialog();
        });
        
        // Stop discovery when dialog is dismissed
        dialog.setOnDismissListener(d -> {
            nearbyFriendService.stopDiscovery();
            currentDialog = null;
        });

        dialog.show();
    }

    private void showEmailSearchDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_email_friend_search);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Close button
        ImageView btnClose = dialog.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        EditText etSearch = dialog.findViewById(R.id.et_search);
        TextView tvEmptyState = dialog.findViewById(R.id.tv_empty_state);
        RecyclerView rvSearchResults = dialog.findViewById(R.id.rv_search_results);
        View progressBar = dialog.findViewById(R.id.progress_bar);

        // Setup search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 3) {
                    // TODO: Implement search API
                    tvEmptyState.setText("'" + query + "'(으)로 친구 요청을 보내려면\n검색 버튼을 누르세요.");
                } else {
                    tvEmptyState.setText("검색어를 입력해주세요.");
                }
            }
        });

        // Handle search/send action
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String email = etSearch.getText().toString().trim();
            if (!email.isEmpty()) {
                sendFriendRequest(email);
                dialog.dismiss();
                return true;
            }
            return false;
        });

        dialog.show();
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
