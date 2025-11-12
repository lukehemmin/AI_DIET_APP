import React, { useState, useEffect } from 'react';
import { Sparkles } from 'lucide-react';
import type { Meal } from '../types';
import { getAIFeedback } from '../services/geminiService';

interface AIInsightCardProps {
  meals: Meal[];
}

const SkeletonLoader = () => (
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

const AIInsightCard: React.FC<AIInsightCardProps> = ({ meals }) => {
  const [feedback, setFeedback] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchFeedback = async () => {
      if (meals.length === 0) {
        setFeedback("선택된 기간에 식단 기록이 없어 분석할 수 없습니다. 기록을 추가하거나 기간을 변경해주세요.");
        setIsLoading(false);
        setError(null);
        return;
      }
      setIsLoading(true);
      setError(null);
      try {
        const result = await getAIFeedback(meals);
        setFeedback(result);
      } catch (e) {
        setError('AI 피드백을 불러오는 데 실패했습니다.');
        console.error(e);
      } finally {
        setIsLoading(false);
      }
    };

    fetchFeedback();
  }, [meals]);


  return (
    <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
        <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text mb-3 flex items-center">
            <Sparkles size={22} className="mr-2 text-primary-blue" />
            AI 식습관 분석
        </h2>
        <div className="text-sm text-gray-subtext dark:text-dark-subtext min-h-[60px] flex items-center">
            {isLoading && <SkeletonLoader />}
            {error && <p className="text-red-500">{error}</p>}
            {!isLoading && !error && <p><MarkdownRenderer text={feedback} /></p>}
        </div>
    </div>
  );
};

export default AIInsightCard;
