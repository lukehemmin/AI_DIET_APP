import React from 'react';
import type { Meal } from '../types';
import { Trash2 } from 'lucide-react';

interface MealItemProps {
  meal: Meal;
  onDelete: (id: number) => void;
}

const MealItem: React.FC<MealItemProps> = ({ meal, onDelete }) => {
  return (
    <div className="flex items-center space-x-4 group pt-3 first:pt-0">
      <div className="flex-1 min-w-0">
        <p className="font-semibold text-gray-text dark:text-dark-text truncate">{meal.foodItem}</p>
        <p className="text-sm text-gray-subtext dark:text-dark-subtext">{meal.servingSize}g</p>
      </div>
      <div className="flex items-center space-x-3 flex-shrink-0">
        <p className="font-bold text-gray-text dark:text-dark-text text-lg text-right">{meal.kcal}<span className="text-sm font-normal text-gray-subtext dark:text-dark-subtext ml-1">kcal</span></p>
        <button 
            onClick={() => onDelete(meal.id)} 
            className="text-gray-500 opacity-0 group-hover:opacity-100 transition-opacity hover:text-red-500 dark:text-gray-600 dark:hover:text-red-500" 
            aria-label="삭제"
        >
          <Trash2 size={20} />
        </button>
      </div>
    </div>
  );
};

export default MealItem;