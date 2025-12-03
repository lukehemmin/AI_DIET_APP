package com.lukehemmin.dodietapi.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeDefinition {
    FIRST_MEAL("first_meal", "시작이 반이다", "첫 식단을 기록했어요!", "medal_bronze"),
    THREE_DAY_STREAK("three_day_streak", "작심삼일 극복", "3일 연속으로 기록했어요!", "medal_silver"),
    SEVEN_DAY_STREAK("seven_day_streak", "일주일의 기적", "7일 연속으로 기록했어요!", "medal_gold"),
    MEAL_MASTER("meal_master", "식단 마스터", "총 100끼를 기록했어요!", "trophy");

    private final String id;
    private final String name;
    private final String description;
    private final String icon;
}
