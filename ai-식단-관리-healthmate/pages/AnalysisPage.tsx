

import React, { useState, useMemo, useEffect, useCallback } from 'react';
import type { Meal, DateRange, ChallengeProgress, AIChallenge, Recipe, UserProfile } from '../types';
import { getAIChallenge, getAIWorkoutPlan, getAIRecipe } from '../services/geminiService';
import AIInsightCard from '../components/AIInsightCard';
import NutrientRadarChart from '../components/charts/NutrientRadarChart';
import MealTimeHeatmap from '../components/charts/MealTimeHeatmap';
import CalorieTrendChart from '../components/charts/CalorieTrendChart';
import ChallengesPage from './ChallengesPage';
import AIExerciseCard from '../components/AIExerciseCard';
import AIRecipeCard from '../components/AIRecipeCard';
import AIPatternAnalysisCard from '../components/AIPatternAnalysisCard';
import AIPlanner from '../components/AIPlanner';
import FridgeRecipeGenerator from '../components/FridgeRecipeGenerator';

interface AnalysisPageProps {
  meals: Meal[];
  dailyTotals: {
    kcal: number;
    carbs: number;
    protein: number;
    fat: number;
  };
  dailyCalorieGoal: number;
  challenges: ChallengeProgress[];
  userProfile: UserProfile;
  onLogMeal: (foodItem: string) => void;
}

