package com.lukehemmin.ai_diet_app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lukehemmin.ai_diet_app.R;

public class HealthReportBottomSheet extends BottomSheetDialogFragment {

    private TextView txtAiSummary;
    private TextView txtWeeklyAvg;
    private TextView txtMostEaten;
    private TextView txtChallenges;
    private ImageView btnClose;

    public static HealthReportBottomSheet newInstance() {
        return new HealthReportBottomSheet();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_health_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        loadReportData();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                
                // 화면 높이의 90%로 설정
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight((int) (screenHeight * 0.9));
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                
                // 배경 투명하게 설정
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });
        
        return dialog;
    }

    private void initViews(View view) {
        txtAiSummary = view.findViewById(R.id.txt_ai_summary);
        txtWeeklyAvg = view.findViewById(R.id.txt_weekly_avg);
        txtMostEaten = view.findViewById(R.id.txt_most_eaten);
        txtChallenges = view.findViewById(R.id.txt_challenges);
        btnClose = view.findViewById(R.id.btn_close_report);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void loadReportData() {
        // TODO: API에서 실제 데이터 로드
        // 현재는 기본값 표시
        txtAiSummary.setText("지난 한 주도 수고 많으셨습니다! 꾸준히 식단을 기록하며 건강한 습관을 만들어가요. 다음 주도 화이팅!");
        txtWeeklyAvg.setText("0");
        txtMostEaten.setText("기록이 부족해요.");
        txtChallenges.setText("아직 없어요.");
    }
}
