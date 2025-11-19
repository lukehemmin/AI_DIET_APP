
import React, { useState } from 'react';
import { Refrigerator, ChefHat, Sparkles, LoaderCircle } from 'lucide-react';
import type { UserProfile, Recipe } from '../types';
import { getFridgeRecipe } from '../services/geminiService';

interface FridgeRecipeGeneratorProps {
  userProfile: UserProfile;
}

// Reuse from other components
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

// Reuse from other components
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


const FridgeRecipeGenerator: React.FC<FridgeRecipeGeneratorProps> = ({ userProfile }) => {
    const [ingredients, setIngredients] = useState('');
    const [recipe, setRecipe] = useState<Recipe | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleGenerateRecipe = async () => {
        if (!ingredients.trim()) {
            setError("재료를 입력해주세요.");
            return;
        }
        setIsLoading(true);
        setError(null);
        setRecipe(null);
        try {
            const result = await getFridgeRecipe(ingredients, userProfile);
            setRecipe(result);
        } catch (err) {
            setError(err instanceof Error ? err.message : '레시피 생성 중 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card space-y-4">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text flex items-center">
                <Refrigerator size={22} className="mr-2 text-primary-blue" />
                냉장고 파먹기 AI 레시피
            </h2>

            <textarea
                value={ingredients}
                onChange={(e) => setIngredients(e.target.value)}
                placeholder="가지고 있는 재료를 입력해보세요. (예: 닭가슴살, 양파, 계란, 우유)"
                className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition text-sm min-h-[80px]"
            />
            <button
                onClick={handleGenerateRecipe}
                disabled={isLoading}
                className="w-full bg-primary-blue text-white py-3 rounded-xl font-semibold hover:bg-blue-600 transition flex items-center justify-center disabled:opacity-75 disabled:cursor-wait"
            >
                {isLoading ? (
                    <LoaderCircle size={20} className="mr-2 animate-spin" />
                ) : (
                    <Sparkles size={20} className="mr-2" />
                )}
                {isLoading ? 'AI가 레시피 찾는 중...' : '나만의 레시피 생성하기'}
            </button>

            <div className="min-h-[120px] pt-4 border-t border-gray-70 dark:border-dark-border">
                {isLoading && <SkeletonLoader />}
                {error && <p className="text-red-500 text-center">{error}</p>}
                {!isLoading && recipe && (
                    <div className="space-y-3 animate-fade-in">
                        <div>
                            <h3 className="font-bold text-lg text-gray-text dark:text-dark-text flex items-center"><ChefHat size={18} className="mr-2"/>{recipe.name}</h3>
                            <p className="text-sm font-semibold text-primary-blue">{recipe.kcal} kcal</p>
                        </div>
                        <div>
                            <h4 className="font-semibold text-sm text-gray-text dark:text-dark-text mb-1">필요 재료</h4>
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
                {!isLoading && !recipe && !error && (
                     <div className="flex flex-col items-center justify-center text-center text-gray-subtext dark:text-dark-subtext pt-8">
                        <p>재료를 입력하고 버튼을 누르면 AI가 맞춤 레시피를 만들어드려요!</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default FridgeRecipeGenerator;
