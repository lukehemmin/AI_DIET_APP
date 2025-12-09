package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lukehemmin.ai_diet_app.R;

public class CreateGroupBottomSheet extends BottomSheetDialogFragment {

    private EditText etGroupName;
    private EditText etGroupDescription;
    private EditText etGroupGoal;
    private AppCompatButton btnAiChallenge;
    private ImageView btnClose;

    private OnGroupCreateListener listener;

    public interface OnGroupCreateListener {
        void onAiChallengeRequested(String groupName, String description, String goal);
    }

    public void setOnGroupCreateListener(OnGroupCreateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_group, container, false);
        
        initializeViews(view);
        setupListeners();
        
        return view;
    }

    private void initializeViews(View view) {
        etGroupName = view.findViewById(R.id.et_group_name);
        etGroupDescription = view.findViewById(R.id.et_group_description);
        etGroupGoal = view.findViewById(R.id.et_group_goal);
        btnAiChallenge = view.findViewById(R.id.btn_ai_challenge);
        btnClose = view.findViewById(R.id.btn_close);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        
        btnAiChallenge.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String description = etGroupDescription.getText().toString().trim();
            String goal = etGroupGoal.getText().toString().trim();

            if (groupName.isEmpty()) {
                Toast.makeText(getContext(), "그룹 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onAiChallengeRequested(groupName, description, goal);
            }
            dismiss();
        });
    }

    public static CreateGroupBottomSheet newInstance() {
        return new CreateGroupBottomSheet();
    }
}
