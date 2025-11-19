
import React, { useState } from 'react';
import { Sparkles, Bot, PlusCircle, LoaderCircle, Sun, Moon, Utensils, Sandwich } from 'lucide-react';
import type { UserProfile, MealPlan, PlannedMeal } from '../types';
import { getAIMealPlan } from '../services/geminiService';

interface AIPlannerProps {
  userProfile: UserProfile;
  onLogMeal: (foodItem: string) => void;
}

const MealCard: React.FC<{
  title: string,
  icon: React.ElementType,
  meal: PlannedMeal | undefined,
  onLog: (foodItem: string) => void
}> = ({ title, icon: Icon, meal, onLog }) => {
  if (!meal) return null;

  return (
    <div className="bg-gray-90 dark:bg-dark-bg p-4 rounded-lg">
      <div className="flex justify-between items-start">
        <div>
          <h4 className="font-semibold text-gray-text dark:text-dark-text flex items-center mb-1">
            <Icon size={18} className="mr-2 text-primary-blue" />
            {title} - <span className="text-primary-blue ml-1.5">{meal.kcal} kcal</span>
          </h4>
          <p className="text-sm text-gray-text dark:text-dark-text font-bold ml-7">{meal.name}</p>
          <p className="text-xs text-gray-subtext dark:text-dark-subtext ml-7">{meal.description}</p>
        </div>
        <button
          onClick={() => onLog(meal.name)}
          className="text-xs font-semibold text-primary-blue hover:opacity-80 transition-opacity flex items-center"
        >
          <PlusCircle size={14} className="mr-1" />
          기록
        </button>
      </div>
    </div>
  );
};

const SkeletonLoader: React.FC = () => (
  <div className="space-y-3 animate-pulse">
    {[...Array(4)].map((_, i) => (
      <div key={i} className="bg-gray-70 dark:bg-dark-border p-4 rounded-lg">
        <div className="flex items-center space-x-3">
          <div className="w-8 h-8 bg-gray-50 dark:bg-slate-700 rounded-full"></div>
          <div className="flex-1 space-y-2">
            <div className="h-4 bg-gray-50 dark:bg-slate-700 rounded w-1/2"></div>
            <div className="h-3 bg-gray-50 dark:bg-slate-700 rounded w-full"></div>
          </div>
        </div>
      </div>
    ))}
  </div>
);

const AIPlanner: React.FC<AIPlannerProps> = ({ userProfile, onLogMeal }) => {
  const [preferences, setPreferences] = useState('');
  const [mealPlan, setMealPlan] = useState<MealPlan | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleGeneratePlan = async () => {
    setIsLoading(true);
    setError(null);
    setMealPlan(null);
    try {
      const plan = await getAIMealPlan(userProfile, preferences);
      setMealPlan(plan);
    } catch (err) {
      setError(err instanceof Error ? err.message : '계획 생성 중 오류 발생');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card space-y-4">
        <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text flex items-center">
          <Sparkles size={22} className="mr-2 text-primary-blue" />
          AI 하루 식단 계획
        </h2>
        <textarea
          value={preferences}
          onChange={(e) => setPreferences(e.target.value)}
          placeholder="특별히 원하는 식단이 있나요? (예: 고단백, 채식 위주, 20분 내로 조리 가능한 음식)"
          className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition text-sm min-h-[80px]"
        />
        <button
          onClick={handleGeneratePlan}
          disabled={isLoading}
          className="w-full bg-primary-blue text-white py-3 rounded-xl font-semibold hover:bg-blue-600 transition flex items-center justify-center disabled:opacity-75 disabled:cursor-wait"
        >
          {isLoading ? (
            <LoaderCircle size={20} className="mr-2 animate-spin" />
          ) : (
            <Bot size={20} className="mr-2" />
          )}
          {isLoading ? 'AI가 식단을 짜는 중...' : 'AI 플랜 생성하기'}
        </button>
      </div>

      {error && <p className="text-red-500 text-center">{error}</p>}
      
      {isLoading && (
        <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
            <SkeletonLoader />
        </div>
      )}

      {mealPlan && (
        <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card animate-fade-in space-y-4">
            <div className="flex justify-between items-baseline">
                <h3 className="text-lg font-semibold text-gray-text dark:text-dark-text">AI 추천 식단</h3>
                <p className="font-bold text-primary-blue">{mealPlan.totalKcal.toLocaleString()} kcal / 일</p>
            </div>
          <div className="space-y-3">
            <MealCard title="아침" icon={Sun} meal={mealPlan.breakfast} onLog={onLogMeal} />
            <MealCard title="점심" icon={Utensils} meal={mealPlan.lunch} onLog={onLogMeal} />
            <MealCard title="저녁" icon={Moon} meal={mealPlan.dinner} onLog={onLogMeal} />
            <MealCard title="간식" icon={Sandwich} meal={mealPlan.snacks} onLog={onLogMeal} />
          </div>
        </div>
      )}
    </div>
  );
};

export default AIPlanner;