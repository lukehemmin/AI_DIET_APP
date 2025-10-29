package com.lukehemmin.ai_diet_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CalorieTrendChartView extends View {

    private Paint barPaintGreen;
    private Paint barPaintOrange;
    private Paint textPaint;
    private Paint gridPaint;
    
    private List<Float> calorieData;
    private float targetCalorie = 1800f;
    
    private int barCount = 10;
    private float barSpacing;
    private float barWidth;
    private float chartHeight;
    private float chartWidth;
    private float topPadding = 40f;
    private float bottomPadding = 60f;
    private float leftPadding = 50f;
    private float rightPadding = 20f;

    public CalorieTrendChartView(Context context) {
        super(context);
        init();
    }

    public CalorieTrendChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalorieTrendChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 녹색 바 (목표 이하)
        barPaintGreen = new Paint();
        barPaintGreen.setColor(0xFF34C759); // primary_green
        barPaintGreen.setStyle(Paint.Style.FILL);
        barPaintGreen.setAntiAlias(true);

        // 주황색 바 (목표 초과)
        barPaintOrange = new Paint();
        barPaintOrange.setColor(0xFFFF9500); // primary_orange
        barPaintOrange.setStyle(Paint.Style.FILL);
        barPaintOrange.setAntiAlias(true);

        // 텍스트
        textPaint = new Paint();
        textPaint.setColor(0xFF8E8E93); // text_secondary
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 그리드선
        gridPaint = new Paint();
        gridPaint.setColor(0xFFF2F2F7); // background_light
        gridPaint.setStrokeWidth(2f);
        gridPaint.setAntiAlias(true);

        // 테스트 데이터
        calorieData = new ArrayList<>();
        calorieData.add(1800f);
        calorieData.add(1850f);
        calorieData.add(1900f);
        calorieData.add(2000f);
        calorieData.add(1780f);
        calorieData.add(1850f);
        calorieData.add(1900f);
        calorieData.add(1950f);
        calorieData.add(1800f);
        calorieData.add(1900f);
    }

    public void setChartData(List<Float> data, float target) {
        this.calorieData = new ArrayList<>(data);
        this.targetCalorie = target;
        invalidate(); // 다시 그리기
    }

    public void setChartData(List<Float> data) {
        setChartData(data, this.targetCalorie);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        chartWidth = w - leftPadding - rightPadding;
        chartHeight = h - topPadding - bottomPadding;
        
        barSpacing = chartWidth / (barCount * 2);
        barWidth = barSpacing * 0.7f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (calorieData == null || calorieData.isEmpty()) {
            return;
        }

        // 최대/최소값 계산
        float maxCalorie = targetCalorie * 1.2f;
        float minCalorie = targetCalorie * 0.85f;

        // Y축 그리드 및 레이블 그리기
        drawYAxisGridAndLabels(canvas, minCalorie, maxCalorie);

        // 바 그리기
        for (int i = 0; i < Math.min(barCount, calorieData.size()); i++) {
            float calorie = calorieData.get(i);
            float normalizedHeight = (calorie - minCalorie) / (maxCalorie - minCalorie);
            float barHeight = normalizedHeight * chartHeight;

            float left = leftPadding + (i * 2 + 0.65f) * barSpacing;
            float top = topPadding + chartHeight - barHeight;
            float right = left + barWidth;
            float bottom = topPadding + chartHeight;

            // 바 그리기 (목표 이하는 녹색, 초과는 주황색)
            Paint barPaint = calorie <= targetCalorie ? barPaintGreen : barPaintOrange;
            
            RectF rect = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rect, 8f, 8f, barPaint);

            // X축 레이블 (날짜)
            float labelX = left + barWidth / 2;
            float labelY = bottom + 40f;
            canvas.drawText(String.valueOf(i + 1), labelX, labelY, textPaint);
        }
    }

    private void drawYAxisGridAndLabels(Canvas canvas, float minCalorie, float maxCalorie) {
        int gridLineCount = 5;
        float calorieStep = (maxCalorie - minCalorie) / (gridLineCount - 1);

        Paint labelPaint = new Paint(textPaint);
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        labelPaint.setTextSize(22f);

        for (int i = 0; i < gridLineCount; i++) {
            float calorie = minCalorie + (i * calorieStep);
            float y = topPadding + chartHeight - (i * chartHeight / (gridLineCount - 1));

            // 그리드선
            canvas.drawLine(leftPadding, y, leftPadding + chartWidth, y, gridPaint);

            // Y축 레이블
            String label = String.format("%.0f", calorie);
            canvas.drawText(label, leftPadding - 10f, y + 8f, labelPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = 600; // 200dp 정도

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = desiredHeight;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = desiredHeight;
        }

        setMeasuredDimension(width, height);
    }
}
