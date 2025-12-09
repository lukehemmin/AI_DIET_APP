package com.lukehemmin.dodietapi.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeDefinition {
    // 기록 관련 업적
    FIRST_MEAL("first_meal", "첫 걸음", "첫 식단을 기록했습니다.", "first_step"),
    THREE_DAY_STREAK("three_day_streak", "작심삼일 극복", "3일 연속으로 기록했어요!", "streak"),
    SEVEN_DAY_STREAK("seven_day_streak", "일주일의 기적", "7일 연속으로 기록했어요!", "streak"),
    FOURTEEN_DAY_STREAK("fourteen_day_streak", "2주의 습관", "14일 연속으로 기록했어요!", "streak"),
    THIRTY_DAY_STREAK("thirty_day_streak", "한 달의 기록", "30일 연속으로 기록했어요!", "streak"),
    
    // 식단 마스터 업적
    MEAL_10("meal_10", "식단 기록 10회", "총 10끼를 기록했어요!", "nutrition"),
    MEAL_50("meal_50", "식단 기록 50회", "총 50끼를 기록했어요!", "nutrition"),
    MEAL_MASTER("meal_master", "식단 마스터", "총 100끼를 기록했어요!", "nutrition"),
    
    // 챌린지 관련 업적
    FIRST_CHALLENGE("first_challenge", "챌린지 완료!", "첫 챌린지를 완료했습니다.", "challenge"),
    CHALLENGE_5("challenge_5", "챌린지 마니아", "5개의 챌린지를 완료했어요!", "challenge"),
    
    // 수분 섭취 업적
    WATER_MASTER("water_master", "수분 마스터", "하루 수분 섭취 목표를 처음으로 달성했습니다.", "water"),
    WATER_WEEK("water_week", "물 마시기 습관", "7일 연속 수분 목표 달성!", "water"),
    
    // AI 관련 업적
    AI_FIRST_CHAT("ai_first_chat", "AI와 첫 대화", "AI 영양사와 첫 대화를 나눴어요!", "ai_chat"),
    AI_CHAT_10("ai_chat_10", "AI 단골손님", "AI와 10번 대화를 나눴어요!", "ai_chat"),
    
    // 영양 균형 업적
    NUTRITION_BALANCE("nutrition_balance", "균형 잡힌 식단", "영양소 균형이 맞는 식단을 5번 기록했어요!", "nutrition");

    private final String id;
    private final String name;
    private final String description;
    private final String icon;
}
