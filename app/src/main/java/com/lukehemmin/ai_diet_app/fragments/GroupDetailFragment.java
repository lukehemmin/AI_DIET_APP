package com.lukehemmin.ai_diet_app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.GroupDetailResponse;
import com.lukehemmin.ai_diet_app.data.model.GroupMemberResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupDetailFragment extends Fragment {

    private static final String ARG_GROUP_ID = "group_id";
    private static final String ARG_GROUP_NAME = "group_name";

    private ImageView btnBack;
    private ImageView btnMenu;
    private TextView tvGroupName;
    private TextView tvGroupDescription;
    private TextView tvMemberCount;
    private LinearLayout llMemberAvatars;
    private TextView tvChallengeName;
    private ProgressBar progressChallenge;
    private TextView tvChallengeProgress;
    private CardView cardEmptyFeed;
    private RecyclerView rvFeed;

    private ApiService apiService;
    private String groupId;
    private String groupName;
    private String userRole = "NONE";  // LEADER, MEMBER, NONE

    public static GroupDetailFragment newInstance(String groupId, String groupName) {
        GroupDetailFragment fragment = new GroupDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putString(ARG_GROUP_NAME, groupName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
            groupName = getArguments().getString(ARG_GROUP_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_detail, container, false);
        
        initializeViews(view);
        setupListeners();
        loadGroupDetail();
        
        return view;
    }

    private void initializeViews(View view) {
        btnBack = view.findViewById(R.id.btn_back);
        btnMenu = view.findViewById(R.id.btn_menu);
        tvGroupName = view.findViewById(R.id.tv_group_name);
        tvGroupDescription = view.findViewById(R.id.tv_group_description);
        tvMemberCount = view.findViewById(R.id.tv_member_count);
        llMemberAvatars = view.findViewById(R.id.ll_member_avatars);
        tvChallengeName = view.findViewById(R.id.tv_challenge_name);
        progressChallenge = view.findViewById(R.id.progress_challenge);
        tvChallengeProgress = view.findViewById(R.id.tv_challenge_progress);
        cardEmptyFeed = view.findViewById(R.id.card_empty_feed);
        rvFeed = view.findViewById(R.id.rv_feed);

        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);

        // 초기 값 설정
        tvGroupName.setText(groupName != null ? groupName : "그룹");
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnMenu.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        
        // 기본 메뉴 항목
        popup.getMenu().add(0, 1, 0, "🔔 알림 설정");
        
        // 방장만 볼 수 있는 메뉴
        if ("LEADER".equals(userRole)) {
            popup.getMenu().add(0, 2, 1, "⚙️ 그룹 설정");
            popup.getMenu().add(0, 3, 2, "👥 멤버 관리");
            popup.getMenu().add(0, 4, 3, "🗑️ 그룹 삭제");
        }
        
        // 모든 멤버가 볼 수 있는 메뉴
        popup.getMenu().add(0, 5, 4, "🚪 그룹 나가기");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: // 알림 설정
                    Toast.makeText(getContext(), "알림 설정 기능 준비 중", Toast.LENGTH_SHORT).show();
                    return true;
                case 2: // 그룹 설정
                    openGroupSettings();
                    return true;
                case 3: // 멤버 관리
                    Toast.makeText(getContext(), "멤버 관리 기능 준비 중", Toast.LENGTH_SHORT).show();
                    return true;
                case 4: // 그룹 삭제
                    confirmDeleteGroup();
                    return true;
                case 5: // 그룹 나가기
                    confirmLeaveGroup();
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    private void openGroupSettings() {
        // TODO: 그룹 설정 다이얼로그 또는 화면 열기
        Toast.makeText(getContext(), "그룹 설정 기능 준비 중", Toast.LENGTH_SHORT).show();
    }

    private void confirmLeaveGroup() {
        new AlertDialog.Builder(getContext())
                .setTitle("그룹 나가기")
                .setMessage("정말 이 그룹에서 나가시겠습니까?")
                .setPositiveButton("나가기", (dialog, which) -> leaveGroup())
                .setNegativeButton("취소", null)
                .show();
    }

    private void leaveGroup() {
        apiService.leaveGroup(groupId).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, 
                                   Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "그룹에서 나갔습니다", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                } else {
                    Toast.makeText(getContext(), "그룹 나가기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteGroup() {
        new AlertDialog.Builder(getContext())
                .setTitle("그룹 삭제")
                .setMessage("정말 이 그룹을 삭제하시겠습니까?\n모든 멤버가 그룹에서 제외됩니다.")
                .setPositiveButton("삭제", (dialog, which) -> deleteGroup())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deleteGroup() {
        apiService.deleteGroup(groupId).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call, 
                                   Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "그룹이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                } else {
                    Toast.makeText(getContext(), "그룹 삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupDetail() {
        if (groupId == null) {
            Toast.makeText(getContext(), "그룹 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getGroupDetail(groupId).enqueue(new Callback<ApiResponse<GroupDetailResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<GroupDetailResponse>> call, 
                                   Response<ApiResponse<GroupDetailResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateUI(response.body().getData());
                } else {
                    Toast.makeText(getContext(), "그룹 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<GroupDetailResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(GroupDetailResponse group) {
        if (group == null) return;

        // 사용자 역할 저장
        userRole = group.getUserRole() != null ? group.getUserRole() : "NONE";

        tvGroupName.setText(group.getName());
        tvGroupDescription.setText(group.getDescription() != null ? group.getDescription() : "");
        
        // 멤버 수
        int memberCount = group.getMembers() != null ? group.getMembers().size() : 0;
        tvMemberCount.setText(" 멤버 (" + memberCount + "명)");

        // 멤버 아바타 추가
        updateMemberAvatars(group.getMembers());

        // 챌린지 정보
        tvChallengeName.setText(group.getChallenge() != null ? group.getChallenge() : "진행 중인 챌린지 없음");
        progressChallenge.setProgress(group.getProgress());
        tvChallengeProgress.setText(group.getProgress() + "% 달성");

        // 피드 로드
        loadGroupFeeds();
    }

    private void loadGroupFeeds() {
        apiService.getGroupFeeds(groupId, 0, 20).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                                   Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // TODO: 피드 어댑터에 데이터 설정
                    List<?> feeds = (List<?>) response.body().getData().get("feeds");
                    if (feeds != null && !feeds.isEmpty()) {
                        cardEmptyFeed.setVisibility(View.GONE);
                        rvFeed.setVisibility(View.VISIBLE);
                        // TODO: 피드 어댑터 구현 후 연결
                    } else {
                        cardEmptyFeed.setVisibility(View.VISIBLE);
                        rvFeed.setVisibility(View.GONE);
                    }
                } else {
                    cardEmptyFeed.setVisibility(View.VISIBLE);
                    rvFeed.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                cardEmptyFeed.setVisibility(View.VISIBLE);
                rvFeed.setVisibility(View.GONE);
            }
        });
    }

    private void updateMemberAvatars(List<GroupMemberResponse> members) {
        llMemberAvatars.removeAllViews();

        if (members == null || members.isEmpty()) return;

        int maxDisplay = Math.min(members.size(), 5);
        int avatarSize = (int) (40 * getResources().getDisplayMetrics().density);
        int margin = (int) (-10 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < maxDisplay; i++) {
            GroupMemberResponse member = members.get(i);
            
            ImageView avatar = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avatarSize, avatarSize);
            if (i > 0) {
                params.setMarginStart(margin);
            }
            avatar.setLayoutParams(params);
            avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            avatar.setBackgroundResource(R.drawable.bg_profile_image);
            avatar.setClipToOutline(true);

            // 프로필 이미지 로드
            if (member.getProfileImageUrl() != null && !member.getProfileImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(member.getProfileImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(avatar);
            } else {
                avatar.setImageResource(R.drawable.ic_launcher_foreground);
            }

            llMemberAvatars.addView(avatar);
        }

        // 5명 이상인 경우 +N 표시
        if (members.size() > 5) {
            TextView tvMore = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginStart((int) (8 * getResources().getDisplayMetrics().density));
            tvMore.setLayoutParams(params);
            tvMore.setText("+" + (members.size() - 5));
            tvMore.setTextColor(getResources().getColor(R.color.gray_subtext, null));
            tvMore.setTextSize(14);
            llMemberAvatars.addView(tvMore);
        }
    }
}
