package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.fragments.AnalysisAiRecipeFragment;
import com.lukehemmin.ai_diet_app.fragments.AnalysisChallengeFragment;
import com.lukehemmin.ai_diet_app.fragments.AnalysisDietFragment;

public class AnalysisFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);

        tabLayout = view.findViewById(R.id.tab_layout_analysis);
        viewPager = view.findViewById(R.id.view_pager_analysis);

        AnalysisPagerAdapter adapter = new AnalysisPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("식단분석");
                    break;
                case 1:
                    tab.setText("AI 식단 추천");
                    break;
                case 2:
                    tab.setText("챌린지 및 업적");
                    break;
            }
        }).attach();

        return view;
    }

    private static class AnalysisPagerAdapter extends FragmentStateAdapter {

        public AnalysisPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AnalysisDietFragment();
                case 1:
                    return new AnalysisAiRecipeFragment();
                case 2:
                    return new AnalysisChallengeFragment();
                default:
                    return new Fragment(); // Should not happen
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Number of tabs
        }
    }
}
