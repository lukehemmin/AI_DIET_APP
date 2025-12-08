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

import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfileFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageView btnSettings;
    private TextView txtProfileName, txtSinceDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupViewPager();
        setupListeners();
        loadProfileData();
        
        return view;
    }

    private void initializeViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        btnSettings = view.findViewById(R.id.btn_settings);
        txtProfileName = view.findViewById(R.id.txt_profile_name);
        txtSinceDate = view.findViewById(R.id.txt_since_date);
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
    }

    private void loadProfileData() {
        txtProfileName.setText("박상호");
        txtSinceDate.setText("since 2024. 05. 21");
    }

    private void openSettings() {
        android.content.Intent intent = new android.content.Intent(getContext(), com.lukehemmin.ai_diet_app.SettingsActivity.class);
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
