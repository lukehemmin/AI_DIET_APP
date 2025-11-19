package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lukehemmin.ai_diet_app.R;

public class AnalysisChallengeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analysis_challenge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Challenge 1
        View challenge1 = view.findViewById(R.id.challenge_item_1);
        TextView title1 = challenge1.findViewById(R.id.challenge_title);
        TextView desc1 = challenge1.findViewById(R.id.challenge_description);
        title1.setText("단백질 섭취 챌린지");
        desc1.setText("최근 식사 기록 3일간 단백질 목표 달성 (0/3일)");
        ImageView icon1 = challenge1.findViewById(R.id.challenge_icon);
        icon1.setImageResource(android.R.drawable.ic_menu_compass);

        // Challenge 2
        View challenge2 = view.findViewById(R.id.challenge_item_2);
        TextView title2 = challenge2.findViewById(R.id.challenge_title);
        TextView desc2 = challenge2.findViewById(R.id.challenge_description);
        title2.setText("아침 식사 챙기기");
        desc2.setText("최근 7일간 아침 식사 기록 (0/7일)");
        ImageView icon2 = challenge2.findViewById(R.id.challenge_icon);
        icon2.setImageResource(android.R.drawable.ic_menu_agenda);

        // Challenge 3
        View challenge3 = view.findViewById(R.id.challenge_item_3);
        TextView title3 = challenge3.findViewById(R.id.challenge_title);
        TextView desc3 = challenge3.findViewById(R.id.challenge_description);
        title3.setText("주간 칼로리 목표");
        desc3.setText("최근 7일간 목표 칼로리 범위 유지 (0/7일)");
        ImageView icon3 = challenge3.findViewById(R.id.challenge_icon);
        icon3.setImageResource(android.R.drawable.ic_menu_myplaces);
    }
}
