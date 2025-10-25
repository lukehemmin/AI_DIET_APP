package com.lukehemmin.ai_diet_app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class NutritionCircleView extends View {

    private Paint paintCarbs;
    private Paint paintProtein;
    private Paint paintFat;
    private Paint paintBackground;

    private RectF oval;

    private float carbsPercentage = 0f;
    private float proteinPercentage = 0f;
    private float fatPercentage = 0f;

    private static final float STROKE_WIDTH = 40f;

    public NutritionCircleView(Context context) {
        super(context);
        init();
    }

    public NutritionCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NutritionCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 탄수화물 (파랑)
        paintCarbs = new Paint();
        paintCarbs.setColor(0xFF007AFF);
        paintCarbs.setStyle(Paint.Style.STROKE);
        paintCarbs.setStrokeWidth(STROKE_WIDTH);
        paintCarbs.setStrokeCap(Paint.Cap.ROUND);
        paintCarbs.setAntiAlias(true);

        // 단백질 (초록)
        paintProtein = new Paint();
        paintProtein.setColor(0xFF34C759);
        paintProtein.setStyle(Paint.Style.STROKE);
        paintProtein.setStrokeWidth(STROKE_WIDTH);
        paintProtein.setStrokeCap(Paint.Cap.ROUND);
        paintProtein.setAntiAlias(true);

        // 지방 (주황)
        paintFat = new Paint();
        paintFat.setColor(0xFFFF9500);
        paintFat.setStyle(Paint.Style.STROKE);
        paintFat.setStrokeWidth(STROKE_WIDTH);
        paintFat.setStrokeCap(Paint.Cap.ROUND);
        paintFat.setAntiAlias(true);

        // 배경 (회색)
        paintBackground = new Paint();
        paintBackground.setColor(0xFFE0E0E0);
        paintBackground.setStyle(Paint.Style.STROKE);
        paintBackground.setStrokeWidth(STROKE_WIDTH);
        paintBackground.setAntiAlias(true);

        oval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);

        float padding = STROKE_WIDTH / 2 + 10;
        oval.set(padding, padding, size - padding, size - padding);

        // 배경 원 그리기
        canvas.drawCircle(size / 2f, size / 2f, (size - STROKE_WIDTH) / 2f, paintBackground);

        // 총 퍼센트 계산
        float total = carbsPercentage + proteinPercentage + fatPercentage;
        if (total == 0) return;

        // 시작 각도 (12시 방향부터)
        float startAngle = -90f;

        // 탄수화물 그리기
        if (carbsPercentage > 0) {
            float sweepAngle = (carbsPercentage / total) * 360f;
            canvas.drawArc(oval, startAngle, sweepAngle, false, paintCarbs);
            startAngle += sweepAngle;
        }

        // 단백질 그리기
        if (proteinPercentage > 0) {
            float sweepAngle = (proteinPercentage / total) * 360f;
            canvas.drawArc(oval, startAngle, sweepAngle, false, paintProtein);
            startAngle += sweepAngle;
        }

        // 지방 그리기
        if (fatPercentage > 0) {
            float sweepAngle = (fatPercentage / total) * 360f;
            canvas.drawArc(oval, startAngle, sweepAngle, false, paintFat);
        }
    }

    public void setNutritionData(float carbs, float protein, float fat) {
        this.carbsPercentage = carbs;
        this.proteinPercentage = protein;
        this.fatPercentage = fat;
        invalidate();
    }
}
