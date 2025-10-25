package com.lukehemmin.ai_diet_app;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CalendarDialog extends DialogFragment {

    private TextView tvMonthYear;
    private LinearLayout calendarGrid;
    private Calendar currentCalendar;
    private Calendar today;

    // 테스트: 기록이 있는 날짜들 (나중에 DB에서 가져올 예정)
    private Set<String> datesWithRecords = new HashSet<>();

    // 날짜 선택 리스너
    public interface OnDateSelectedListener {
        void onDateSelected(Calendar selectedDate);
    }

    private OnDateSelectedListener listener;

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_calendar, container, false);

        currentCalendar = Calendar.getInstance();
        today = Calendar.getInstance();

        // 테스트 데이터: 기록이 있는 날짜들
        initTestData();

        initViews(view);
        setupListeners();
        updateCalendar();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 다이얼로그 배경을 투명하게 설정
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        }
    }

    private void initTestData() {
        // 테스트: 몇 개의 날짜에 기록이 있다고 가정
        Calendar testCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 10월 1, 2, 3, 15, 20일에 기록이 있다고 가정
        testCal.set(2025, Calendar.OCTOBER, 1);
        datesWithRecords.add(sdf.format(testCal.getTime()));

        testCal.set(2025, Calendar.OCTOBER, 2);
        datesWithRecords.add(sdf.format(testCal.getTime()));

        testCal.set(2025, Calendar.OCTOBER, 3);
        datesWithRecords.add(sdf.format(testCal.getTime()));

        testCal.set(2025, Calendar.OCTOBER, 15);
        datesWithRecords.add(sdf.format(testCal.getTime()));

        testCal.set(2025, Calendar.OCTOBER, 20);
        datesWithRecords.add(sdf.format(testCal.getTime()));
    }

    private void initViews(View view) {
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        calendarGrid = view.findViewById(R.id.calendarGrid);

        ImageButton btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        ImageButton btnNextMonth = view.findViewById(R.id.btnNextMonth);
        ImageButton btnClose = view.findViewById(R.id.btnClose);

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void setupListeners() {
        // 리스너는 날짜 클릭 시 처리
    }

    private void updateCalendar() {
        // 월/년 표시 업데이트
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy년 M월", Locale.KOREAN);
        tvMonthYear.setText(monthYearFormat.format(currentCalendar.getTime()));

        // 기존 날짜 그리드 초기화
        calendarGrid.removeAllViews();

        // 달력 그리드 생성
        generateCalendarGrid();
    }

    private void generateCalendarGrid() {
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 = 일요일
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 이전 달로 이동하여 마지막 날 가져오기
        Calendar prevMonth = (Calendar) cal.clone();
        prevMonth.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 총 6주 (42일) 표시
        int totalCells = 42;
        int currentDay = 1;
        int nextMonthDay = 1;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int week = 0; week < 6; week++) {
            LinearLayout weekRow = new LinearLayout(requireContext());
            weekRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            weekRow.setOrientation(LinearLayout.HORIZONTAL);

            for (int day = 0; day < 7; day++) {
                int cellIndex = week * 7 + day;

                View dayView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_calendar_day, weekRow, false);

                TextView tvDay = dayView.findViewById(R.id.tvDay);
                View vRecordIndicator = dayView.findViewById(R.id.vRecordIndicator);

                int displayDay;
                boolean isCurrentMonth = false;
                boolean isToday = false;
                Calendar cellCalendar = Calendar.getInstance();

                // 이전 달 날짜
                if (cellIndex < firstDayOfWeek - 1) {
                    displayDay = daysInPrevMonth - (firstDayOfWeek - 2 - cellIndex);
                    cellCalendar.set(prevMonth.get(Calendar.YEAR),
                            prevMonth.get(Calendar.MONTH),
                            displayDay);
                    tvDay.setTextColor(Color.parseColor("#CCCCCC"));
                }
                // 현재 달 날짜
                else if (currentDay <= daysInMonth) {
                    displayDay = currentDay;
                    cellCalendar.set(cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            displayDay);
                    isCurrentMonth = true;

                    // 오늘인지 확인
                    if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                            displayDay == today.get(Calendar.DAY_OF_MONTH)) {
                        isToday = true;
                        tvDay.setBackgroundResource(R.drawable.bg_calendar_today);
                        tvDay.setTextColor(Color.WHITE);
                    } else {
                        // 요일별 색상 적용
                        if (day == 0) { // 일요일
                            tvDay.setTextColor(Color.parseColor("#FF3B30"));
                        } else if (day == 6) { // 토요일
                            tvDay.setTextColor(Color.parseColor("#007AFF"));
                        } else {
                            tvDay.setTextColor(Color.BLACK);
                        }
                    }

                    currentDay++;
                }
                // 다음 달 날짜
                else {
                    displayDay = nextMonthDay;
                    Calendar nextMonth = (Calendar) cal.clone();
                    nextMonth.add(Calendar.MONTH, 1);
                    cellCalendar.set(nextMonth.get(Calendar.YEAR),
                            nextMonth.get(Calendar.MONTH),
                            displayDay);
                    tvDay.setTextColor(Color.parseColor("#CCCCCC"));
                    nextMonthDay++;
                }

                tvDay.setText(String.valueOf(displayDay));

                // 기록 표시 확인
                String dateKey = sdf.format(cellCalendar.getTime());
                if (datesWithRecords.contains(dateKey)) {
                    vRecordIndicator.setVisibility(View.VISIBLE);
                }

                // 날짜 클릭 리스너
                final Calendar finalCellCalendar = (Calendar) cellCalendar.clone();
                dayView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDateSelected(finalCellCalendar);
                    }
                    dismiss();
                });

                weekRow.addView(dayView);
            }

            calendarGrid.addView(weekRow);
        }
    }
}