const AnalysisPage: React.FC<AnalysisPageProps> = ({ meals, dailyTotals, dailyCalorieGoal, challenges, userProfile, onLogMeal }) => {
  const [dateRange, setDateRange] = useState<DateRange>('7d');
  const [activeTab, setActiveTab] = useState<'analysis' | 'planner' | 'challenges'>('analysis');
  const [aiChallenge, setAiChallenge] = useState<AIChallenge | null>(null);
  const [isFetchingAiChallenge, setIsFetchingAiChallenge] = useState(true);
  
  const [workoutPlan, setWorkoutPlan] = useState<string | null>(null);
  const [isFetchingWorkout, setIsFetchingWorkout] = useState(true);
  const [recipe, setRecipe] = useState<Recipe | null>(null);
  const [isFetchingRecipe, setIsFetchingRecipe] = useState(true);


  const filteredMeals = useMemo(() => {
    const days = dateRange === '7d' ? 7 : 30;
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - days);
    cutoffDate.setHours(0, 0, 0, 0);
    return meals.filter(meal => new Date(meal.date) >= cutoffDate);
  }, [meals, dateRange]);
  
  const aggregatedTotals = useMemo(() => {
    return filteredMeals.reduce((acc, meal) => {
        acc.carbs += meal.macro.carbs;
        acc.protein += meal.macro.protein;
        acc.fat += meal.macro.fat;
        return acc;
      }, { carbs: 0, protein: 0, fat: 0 });
  }, [filteredMeals]);

  const handleFetchAiChallenge = useCallback(async () => {
    setIsFetchingAiChallenge(true);
    try {
      const challenge = await getAIChallenge(meals);
      setAiChallenge(challenge);
    } catch (error) {
      console.error("Failed to fetch AI challenge:", error);
      setAiChallenge({
        title: "오류",
        description: "챌린지를 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.",
        icon: 'Zap',
      });
    } finally {
      setIsFetchingAiChallenge(false);
    }
  }, [meals]);

  const fetchWorkoutPlan = useCallback(async () => {
    setIsFetchingWorkout(true);
    try {
      const plan = await getAIWorkoutPlan(meals, userProfile);
      setWorkoutPlan(plan);
    } catch (error) {
      console.error(error);
      setWorkoutPlan("운동 계획을 불러오는 데 실패했습니다.");
    } finally {
      setIsFetchingWorkout(false);
    }
  }, [meals, userProfile]);

  const fetchRecipe = useCallback(async () => {
    setIsFetchingRecipe(true);
    try {
      const newRecipe = await getAIRecipe(meals);
      setRecipe(newRecipe);
    } catch (error) {
      console.error(error);
      setRecipe(null); // Or set an error state
    } finally {
      setIsFetchingRecipe(false);
    }
  }, [meals]);
  
  useEffect(() => {
    if(activeTab === 'analysis') {
      fetchWorkoutPlan();
      fetchRecipe();
    } else if (activeTab === 'challenges') {
      handleFetchAiChallenge();
    }
  }, [activeTab, handleFetchAiChallenge, fetchWorkoutPlan, fetchRecipe]);


  return (
    <div className="space-y-6 animate-fade-in">
       <div className="flex justify-between items-start">
        <div>
          <h1 className="text-2xl font-bold text-gray-text dark:text-dark-text">분석 및 계획</h1>
          <p className="text-gray-subtext dark:text-dark-subtext">나의 식습관과 성장 과정을 확인해보세요.</p>
        </div>
       </div>

      <div className="border-b border-gray-70 dark:border-dark-border">
          <nav className="-mb-px flex space-x-6">
            <button
              onClick={() => setActiveTab('analysis')}
              className={`py-3 px-1 border-b-2 font-semibold ${
                activeTab === 'analysis'
                  ? 'border-primary-blue text-primary-blue'
                  : 'border-transparent text-gray-subtext dark:text-dark-subtext hover:border-gray-50 dark:hover:border-slate-600'
              }`}
            >
              식단 분석
            </button>
            <button
              onClick={() => setActiveTab('planner')}
              className={`py-3 px-1 border-b-2 font-semibold ${
                activeTab === 'planner'
                  ? 'border-primary-blue text-primary-blue'
                  : 'border-transparent text-gray-subtext dark:text-dark-subtext hover:border-gray-50 dark:hover:border-slate-600'
              }`}
            >
              AI 식단 플래너
            </button>
            <button
              onClick={() => setActiveTab('challenges')}
              className={`py-3 px-1 border-b-2 font-semibold ${
                activeTab === 'challenges'
                  ? 'border-primary-blue text-primary-blue'
                  : 'border-transparent text-gray-subtext dark:text-dark-subtext hover:border-gray-50 dark:hover:border-slate-600'
              }`}
            >
              챌린지 및 업적
            </button>
          </nav>
      </div>
       
      {activeTab === 'analysis' && (
        <div className="space-y-8 animate-fade-in">
          <AIExerciseCard plan={workoutPlan} isLoading={isFetchingWorkout} />
          <AIRecipeCard recipe={recipe} isLoading={isFetchingRecipe} onGenerateNew={fetchRecipe} />
          <FridgeRecipeGenerator userProfile={userProfile} />
          <div className="flex justify-end">
            <div className="flex space-x-1 bg-gray-70 dark:bg-dark-border p-1 rounded-lg">
              <button 
                onClick={() => setDateRange('7d')}
                className={`px-3 py-1 text-sm font-semibold rounded-md transition-colors ${dateRange === '7d' ? 'bg-gray-100 dark:bg-dark-card text-primary-blue' : 'text-gray-subtext dark:text-dark-subtext'}`}
              >
                최근 7일
              </button>
              <button 
                onClick={() => setDateRange('30d')}
                className={`px-3 py-1 text-sm font-semibold rounded-md transition-colors ${dateRange === '30d' ? 'bg-gray-100 dark:bg-dark-card text-primary-blue' : 'text-gray-subtext dark:text-dark-subtext'}`}
              >
                최근 30일
              </button>
            </div>
          </div>
          <CalorieTrendChart meals={filteredMeals} dailyGoal={dailyCalorieGoal} />
          <AIInsightCard meals={filteredMeals} />
          <AIPatternAnalysisCard meals={filteredMeals} dateRange={dateRange} />
          <NutrientRadarChart dailyTotals={aggregatedTotals} />
          <MealTimeHeatmap meals={filteredMeals} />
        </div>
      )}

      {activeTab === 'planner' && (
        <div className="animate-fade-in">
            <AIPlanner userProfile={userProfile} onLogMeal={onLogMeal} />
        </div>
      )}

      {activeTab === 'challenges' && (
        <div className="animate-fade-in">
            <ChallengesPage 
              challenges={challenges}
              aiChallenge={aiChallenge}
              isFetchingAiChallenge={isFetchingAiChallenge}
              onFetchAiChallenge={handleFetchAiChallenge}
            />
        </div>
      )}

    </div>
  );
};

export default AnalysisPage;
