package com.lukehemmin.ai_diet_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

public class HealthReportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_report);

        findViewById(R.id.btn_close_report).setOnClickListener(v -> finish());
    }
}
