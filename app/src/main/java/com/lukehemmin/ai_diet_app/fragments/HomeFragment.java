package com.lukehemmin.ai_diet_app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lukehemmin.ai_diet_app.R;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private TextView txtCurrentKcal, txtGoalKcal, txtCurrentDate;
    private TextView txtCarbsLegend, txtProteinLegend, txtFatLegend;

    private TextView txtWaterCount, txtEmptyMeals, btnAddManual;
    private ProgressBar progressCalorie;
    private RecyclerView rvMeals;
    private FloatingActionButton fabAddMeal, fabCamera, fabGallery;
    private TextView txtCameraLabel, txtGalleryLabel;
    private ImageView btnPrevDate, btnNextDate;
    private LinearLayout waterGlassesContainer;
    private PieChart pieChart;
    private boolean isFabOpen = false;

    // Camera & Gallery Launchers
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "사진이 촬영되었습니다.", Toast.LENGTH_SHORT).show();
                    // TODO: 촬영된 이미지 처리 (result.getData())
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    Toast.makeText(getContext(), "사진이 선택되었습니다.", Toast.LENGTH_SHORT).show();
                    // TODO: 선택된 이미지 처리 (selectedImage)
                }
            }
    );

    // Water glass indicators
    private TextView[] waterGlasses;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        initializeViews(view);
        setupListeners(view);
        loadInitialData();
        setupPieChart();
        updatePieChart(0, 0, 0);
        
        return view;
    }

    private void initializeViews(View view) {
        // Dashboard
        txtCurrentKcal = view.findViewById(R.id.txt_current_kcal);
        txtGoalKcal = view.findViewById(R.id.txt_goal_kcal);
        progressCalorie = view.findViewById(R.id.progress_calorie);
        txtCarbsLegend = view.findViewById(R.id.txt_carbs_legend);
        txtProteinLegend = view.findViewById(R.id.txt_protein_legend);
        txtFatLegend = view.findViewById(R.id.txt_fat_legend);
        pieChart = view.findViewById(R.id.pie_chart_nutrients);

        // Header
        txtCurrentDate = view.findViewById(R.id.txt_current_date);
        btnPrevDate = view.findViewById(R.id.btn_prev_date);
        btnNextDate = view.findViewById(R.id.btn_next_date);

        // Water intake
        txtWaterCount = view.findViewById(R.id.txt_water_count);
        btnPrevDate = view.findViewById(R.id.btn_prev_date);
        btnNextDate = view.findViewById(R.id.btn_next_date);
        btnPrevDate = view.findViewById(R.id.btn_prev_date);
        btnNextDate = view.findViewById(R.id.btn_next_date);

        // Water intake
        txtWaterCount = view.findViewById(R.id.txt_water_count);
        waterGlassesContainer = view.findViewById(R.id.water_glasses_container);

        // Meal Log
        rvMeals = view.findViewById(R.id.rv_meals);
        txtEmptyMeals = view.findViewById(R.id.txt_empty_meals);
        btnAddManual = view.findViewById(R.id.btn_add_manual);
        fabAddMeal = view.findViewById(R.id.fab_main);
        fabCamera = view.findViewById(R.id.fab_camera);
        fabGallery = view.findViewById(R.id.fab_gallery);
        txtCameraLabel = view.findViewById(R.id.txt_camera_label);
        txtGalleryLabel = view.findViewById(R.id.txt_gallery_label);

        // Setup RecyclerView
        rvMeals.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initial FAB state
        closeFabMenu();
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.btn_water_minus).setOnClickListener(v -> updateWaterIntake(-1));
        view.findViewById(R.id.btn_water_plus).setOnClickListener(v -> updateWaterIntake(1));
        
        btnAddManual.setOnClickListener(v -> openManualAddModal());
        fabAddMeal.setOnClickListener(v -> {
            if (isFabOpen) {
                closeFabMenu();
            } else {
                showFabMenu();
            }
        });
        
        fabCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                cameraLauncher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "카메라를 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            closeFabMenu();
        });

        fabGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
            closeFabMenu();
        });

        btnPrevDate.setOnClickListener(v -> navigateDate(-1));
        btnNextDate.setOnClickListener(v -> navigateDate(1));
    }

    private void loadInitialData() {
        // Initialize with default values
        txtCurrentKcal.setText("0");
        txtGoalKcal.setText("/ 2,662 kcal");
        progressCalorie.setProgress(0);
        
        txtCarbsLegend.setText("탄수화물 (0%)");
        txtProteinLegend.setText("단백질 (0%)");
        txtFatLegend.setText("지방 (0%)");
        
        txtWaterCount.setText("0");
        updateWaterGlasses(0);
        
        txtCurrentDate.setText("오늘");
        
        // Show empty state
        showEmptyState(true);
        updateCalorieProgress(0, 2662);
    }
    private void updateWaterIntake(int delta) {
        int current = Integer.parseInt(txtWaterCount.getText().toString());
        int newValue = Math.max(0, Math.min(8, current + delta));
        txtWaterCount.setText(String.valueOf(newValue));
        updateWaterGlasses(newValue);
    }

    private void updateWaterGlasses(int count) {
        for (int i = 0; i < waterGlassesContainer.getChildCount(); i++) {
            ImageView glass = (ImageView) waterGlassesContainer.getChildAt(i);
            if (i < count) {
                glass.setColorFilter(getContext().getColor(R.color.primary_blue), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                glass.setColorFilter(getContext().getColor(R.color.gray_70), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }


    private void showEmptyState(boolean show) {
        txtEmptyMeals.setVisibility(show ? View.VISIBLE : View.GONE);
        rvMeals.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void openManualAddModal() {
        // TODO: Implement manual add modal
    }

    private void navigateDate(int delta) {
        // TODO: Implement date navigation
    }

    private void showFabMenu() {
        isFabOpen = true;
        fabAddMeal.setImageResource(R.drawable.ic_plus_circle); // Change to close icon

        fabCamera.setVisibility(View.VISIBLE);
        fabGallery.setVisibility(View.VISIBLE);

        fabCamera.animate().translationY(-getResources().getDimension(R.dimen.fab_margin_1));
        fabGallery.animate().translationY(-getResources().getDimension(R.dimen.fab_margin_2));
    }

    private void closeFabMenu() {
        isFabOpen = false;
        fabAddMeal.setImageResource(R.drawable.ic_plus);

        fabCamera.animate().translationY(0);
        txtCameraLabel.animate().translationY(0).withEndAction(() -> {
            fabCamera.setVisibility(View.GONE);
            txtCameraLabel.setVisibility(View.GONE);
        });

        fabGallery.animate().translationY(0).withEndAction(() -> {
            fabGallery.setVisibility(View.GONE);
            txtGalleryLabel.setVisibility(View.GONE);
        });
    }

    private void updateCalorieProgress(int currentKcal, int goalKcal) {
        if (goalKcal > 0) {
            int progress = (int) ((currentKcal / (float) goalKcal) * 100);
            progressCalorie.setProgress(progress);
        } else {
            progressCalorie.setProgress(0);
        }
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setHoleRadius(58f);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
    }

    private void updatePieChart(float protein, float fat, float carbs) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (protein == 0 && fat == 0 && carbs == 0) {
            entries.add(new PieEntry(1f));
            colors.add(getContext().getColor(R.color.gray_70));
        } else {
            if (protein > 0) {
                entries.add(new PieEntry(protein, "단백질"));
                colors.add(getContext().getColor(R.color.primary_green));
            }
            if (fat > 0) {
                entries.add(new PieEntry(fat, "지방"));
                colors.add(getContext().getColor(R.color.primary_orange));
            }
            if (carbs > 0) {
                entries.add(new PieEntry(carbs, "탄수화물"));
                colors.add(getContext().getColor(R.color.primary_blue));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Nutrients");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(0f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(0f);

        pieChart.setData(data);
        pieChart.invalidate();
    }
}
