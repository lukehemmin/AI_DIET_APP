import React from 'react';
import { Plus, Minus } from 'lucide-react';
import { DAILY_WATER_GOAL } from '../constants';

interface WaterIntakeCardProps {
  currentIntake: number;
  onUpdate: (amount: number) => void;
}

const WaterIntakeCard: React.FC<WaterIntakeCardProps> = ({ currentIntake, onUpdate }) => {
  const goal = DAILY_WATER_GOAL;
  const isGoalReached = currentIntake >= goal;

  return (
    <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card animate-slide-up">
      <div className="flex justify-between items-center mb-4">
        <div>
          <h3 className="font-semibold text-gray-text dark:text-dark-text">오늘의 수분 섭취</h3>
          <p className="text-sm text-gray-subtext dark:text-dark-subtext">
            {isGoalReached ? "목표 달성! 훌륭해요! 🎉" : `목표: ${goal}잔`}
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <button
            onClick={() => onUpdate(-1)}
            disabled={currentIntake === 0}
            className="bg-gray-70 dark:bg-dark-border p-2 rounded-full text-gray-subtext dark:text-dark-subtext disabled:opacity-50 transition"
            aria-label="물 한 잔 줄이기"
          >
            <Minus size={16} />
          </button>
          <span className="text-lg font-bold text-primary-blue w-8 text-center">{currentIntake}</span>
          <button
            onClick={() => onUpdate(1)}
            className="bg-gray-70 dark:bg-dark-border p-2 rounded-full text-gray-subtext dark:text-dark-subtext transition"
            aria-label="물 한 잔 추가"
          >
            <Plus size={16} />
          </button>
        </div>
      </div>
      <div className="grid grid-cols-8 gap-2">
        {Array.from({ length: goal }).map((_, index) => (
          <div key={index} className="w-full aspect-square rounded-full flex items-center justify-center bg-gray-70 dark:bg-dark-border transition-colors">
            <div
              className={`w-full h-full rounded-full bg-primary-blue transition-all duration-300 ease-in-out
                         ${index < currentIntake ? 'scale-100 opacity-100' : 'scale-0 opacity-0'}`}
            ></div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default WaterIntakeCard;