package com.lukehemmin.ai_diet_app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.UserProfile;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfileFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView btnSettings;
    private ImageView imgProfile;
    private TextView txtProfileName, txtSinceDate;
    
    private ApiService apiService;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupImagePicker();
        setupViewPager();
        setupListeners();
        loadProfileData();
        
        return view;
    }

    private void initializeViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        btnSettings = view.findViewById(R.id.btn_settings);
        imgProfile = view.findViewById(R.id.img_profile);
        txtProfileName = view.findViewById(R.id.txt_profile_name);
        txtSinceDate = view.findViewById(R.id.txt_since_date);
        
        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadProfileImage(imageUri);
                    }
                }
            }
        );
    }

    private void setupViewPager() {
        viewPager.setAdapter(new ProfilePagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.profile_info);
                    break;
                case 1:
                    tab.setText(R.string.profile_achievements);
                    break;
                case 2:
                    tab.setText("친구 (2)");
                    break;
            }
        }).attach();
    }

    private void setupListeners() {
        btnSettings.setOnClickListener(v -> openSettings());
        
        // 프로필 이미지 클릭 시 이미지 선택
        imgProfile.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        try {
            // Uri를 File로 변환
            File file = createTempFileFromUri(imageUri);
            if (file == null) {
                Toast.makeText(getContext(), "이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            apiService.uploadProfileImage(body).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, String>>> call, Response<ApiResponse<Map<String, String>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String newImageUrl = response.body().getData().get("profileImageUrl");
                        loadProfileImage(newImageUrl);
                        Toast.makeText(getContext(), "프로필 사진이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "업로드에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                    Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "이미지 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("profile", ".jpg", getContext().getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty() && getContext() != null) {
            String fullUrl = imageUrl;
            // 상대 경로인 경우 전체 URL로 변환
            if (!imageUrl.startsWith("http")) {
                fullUrl = RetrofitClient.getBaseUrl(getContext()) + "uploads/" + imageUrl;
            }
            
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .circleCrop()
                    .into(imgProfile);
        }
    }

    private void loadProfileData() {
        apiService.getProfile().enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserProfile profile = response.body().getData();
                    updateUI(profile);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                // 실패 시 기본값 유지
            }
        });
    }

    private void updateUI(UserProfile profile) {
        if (profile == null) return;
        
        txtProfileName.setText(profile.getName() != null ? profile.getName() : "사용자");
        
        // 가입일 표시
        if (profile.getCreatedAt() != null) {
            try {
                // 서버에서 받은 날짜 형식 파싱 후 표시 형식으로 변환
                String dateStr = profile.getCreatedAt().split("T")[0]; // "2024-05-21T..."
                txtSinceDate.setText("since " + dateStr.replace("-", ". "));
            } catch (Exception e) {
                txtSinceDate.setText("since " + profile.getCreatedAt());
            }
        }
        
        // 프로필 이미지 로드
        loadProfileImage(profile.getProfileImageUrl());
    }

    private void openSettings() {
        Intent intent = new Intent(getContext(), com.lukehemmin.ai_diet_app.SettingsActivity.class);
        startActivity(intent);
    }

    private static class ProfilePagerAdapter extends FragmentStateAdapter {

        public ProfilePagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ProfileInfoFragment();
                case 1:
                    return new ProfileAchievementsFragment();
                case 2:
                    return new ProfileFriendsFragment();
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
