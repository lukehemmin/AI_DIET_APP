import React from 'react';
import { Dumbbell } from 'lucide-react';

interface AIExerciseCardProps {
  plan: string | null;
  isLoading: boolean;
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

const AIExerciseCard: React.FC<AIExerciseCardProps> = ({ plan, isLoading }) => {
  return (
    <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
        <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text mb-3 flex items-center">
            <Dumbbell size={22} className="mr-2 text-primary-blue" />
            AI 주간 운동 플랜
        </h2>
        <div className="text-sm text-gray-subtext dark:text-dark-subtext min-h-[60px] flex items-center">
            {isLoading && <SkeletonLoader />}
            {!isLoading && plan && <p className="leading-relaxed"><MarkdownRenderer text={plan} /></p>}
        </div>
    </div>
  );
};

export default AIExerciseCard;
