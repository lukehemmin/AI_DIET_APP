package com.lukehemmin.ai_diet_app.data.model;

public enum ActivityLevel {
    SEDENTARY("SEDENTARY", "활동 적음 (거의 운동하지 않음)"),
    LIGHT("LIGHT", "가벼운 활동 (주 1-3회 가벼운 운동)"),
    MODERATE("MODERATE", "보통 활동 (주 3-5회 보통 수준의 운동)"),
    ACTIVE("ACTIVE", "활동적 (주 6-7회 강도 높은 운동)"),
    VERY_ACTIVE("VERY_ACTIVE", "매우 활동적 (매일 힘든 운동/육체 노동)");

    private final String serverValue;
    private final String displayValue;

    ActivityLevel(String serverValue, String displayValue) {
        this.serverValue = serverValue;
        this.displayValue = displayValue;
    }

    public String getServerValue() {
        return serverValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }

    public static ActivityLevel fromServerValue(String value) {
        for (ActivityLevel level : values()) {
            if (level.serverValue.equals(value)) {
                return level;
            }
        }
        return SEDENTARY; // Default
    }
}
