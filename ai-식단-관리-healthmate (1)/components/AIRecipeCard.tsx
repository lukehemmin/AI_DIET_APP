import React from 'react';
import { ChefHat, RefreshCw } from 'lucide-react';
import type { Recipe } from '../types';

interface AIRecipeCardProps {
  recipe: Recipe | null;
  isLoading: boolean;
  onGenerateNew: () => void;
}

const SkeletonLoader: React.FC = () => (
  <div className="space-y-3 animate-pulse">
    <div className="h-5 bg-gray-70 dark:bg-dark-border rounded w-1/2"></div>
    <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-1/3"></div>
    <div className="space-y-2 pt-2">
        <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-full"></div>
        <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-5/6"></div>
    </div>
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

const AIRecipeCard: React.FC<AIRecipeCardProps> = ({ recipe, isLoading, onGenerateNew }) => {
  return (
    <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
        <div className="flex justify-between items-center mb-3">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text flex items-center">
                <ChefHat size={22} className="mr-2 text-primary-blue" />
                AI 맞춤 레시피
            </h2>
            <button
                onClick={onGenerateNew}
                disabled={isLoading}
                className="flex items-center text-sm font-semibold text-primary-blue hover:opacity-80 transition-opacity disabled:opacity-50 disabled:cursor-wait"
            >
                <RefreshCw size={14} className={`mr-1.5 ${isLoading ? 'animate-spin' : ''}`} />
                다른 레시피 추천
            </button>
        </div>
        
        <div className="min-h-[120px]">
            {isLoading && <SkeletonLoader />}
            {!isLoading && recipe && (
                 <div className="space-y-3 animate-fade-in">
                    <div>
                        <h3 className="font-bold text-lg text-gray-text dark:text-dark-text">{recipe.name}</h3>
                        <p className="text-sm font-semibold text-primary-blue">{recipe.kcal} kcal</p>
                    </div>
                    <div>
                        <h4 className="font-semibold text-sm text-gray-text dark:text-dark-text mb-1">재료</h4>
                        <p className="text-sm text-gray-subtext dark:text-dark-subtext">{recipe.ingredients.join(', ')}</p>
                    </div>
                     <div>
                        <h4 className="font-semibold text-sm text-gray-text dark:text-dark-text mb-1">조리법</h4>
                        <div className="text-sm text-gray-subtext dark:text-dark-subtext">
                            <MarkdownRenderer text={recipe.instructions.map((step, i) => `${i+1}. ${step}`).join('\n')} />
                        </div>
                    </div>
                </div>
            )}
             {!isLoading && !recipe && (
                <div className="flex items-center justify-center h-full pt-4">
                    <p className="text-gray-subtext dark:text-dark-subtext">레시피를 불러오지 못했습니다.</p>
                </div>
             )}
        </div>
    </div>
  );
};

export default AIRecipeCard;
