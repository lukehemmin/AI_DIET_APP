package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lukehemmin.ai_diet_app.R;

public class ProfileFragment extends Fragment {

    private TabLayout tabLayout;
    private FrameLayout contentContainer;
    private ImageView btnSettings;
    private TextView btnEditProfile;
    private TextView txtProfileName, txtSinceDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupTabs();
        setupListeners();
        loadProfileData();
        
        // Load initial content
        loadProfileInfoContent();
        
        return view;
    }

    private void initializeViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        contentContainer = view.findViewById(R.id.profile_content_container);
        btnSettings = view.findViewById(R.id.btn_settings);
        txtProfileName = view.findViewById(R.id.txt_profile_name);
        txtSinceDate = view.findViewById(R.id.txt_since_date);
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.profile_info));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.profile_achievements));
        tabLayout.addTab(tabLayout.newTab().setText("친구 (2)"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadProfileInfoContent();
                        break;
                    case 1:
                        // Load achievements content
                        break;
                    case 2:
                        // Load friends content
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void setupListeners() {
        btnSettings.setOnClickListener(v -> openSettings());
    }

    private void loadProfileData() {
        txtProfileName.setText("박상호");
        txtSinceDate.setText("since 2024. 05. 21");
    }

    private void loadProfileInfoContent() {
        View profileInfoView = getLayoutInflater().inflate(R.layout.fragment_profile_info, contentContainer, false);
        contentContainer.removeAllViews();
        contentContainer.addView(profileInfoView);
    }

    private void openSettings() {
        // TODO: Open settings
    }
}
