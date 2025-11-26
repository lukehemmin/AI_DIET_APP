package com.lukehemmin.ai_diet_app.fragments;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import com.lukehemmin.ai_diet_app.R;

public class AnalysisDietFragment extends Fragment {

    private BarChart barChart;
    private RadarChart radarChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis_diet, container, false);

        barChart = view.findViewById(R.id.chart_calorie_trend);
        radarChart = view.findViewById(R.id.chart_weekly_nutrition);

        setupBarChart();
        updateBarChart();

        setupRadarChart();
        updateRadarChart();

        updateMealTimeTable(view);

        return view;
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.getXAxis().setEnabled(false);
        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
    }

    private void updateBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 1800));
        entries.add(new BarEntry(1, 2200));
        entries.add(new BarEntry(2, 2000));
        entries.add(new BarEntry(3, 2500));
        entries.add(new BarEntry(4, 2300));
        entries.add(new BarEntry(5, 2100));
        entries.add(new BarEntry(6, 2400));

        BarDataSet dataSet = new BarDataSet(entries, "Calorie Intake");
        dataSet.setColor(getContext().getColor(R.color.primary_blue));
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }

    private void setupRadarChart() {
        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.getYAxis().setEnabled(false);
        radarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"탄수화물", "단백질", "지방"}));
    }

    private void updateRadarChart() {
        ArrayList<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(0.8f));
        entries.add(new RadarEntry(0.6f));
        entries.add(new RadarEntry(0.9f));

        RadarDataSet dataSet = new RadarDataSet(entries, "Nutrition Balance");
        dataSet.setColor(getContext().getColor(R.color.primary_green));
        dataSet.setFillColor(getContext().getColor(R.color.primary_green));
        dataSet.setDrawFilled(true);
        dataSet.setLineWidth(2f);

        RadarData radarData = new RadarData(dataSet);
        radarData.setDrawValues(false);
        radarChart.setData(radarData);
        radarChart.invalidate();
    }

    private void updateMealTimeTable(View view) {
        int[][] cellIds = {
            {R.id.cell_b_1, R.id.cell_b_2, R.id.cell_b_3, R.id.cell_b_4, R.id.cell_b_5, R.id.cell_b_6, R.id.cell_b_7},
            {R.id.cell_l_1, R.id.cell_l_2, R.id.cell_l_3, R.id.cell_l_4, R.id.cell_l_5, R.id.cell_l_6, R.id.cell_l_7},
            {R.id.cell_d_1, R.id.cell_d_2, R.id.cell_d_3, R.id.cell_d_4, R.id.cell_d_5, R.id.cell_d_6, R.id.cell_d_7}
        };

        for (int[] row : cellIds) {
            for (int id : row) {
                View cell = view.findViewById(id);
                if (cell != null) {
                    if (Math.random() > 0.5) {
                        cell.setBackgroundColor(getContext().getColor(R.color.primary_blue));
                    }
                }
            }
        }
    }
}
