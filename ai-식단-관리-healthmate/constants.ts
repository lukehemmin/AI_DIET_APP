export const DAILY_CALORIE_GOAL = 2000; // Deprecated, will be calculated dynamically
export const DAILY_PROTEIN_GOAL = 50; // in grams
export const DAILY_WATER_GOAL = 8; // in glasses

// TDEE (Total Daily Energy Expenditure) calculation multipliers
export const ACTIVITY_LEVELS = {
  sedentary: 1.2, // 거의 또는 전혀 운동하지 않음
  light: 1.375, // 가벼운 운동 (주 1-3일)
  moderate: 1.55, // 보통 수준의 운동 (주 3-5일)
  active: 1.725, // 활발한 운동 (주 6-7일)
  veryActive: 1.9, // 매우 활발한 운동 (매일, 강도 높음)
};
