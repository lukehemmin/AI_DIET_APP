import type { Meal, Challenge, ChallengeProgress } from '../types';
import { DAILY_PROTEIN_GOAL } from '../constants';
import { Zap, Target, Award } from 'lucide-react';

const challenges: Challenge[] = [
  { id: 'protein', title: '단백질 섭취 챌린지', description: '최근 식사 기록 3일간 단백질 목표 달성', icon: Target, goal: 3 },
  { id: 'breakfast', title: '아침 식사 챙기기', description: '최근 7일간 아침 식사 기록', icon: Zap, goal: 7 },
  { id: 'calorie', title: '주간 칼로리 목표', description: '최근 7일간 목표 칼로리 범위 유지', icon: Award, goal: 7 },
];

export const calculateChallengesProgress = (meals: Meal[], dailyCalorieGoal: number): ChallengeProgress[] => {
  const today = new Date();
  today.setHours(23, 59, 59, 999); // Set to end of today for correct comparison

  // Protein Challenge Logic: Checks the last 3 days that have meal records.
  const proteinProgress = (): { current: number } => {
    const dailyProtein: { [key: string]: number } = {};
    meals.forEach(meal => {
        const day = new Date(meal.date).toDateString();
        if (!dailyProtein[day]) dailyProtein[day] = 0;
        dailyProtein[day] += meal.macro.protein;
    });

    const recordedDays = Object.keys(dailyProtein)
      .sort((a, b) => new Date(b).getTime() - new Date(a).getTime());
    
    const recentDaysToCheck = recordedDays.slice(0, 3);
    
    const successfulDays = recentDaysToCheck.filter(day => dailyProtein[day] >= DAILY_PROTEIN_GOAL).length;
    
    return { current: successfulDays };
  };
  
  // Breakfast Challenge Logic: Checks how many of the last 7 calendar days have a breakfast record.
  const breakfastProgress = (): { current: number } => {
    const breakfastDays = new Set<string>();
    const sevenDaysAgo = new Date(today);
    sevenDaysAgo.setDate(today.getDate() - 6);
    sevenDaysAgo.setHours(0, 0, 0, 0);
    
    meals.forEach(meal => {
        const mealDate = new Date(meal.date);
        if (meal.time === '아침' && mealDate >= sevenDaysAgo && mealDate <= today) {
            breakfastDays.add(mealDate.toDateString());
        }
    });
    return { current: breakfastDays.size };
  };

  // Calorie Goal Challenge Logic: Checks how many of the last 7 calendar days were within the calorie goal range.
  const calorieProgress = (): { current: number } => {
    const dailyKcal: { [key: string]: number } = {};
    const sevenDaysAgo = new Date(today);
    sevenDaysAgo.setDate(today.getDate() - 6);
    sevenDaysAgo.setHours(0, 0, 0, 0);

    const relevantMeals = meals.filter(m => new Date(m.date) >= sevenDaysAgo && new Date(m.date) <= today);

    relevantMeals.forEach(meal => {
        const day = new Date(meal.date).toDateString();
        if (!dailyKcal[day]) dailyKcal[day] = 0;
        dailyKcal[day] += meal.kcal;
    });

    const successfulDays = Object.values(dailyKcal).filter(kcal => 
        kcal >= dailyCalorieGoal * 0.9 && kcal <= dailyCalorieGoal * 1.1
    ).length;
    
    return { current: successfulDays };
  };

  const progressCalculators: { [key: string]: () => { current: number } } = {
    protein: proteinProgress,
    breakfast: breakfastProgress,
    calorie: calorieProgress,
  };

  return challenges.map(challenge => {
    const { current } = progressCalculators[challenge.id] ? progressCalculators[challenge.id]() : { current: 0 };
    const progress = Math.min(Math.round((current / challenge.goal) * 100), 100);
    const status = progress >= 100 ? '완료' : '진행중';
    
    return { ...challenge, current, progress, status };
  });
};