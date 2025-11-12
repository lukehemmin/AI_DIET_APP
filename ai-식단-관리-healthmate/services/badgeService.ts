import { Utensils, Star, Droplets } from 'lucide-react';
import type { Badge, Meal, ChallengeProgress } from '../types';
import { DAILY_WATER_GOAL } from '../constants';


export const allBadges: Badge[] = [
  { id: 'first_meal', name: '첫 걸음', description: '첫 식단을 기록했습니다.', icon: Utensils },
  { id: 'first_challenge', name: '챌린지 완료!', description: '첫 챌린지를 완료했습니다.', icon: Star },
  { id: 'water_master', name: '수분 마스터', description: '하루 수분 섭취 목표를 처음으로 달성했습니다.', icon: Droplets },
];

export const checkNewBadges = (
  meals: Meal[],
  challenges: ChallengeProgress[],
  waterIntakeToday: number,
  unlockedBadgeIds: string[]
): string[] => {
  const newlyUnlocked: string[] = [];
  const dailyWaterGoal = DAILY_WATER_GOAL;

  // Check for 'first_meal'
  if (meals.length > 0 && !unlockedBadgeIds.includes('first_meal')) {
    newlyUnlocked.push('first_meal');
  }

  // Check for 'first_challenge'
  if (challenges.some(c => c.status === '완료') && !unlockedBadgeIds.includes('first_challenge')) {
    newlyUnlocked.push('first_challenge');
  }
  
  // Check for 'water_master'
  if (waterIntakeToday >= dailyWaterGoal && !unlockedBadgeIds.includes('water_master')) {
    newlyUnlocked.push('water_master');
  }

  return newlyUnlocked;
};
