import React from 'react';
import type { Meal } from '../types';
import Dashboard from '../components/Dashboard';
import SuggestionCard from '../components/SuggestionCard';
import MealLog from '../components/MealLog';
import WaterIntakeCard from '../components/WaterIntakeCard';

interface HomePageProps {
  meals: Meal[];
  dailyTotals: {
    kcal: number;
    carbs: number;
    protein: number;
    fat: number;
  };
  suggestion: {
    icon: 'Dumbbell' | 'Target' | 'Zap';
    title: string;
    description: string;
    ctaText: string;
    isLoading: boolean;
  };
  onDeleteMeal: (id: number) => void;
  onOpenManualAddModal: () => void;
  dailyCalorieGoal: number;
  onViewReport: () => void;
  waterIntake: number;
  onUpdateWater: (amount: number) => void;
}

const HomePage: React.FC<HomePageProps> = ({
  meals,
  dailyTotals,
  suggestion,
  onDeleteMeal,
  onOpenManualAddModal,
  dailyCalorieGoal,
  onViewReport,
  waterIntake,
  onUpdateWater,
}) => {
  return (
    <div className="space-y-6">
      <Dashboard dailyTotals={dailyTotals} dailyGoal={dailyCalorieGoal} />
      <WaterIntakeCard
        currentIntake={waterIntake}
        onUpdate={onUpdateWater}
      />
      <SuggestionCard
        icon={suggestion.icon}
        title={suggestion.title}
        description={suggestion.description}
        ctaText={suggestion.ctaText}
        onViewReport={onViewReport}
        isLoading={suggestion.isLoading}
      />
      <MealLog
        meals={meals}
        onDeleteMeal={onDeleteMeal}
        onOpenManualAddModal={onOpenManualAddModal}
      />
    </div>
  );
};

export default HomePage;
