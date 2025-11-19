

import React, { useState, useEffect, useMemo } from 'react';
import type { Meal, ChallengeProgress } from '../types';
import { getAIWeeklyReport } from '../services/geminiService';
import { X, Sparkles, BarChart, Pizza, Award } from 'lucide-react';

interface ReportPageProps {
  meals: Meal[];
  challenges: ChallengeProgress[];
  onClose: () => void;
}

const SkeletonLoader: React.FC = () => (
    <div className="space-y-2 animate-pulse">
        <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-3/4"></div>
        <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-full"></div>
        <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-5/6"></div>
    </div>
);

const MarkdownRenderer: React.FC<{ text: string }> = ({ text }) => {
  return (
    <>
      {text.split('\n').map((line, lineIndex) => {
        const parts = line.split(/(\*\*.*?\*\*)/g).filter(Boolean);
        return (
          <React.Fragment key={lineIndex}>
            {parts.map((part, partIndex) => {
              if (part.startsWith('**') && part.endsWith('**')) {
                return <strong key={partIndex}>{part.slice(2, -2)}</strong>;
              }
              return <React.Fragment key={partIndex}>{part}</React.Fragment>;
            })}
            {lineIndex < text.split('\n').length - 1 && <br />}
          </React.Fragment>
        );
      })}
    </>
  );
};

const ReportPage: React.FC<ReportPageProps> = ({ meals, challenges, onClose }) => {
  const [report, setReport] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const weeklyMeals = useMemo(() => {
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    sevenDaysAgo.setHours(0, 0, 0, 0);
    return meals.filter(m => new Date(m.date) >= sevenDaysAgo);
  }, [meals]);

  useEffect(() => {
    const fetchReport = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const result = await getAIWeeklyReport(weeklyMeals);
        setReport(result);
      } catch (e) {
        setError('AI 리포트를 불러오는 데 실패했습니다.');
        console.error(e);
      } finally {
        setIsLoading(false);
      }
    };
    fetchReport();
  }, [weeklyMeals]);

  const weeklyStats = useMemo(() => {
    if (weeklyMeals.length === 0) {
      return { avgKcal: 0, topFoods: [], completedChallenges: [] };
    }
    
    const dayCount = new Set(weeklyMeals.map(m => new Date(m.date).toDateString())).size;
    const totalKcal = weeklyMeals.reduce((sum, meal) => sum + meal.kcal, 0);
    const avgKcal = dayCount > 0 ? Math.round(totalKcal / dayCount) : 0;

    // The initial value for the reduce function was incompatible with the accumulator's type, causing a type inference failure.
    // FIX: Using a generic argument for `reduce` correctly types the accumulator, resolving the downstream error.
    const foodCounts = weeklyMeals.reduce<Record<string, number>>((acc, meal) => {
      acc[meal.foodItem] = (acc[meal.foodItem] || 0) + 1;
      return acc;
    }, {});

    const topFoods = Object.entries(foodCounts)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3)
      .map(entry => entry[0]);

    const completedChallenges = challenges.filter(c => c.status === '완료').map(c => c.title);

    return { avgKcal, topFoods, completedChallenges };
  }, [weeklyMeals, challenges]);

  return (
    <div className="fixed inset-0 bg-gray-90 dark:bg-dark-bg z-50 animate-fade-in overflow-y-auto">
      <div className="max-w-3xl mx-auto p-4 md:p-6">
        <header className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-text dark:text-dark-text">주간 건강 리포트</h1>
            <p className="text-gray-subtext dark:text-dark-subtext">지난 한 주를 돌아보세요!</p>
          </div>
          <button onClick={onClose} className="p-2 rounded-full hover:bg-gray-70 dark:hover:bg-dark-border">
            <X size={24} />
          </button>
        </header>

        <div className="space-y-6">
          <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text mb-3 flex items-center">
                <Sparkles size={22} className="mr-2 text-primary-blue" />
                AI 주간 총평
            </h2>
            <div className="text-sm text-gray-subtext dark:text-dark-subtext min-h-[80px] flex items-center">
                {isLoading && <SkeletonLoader />}
                {error && <p className="text-red-500">{error}</p>}
                {!isLoading && !error && <p className="leading-relaxed"><MarkdownRenderer text={report} /></p>}
            </div>
          </div>
          
          <div className="grid md:grid-cols-3 gap-4">
            <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
                <h3 className="font-semibold text-gray-text dark:text-dark-text mb-2 flex items-center"><BarChart size={18} className="mr-2 text-primary-blue" />주간 평균</h3>
                <p className="text-3xl font-bold text-gray-text dark:text-dark-text">{weeklyStats.avgKcal.toLocaleString()}<span className="text-base font-normal text-gray-subtext dark:text-dark-subtext"> kcal/일</span></p>
            </div>
             <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
                <h3 className="font-semibold text-gray-text dark:text-dark-text mb-2 flex items-center"><Pizza size={18} className="mr-2 text-primary-blue" />가장 많이 먹은 음식</h3>
                <ul className="space-y-1">
                    {weeklyStats.topFoods.length > 0 ? weeklyStats.topFoods.map((food, i) => (
                       <li key={i} className="text-sm text-gray-subtext dark:text-dark-subtext truncate"><strong>{i+1}.</strong> {food}</li>
                    )) : <li className="text-sm text-gray-subtext dark:text-dark-subtext">기록이 부족해요.</li>}
                </ul>
            </div>
             <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
                <h3 className="font-semibold text-gray-text dark:text-dark-text mb-2 flex items-center"><Award size={18} className="mr-2 text-primary-blue" />완료한 챌린지</h3>
                 <ul className="space-y-1">
                    {weeklyStats.completedChallenges.length > 0 ? weeklyStats.completedChallenges.map((challenge, i) => (
                       <li key={i} className="text-sm text-green-600 dark:text-state-success font-semibold truncate">✓ {challenge}</li>
                    )) : <li className="text-sm text-gray-subtext dark:text-dark-subtext">아직 없어요.</li>}
                </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReportPage;