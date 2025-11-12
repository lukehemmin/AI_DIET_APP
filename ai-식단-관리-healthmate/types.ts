

import type { LucideIcon } from 'lucide-react';

export interface Nutrients {
  carbs: number;
  protein: number;
  fat: number;
}

export type MealTime = '아침' | '점심' | '저녁' | '간식' | '야식';

export interface Meal {
  id: number;
  foodItem: string;
  servingSize: number;
  kcal: number;
  macro: Nutrients;
  time: MealTime;
  date: Date;
  image?: string;
}

export interface AnalysisResult {
  foodItem: string;
  servingSize: number;
  kcal: number;
  macro: Nutrients;
}

export type Page = 'home' | 'analysis' | 'chat' | 'groups' | 'profile';

export type ActivityLevel = 'sedentary' | 'light' | 'moderate' | 'active' | 'veryActive';

export interface UserProfile {
    gender: 'male' | 'female';
    age: number;
    height: number;
    weight: number;
    activityLevel: ActivityLevel;
    unlockedBadgeIds: string[];
}

export interface Challenge {
  id: string;
  title: string;
  description: string;
  icon: LucideIcon;
  goal: number; // e.g., 3 days, 7 days
}

export interface ChallengeProgress extends Challenge {
  current: number;
  progress: number;
  status: '진행중' | '완료';
}

export interface AIChallenge {
  title: string;
  description: string;
  icon: 'Zap' | 'Target' | 'Award';
}

export interface Friend {
  id: number;
  name: string;
  avatar: string;
}

export interface Group {
  id: number;
  name: string;
  description: string;
  members: number;
  challenge: string;
  progress: number;
}

export interface GroupFeedItem {
  id: number;
  groupId: number; // Which group this feed belongs to
  type: 'meal' | 'challenge';
  userName: string;
  content: string;
  timestamp: Date;
  likes: number;
  likedByMe: boolean;
}

export type DateRange = '7d' | '30d';

export interface Badge {
  id: string;
  name: string;
  description: string;
  icon: LucideIcon;
}

export interface Recipe {
    name: string;
    kcal: number;
    ingredients: string[];
    instructions: string[];
}

export interface ProteinFoodSuggestion {
  name: string;
  reason: string;
}

export interface ChatMessage {
  role: 'user' | 'model';
  text: string;
}

export interface PlannedMeal {
  name: string;
  kcal: number;
  description: string;
}

export interface MealPlan {
  breakfast: PlannedMeal;
  lunch: PlannedMeal;
  dinner: PlannedMeal;
  snacks?: PlannedMeal;
  totalKcal: number;
}

export type Theme = 'light' | 'dark' | 'system';