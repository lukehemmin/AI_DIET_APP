package com.lukehemmin.ai_diet_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.lukehemmin.ai_diet_app.fragments.AnalysisFragment;
import com.lukehemmin.ai_diet_app.fragments.ChatFragment;
import com.lukehemmin.ai_diet_app.fragments.GroupsFragment;
import com.lukehemmin.ai_diet_app.fragments.HomeFragment;
import com.lukehemmin.ai_diet_app.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navAnalysis, navChat, navGroups, navProfile;
    private Fragment homeFragment, analysisFragment, chatFragment, groupsFragment, profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeBottomNavigation();
        
        // Load initial fragment
        if (savedInstanceState == null) {
            homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, homeFragment)
                    .commit();
            activeFragment = homeFragment;
            setActiveNavItem(navHome);
        }
    }

    private void initializeBottomNavigation() {
        navHome = findViewById(R.id.nav_home);
        navAnalysis = findViewById(R.id.nav_analysis);
        navChat = findViewById(R.id.nav_chat);
        navGroups = findViewById(R.id.nav_groups);
        navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            if (homeFragment == null) homeFragment = new HomeFragment();
            showFragment(homeFragment);
            setActiveNavItem(navHome);
        });

        navAnalysis.setOnClickListener(v -> {
            if (analysisFragment == null) analysisFragment = new AnalysisFragment();
            showFragment(analysisFragment);
            setActiveNavItem(navAnalysis);
        });

        navChat.setOnClickListener(v -> {
            if (chatFragment == null) chatFragment = new ChatFragment();
            showFragment(chatFragment);
            setActiveNavItem(navChat);
        });

        navGroups.setOnClickListener(v -> {
            if (groupsFragment == null) groupsFragment = new GroupsFragment();
            showFragment(groupsFragment);
            setActiveNavItem(navGroups);
        });

        navProfile.setOnClickListener(v -> {
            if (profileFragment == null) profileFragment = new ProfileFragment();
            showFragment(profileFragment);
            setActiveNavItem(navProfile);
        });
    }

    private void showFragment(Fragment fragment) {
        if (fragment == activeFragment) return;

        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (!fragment.isAdded()) {
            transaction.add(R.id.fragment_container, fragment);
        } else {
            transaction.show(fragment);
        }

        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        transaction.commit();
        activeFragment = fragment;
    }

    private void setActiveNavItem(LinearLayout activeNav) {
        // Reset all navigation items
        navHome.setSelected(false);
        navAnalysis.setSelected(false);
        navChat.setSelected(false);
        navGroups.setSelected(false);
        navProfile.setSelected(false);
        
        // Set active state
        activeNav.setSelected(true);
    }

    private int getNavLabelId(LinearLayout nav) {
        if (nav == navHome) return R.id.nav_home_label;
        if (nav == navAnalysis) return R.id.nav_analysis_label;
        if (nav == navChat) return R.id.nav_chat_label;
        if (nav == navGroups) return R.id.nav_groups_label;
        if (nav == navProfile) return R.id.nav_profile_label;
        return 0;
    }

    private int getNavIconId(LinearLayout nav) {
        if (nav == navHome) return R.id.nav_home_icon;
        if (nav == navAnalysis) return R.id.nav_analysis_icon;
        if (nav == navChat) return R.id.nav_chat_icon;
        if (nav == navGroups) return R.id.nav_groups_icon;
        if (nav == navProfile) return R.id.nav_profile_icon;
        return 0;
    }
}