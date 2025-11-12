import React from 'react';
import type { Meal, MealTime } from '../types';
import MealItem from './MealItem';
import { PlusCircle } from 'lucide-react';

interface MealLogProps {
  meals: Meal[];
  onDeleteMeal: (id: number) => void;
  onOpenManualAddModal: () => void;
}

const MealLog: React.FC<MealLogProps> = ({ meals, onDeleteMeal, onOpenManualAddModal }) => {
  const groupedMeals = meals.reduce((acc, meal) => {
    const time = meal.time;
    if (!acc[time]) {
      acc[time] = [];
    }
    acc[time].push(meal);
    return acc;
  }, {} as Record<MealTime, Meal[]>);

  const mealTimes: MealTime[] = ['아침', '점심', '저녁', '간식', '야식'];

  return (
    <div className="animate-slide-up">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">오늘의 식단 기록</h2>
        <button 
            onClick={onOpenManualAddModal}
            className="flex items-center text-sm font-semibold text-primary-blue hover:opacity-80 transition-opacity"
        >
            <PlusCircle size={18} className="mr-1.5" />
            수동으로 추가
        </button>
      </div>
      {meals.length > 0 ? (
        <div className="space-y-4">
          {mealTimes.map(time => {
            const mealsForTime = groupedMeals[time];
            if (!mealsForTime || mealsForTime.length === 0) return null;

            const mealsWithImages = mealsForTime.filter(m => m.image).reverse();
            const totalKcalForTime = mealsForTime.reduce((sum, meal) => sum + meal.kcal, 0);

            return (
              <div key={time} className="bg-gray-100 dark:bg-dark-card p-4 rounded-2xl shadow-card animate-slide-up">
                <div className="flex justify-between items-center mb-3">
                  <h3 className="text-lg font-semibold text-gray-text dark:text-dark-text">{time}</h3>
                  <p className="font-bold text-gray-subtext dark:text-dark-subtext">{Math.round(totalKcalForTime)} kcal</p>
                </div>

                {mealsWithImages.length > 0 && (
                  <div className="relative mb-4 h-20">
                    {mealsWithImages.slice(0, 5).map((meal, index) => (
                      <img
                        key={meal.id}
                        src={`data:image/jpeg;base64,${meal.image}`}
                        alt={meal.foodItem}
                        className="absolute top-0 w-20 h-20 object-cover rounded-xl ring-2 ring-gray-100 dark:ring-dark-card shadow-sm"
                        style={{ left: `${index * 24}px`, zIndex: mealsWithImages.length - index }}
                      />
                    ))}
                  </div>
                )}
                
                <div className="space-y-2 divide-y divide-gray-70/50 dark:divide-dark-border/50 -mx-4 px-4">
                  {mealsForTime.map(meal => (
                    <MealItem key={meal.id} meal={meal} onDelete={onDeleteMeal} />
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="text-center py-10 bg-gray-100 dark:bg-dark-card rounded-xl">
          <p className="text-gray-subtext dark:text-dark-subtext">아직 기록된 식단이 없습니다.</p>
          <p className="text-sm text-gray-500 dark:text-slate-500">하단 카메라 버튼 또는 수동 추가를 눌러 시작하세요.</p>
        </div>
      )}
    </div>
  );
};

export default MealLog;