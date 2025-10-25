package com.lukehemmin.ai_diet_app;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TextInputBottomSheet extends BottomSheetDialogFragment {

    private ViewFlipper viewFlipper;
    private TextView tvSelectedMealTime;
    private EditText etFoodName;
    private EditText etMemo;

    // Step 1 버튼들
    private Button btnBreakfast;
    private Button btnLunch;
    private Button btnDinner;
    private Button btnSnack;

    // Step 2 버튼들
    private Button btnBack;
    private Button btnComplete;

    // 선택된 식사 시간
    private String selectedMealTime = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_text_input, container, false);

        initViews(view);
        setupListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bottom Sheet의 높이를 컨텐츠에 맞게 조정
        view.post(() -> {
            View parent = (View) view.getParent();
            if (parent != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);

                // 컨텐츠 높이 측정
                view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                ViewGroup.LayoutParams params = parent.getLayoutParams();
                params.height = view.getMeasuredHeight();
                parent.setLayoutParams(params);
            }
        });
    }

    private void initViews(View view) {
        viewFlipper = view.findViewById(R.id.viewFlipper);

        // Step 1 뷰들
        btnBreakfast = view.findViewById(R.id.btnBreakfast);
        btnLunch = view.findViewById(R.id.btnLunch);
        btnDinner = view.findViewById(R.id.btnDinner);
        btnSnack = view.findViewById(R.id.btnSnack);

        // Step 2 뷰들
        tvSelectedMealTime = view.findViewById(R.id.tvSelectedMealTime);
        etFoodName = view.findViewById(R.id.etFoodName);
        etMemo = view.findViewById(R.id.etMemo);
        btnBack = view.findViewById(R.id.btnBack);
        btnComplete = view.findViewById(R.id.btnComplete);
    }

    private void setupListeners() {
        // Step 1: 식사 시간 선택 버튼들
        btnBreakfast.setOnClickListener(v -> selectMealTime("아침"));
        btnLunch.setOnClickListener(v -> selectMealTime("점심"));
        btnDinner.setOnClickListener(v -> selectMealTime("저녁"));
        btnSnack.setOnClickListener(v -> selectMealTime("간식"));

        // Step 2: 뒤로가기 버튼
        btnBack.setOnClickListener(v -> {
            viewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right);
            viewFlipper.showPrevious();
        });

        // Step 2: 완료 버튼
        btnComplete.setOnClickListener(v -> {
            String foodName = etFoodName.getText().toString().trim();
            String memo = etMemo.getText().toString().trim();

            if (foodName.isEmpty()) {
                Toast.makeText(requireContext(), "음식 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                etFoodName.requestFocus();
                return;
            }

            // 음식 텍스트 생성
            String foodText = foodName;
            if (!memo.isEmpty()) {
                foodText += " (" + memo + ")";
            }

            // MainActivity의 메소드를 통해 FoodAnalysisActivity 시작
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).startFoodAnalysisWithText(foodText, selectedMealTime);
            }

            dismiss();
        });
    }

    private void selectMealTime(String mealTime) {
        selectedMealTime = mealTime;
        tvSelectedMealTime.setText("✓ " + mealTime);

        // Step 2로 전환
        viewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left);
        viewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right);
        viewFlipper.showNext();

        // 키보드 자동으로 띄우기
        etFoodName.requestFocus();
    }
}
