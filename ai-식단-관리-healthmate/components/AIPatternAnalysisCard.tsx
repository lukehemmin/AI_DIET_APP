import React, { useState, useEffect, useMemo } from 'react';
import { ClipboardList } from 'lucide-react';
import type { Meal, DateRange } from '../types';
import { getAIPatternAnalysis } from '../services/geminiService';

interface AIPatternAnalysisCardProps {
  meals: Meal[];
  dateRange: DateRange;
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
        if (line.trim() === '') return null; // Handle empty lines between paragraphs
        const parts = line.split(/(\*\*.*?\*\*)/g).filter(Boolean);
        return (
          <p key={lineIndex} className="mb-2 last:mb-0">
            {parts.map((part, partIndex) => {
              if (part.startsWith('**') && part.endsWith('**')) {
                return <strong key={partIndex}>{part.slice(2, -2)}</strong>;
              }
              return <React.Fragment key={partIndex}>{part}</React.Fragment>;
            })}
          </p>
        );
      })}
    </>
  );
};

const StatItem: React.FC<{ label: string; value: string; unit: string }> = ({ label, value, unit }) => (
    <div className="bg-gray-90 dark:bg-dark-bg p-3 rounded-lg text-center">
        <p className="text-sm text-gray-subtext dark:text-dark-subtext">{label}</p>
        <p className="text-xl font-bold text-gray-text dark:text-dark-text">{value}<span className="text-sm font-normal ml-1">{unit}</span></p>
    </div>
);


const AIPatternAnalysisCard: React.FC<AIPatternAnalysisCardProps> = ({ meals, dateRange }) => {
  const [analysis, setAnalysis] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const stats = useMemo(() => {
    const lateNightMeals = meals.filter(m => m.time === '야식').length;

    const weekdayMeals: Record<string, number> = {};
    const weekendMeals: Record<string, number> = {};

    meals.forEach(meal => {
        const date = new Date(meal.date);
        const day = date.getDay(); // 0 = Sunday, 6 = Saturday
        const dateString = date.toDateString();

        if (day === 0 || day === 6) { // Weekend
            if (!weekendMeals[dateString]) weekendMeals[dateString] = 0;
            weekendMeals[dateString] += meal.kcal;
        } else { // Weekday
            if (!weekdayMeals[dateString]) weekdayMeals[dateString] = 0;
            weekdayMeals[dateString] += meal.kcal;
        }
    });
    
    const weekdayDays = Object.keys(weekdayMeals).length;
    const weekendDays = Object.keys(weekendMeals).length;

    const totalWeekdayKcal = Object.values(weekdayMeals).reduce((sum, kcal) => sum + kcal, 0);
    const totalWeekendKcal = Object.values(weekendMeals).reduce((sum, kcal) => sum + kcal, 0);

    const weekdayAvgKcal = weekdayDays > 0 ? totalWeekdayKcal / weekdayDays : 0;
    const weekendAvgKcal = weekendDays > 0 ? totalWeekendKcal / weekendDays : 0;

    return { lateNightMeals, weekdayAvgKcal, weekendAvgKcal, dateRange };
  }, [meals, dateRange]);


  useEffect(() => {
    const fetchAnalysis = async () => {
      if (meals.length < 3) {
        setAnalysis("식습관 패턴을 분석하기에는 데이터가 부족해요. 3일 이상 꾸준히 기록해보세요!");
        setIsLoading(false);
        setError(null);
        return;
      }
      setIsLoading(true);
      setError(null);
      try {
        const result = await getAIPatternAnalysis(stats);
        setAnalysis(result);
      } catch (e) {
        setError('AI 패턴 분석을 불러오는 데 실패했습니다.');
        console.error(e);
      } finally {
        setIsLoading(false);
      }
    };

    fetchAnalysis();
  }, [stats, meals.length]);


  return (
    <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
        <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text mb-4 flex items-center">
            <ClipboardList size={22} className="mr-2 text-primary-blue" />
            AI 식습관 패턴 분석
        </h2>
        
        <div className="grid grid-cols-3 gap-3 mb-4">
            <StatItem label="야식 횟수" value={String(stats.lateNightMeals)} unit="회" />
            <StatItem label="평일 평균" value={Math.round(stats.weekdayAvgKcal).toLocaleString()} unit="kcal" />
            <StatItem label="주말 평균" value={Math.round(stats.weekendAvgKcal).toLocaleString()} unit="kcal" />
        </div>

        <div className="text-sm text-gray-subtext dark:text-dark-subtext min-h-[100px]">
            {isLoading && <SkeletonLoader />}
            {error && <p className="text-red-500">{error}</p>}
            {!isLoading && !error && (
                <div className="leading-relaxed"><MarkdownRenderer text={analysis} /></div>
            )}
        </div>
    </div>
  );
};

export default AIPatternAnalysisCard;
