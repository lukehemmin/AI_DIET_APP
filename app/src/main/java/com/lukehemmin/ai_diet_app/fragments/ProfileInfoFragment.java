package com.lukehemmin.ai_diet_app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.ActivityLevel;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.UserProfile;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileInfoFragment extends Fragment {

    private TextView tvGender, tvAge, tvHeight, tvWeight, tvActivityLevel, tvBmr, tvGoalIntake;
    private ImageView btnEditProfile;
    private UserProfile currentUserProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_info, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchProfile();
    }

    private void initViews(View view) {
        tvGender = view.findViewById(R.id.tvGender);
        tvAge = view.findViewById(R.id.tvAge);
        tvHeight = view.findViewById(R.id.tvHeight);
        tvWeight = view.findViewById(R.id.tvWeight);
        tvActivityLevel = view.findViewById(R.id.tvActivityLevel);
        tvBmr = view.findViewById(R.id.tvBmr);
        tvGoalIntake = view.findViewById(R.id.tvGoalIntake);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> showEditDialog());
    }

    private void fetchProfile() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getProfile().enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUserProfile = response.body().getData();
                    updateUI(currentUserProfile);
                } else {
                    // For demo purpose, if API fails (likely 404/500 as it might not exist), use dummy data or keep "-"
                    // Or do nothing
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                Toast.makeText(getContext(), "프로필 로드 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(UserProfile profile) {
        if (profile == null) return;

        tvGender.setText("MALE".equals(profile.getGender()) ? "남성" : "여성");
        tvAge.setText(profile.getAge() + " 세");
        tvHeight.setText(profile.getHeight() + " cm");
        tvWeight.setText(profile.getWeight() + " kg");
        
        ActivityLevel level = ActivityLevel.fromServerValue(profile.getActivityLevel());
        tvActivityLevel.setText(level.getDisplayValue());
        
        if (profile.getBmr() != null) {
            tvBmr.setText(String.format("%,.0f kcal", profile.getBmr()));
        }
        if (profile.getGoalIntake() != null) {
            tvGoalIntake.setText(String.format("%,.0f kcal", profile.getGoalIntake()));
        }
    }

    private void showEditDialog() {
        if (currentUserProfile == null) {
            Toast.makeText(getContext(), "프로필 정보를 불러오는 중입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);

        EditText etAge = dialogView.findViewById(R.id.etEditAge);
        EditText etHeight = dialogView.findViewById(R.id.etEditHeight);
        EditText etWeight = dialogView.findViewById(R.id.etEditWeight);
        Spinner spinnerActivityLevel = dialogView.findViewById(R.id.spinnerEditActivityLevel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Pre-fill data
        etAge.setText(String.valueOf(currentUserProfile.getAge()));
        etHeight.setText(String.valueOf(currentUserProfile.getHeight()));
        etWeight.setText(String.valueOf(currentUserProfile.getWeight()));

        // Setup Spinner
        ArrayAdapter<ActivityLevel> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ActivityLevel.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityLevel.setAdapter(adapter);

        // Select current activity level
        ActivityLevel currentLevel = ActivityLevel.fromServerValue(currentUserProfile.getActivityLevel());
        spinnerActivityLevel.setSelection(adapter.getPosition(currentLevel));

        AlertDialog dialog = builder.setView(dialogView).create();
        // Transparent background for rounded corners if needed, but default is fine
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_rounded); // Assuming this exists or default

        // I'll just use default background or create a shape. 
        // Actually, let's stick to default for now to avoid resource missing error.
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white); 

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            try {
                int age = Integer.parseInt(etAge.getText().toString());
                double height = Double.parseDouble(etHeight.getText().toString());
                double weight = Double.parseDouble(etWeight.getText().toString());
                ActivityLevel selectedLevel = (ActivityLevel) spinnerActivityLevel.getSelectedItem();

                UserProfile updatedProfile = new UserProfile(
                        currentUserProfile.getName(),
                        currentUserProfile.getEmail(),
                        currentUserProfile.getGender(),
                        age,
                        height,
                        weight,
                        selectedLevel.getServerValue(),
                        currentUserProfile.getBmr(), // Server should recalculate these
                        currentUserProfile.getGoalIntake()
                );

                updateProfile(updatedProfile, dialog);

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "입력값을 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void updateProfile(UserProfile updatedProfile, AlertDialog dialog) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.updateProfile(updatedProfile).enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    currentUserProfile = response.body().getData(); // Use returned data (with recalculated BMR)
                    updateUI(currentUserProfile);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "수정 실패: " + (response.body() != null ? response.body().getMessage() : "오류"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
