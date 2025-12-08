package com.lukehemmin.ai_diet_app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.MealCreateRequest;
import com.lukehemmin.ai_diet_app.data.model.MealResponse;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManualAddBottomSheet extends BottomSheetDialogFragment {

    private EditText etFoodName;
    private Spinner spinnerMealTime;
    private MaterialButton btnAddToRecord;
    private ImageView btnClose;

    private Calendar selectedDate;
    private OnMealAddedListener listener;

    private final String[] mealTimeLabels = {"아침", "점심", "저녁", "간식"};
    private final String[] mealTimeValues = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"};

    public interface OnMealAddedListener {
        void onMealAdded();
    }

    public static ManualAddBottomSheet newInstance(Calendar date) {
        ManualAddBottomSheet bottomSheet = new ManualAddBottomSheet();
        Bundle args = new Bundle();
        args.putLong("date", date.getTimeInMillis());
        bottomSheet.setArguments(args);
        return bottomSheet;
    }

    public void setOnMealAddedListener(OnMealAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        if (getArguments() != null) {
            selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(getArguments().getLong("date"));
        } else {
            selectedDate = Calendar.getInstance();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_manual_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupSpinner();
        setupListeners();
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
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);

                // 배경 투명하게 설정
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        });

        return dialog;
    }

    private void initViews(View view) {
        etFoodName = view.findViewById(R.id.et_food_name);
        spinnerMealTime = view.findViewById(R.id.spinner_meal_time);
        btnAddToRecord = view.findViewById(R.id.btn_add_to_record);
        btnClose = view.findViewById(R.id.btn_close);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                mealTimeLabels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealTime.setAdapter(adapter);

        // 현재 시간에 따라 기본값 설정
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int defaultIndex;
        if (hour >= 6 && hour < 10) {
            defaultIndex = 0; // 아침
        } else if (hour >= 10 && hour < 14) {
            defaultIndex = 1; // 점심
        } else if (hour >= 14 && hour < 20) {
            defaultIndex = 2; // 저녁
        } else {
            defaultIndex = 3; // 간식
        }
        spinnerMealTime.setSelection(defaultIndex);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());

        btnAddToRecord.setOnClickListener(v -> {
            String foodName = etFoodName.getText().toString().trim();
            if (foodName.isEmpty()) {
                Toast.makeText(getContext(), "음식 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveMeal(foodName);
        });
    }

    private void saveMeal(String foodName) {
        btnAddToRecord.setEnabled(false);

        int selectedTimeIndex = spinnerMealTime.getSelectedItemPosition();
        String selectedMealTime = mealTimeValues[selectedTimeIndex];
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());

        List<MealCreateRequest.MealItemRequest> mealRequests = new ArrayList<>();
        mealRequests.add(new MealCreateRequest.MealItemRequest(
                foodName,
                1.0,  // servingSize
                null, // kcal (unknown)
                null, // carbs
                null, // protein
                null, // fat
                selectedMealTime,
                date,
                null  // imageUrl
        ));

        RetrofitClient.getApiService().createMeals(new MealCreateRequest(mealRequests))
                .enqueue(new Callback<ApiResponse<Map<String, List<MealResponse>>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, List<MealResponse>>>> call,
                                           Response<ApiResponse<Map<String, List<MealResponse>>>> response) {
                        btnAddToRecord.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(getContext(), "식단이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onMealAdded();
                            }
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "저장 실패: " + 
                                    (response.body() != null ? response.body().getMessage() : "오류"), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, List<MealResponse>>>> call, Throwable t) {
                        btnAddToRecord.setEnabled(true);
                        Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
