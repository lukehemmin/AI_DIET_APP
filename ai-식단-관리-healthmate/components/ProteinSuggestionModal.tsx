
import React, { useState, useEffect, useCallback } from 'react';
import { X, Sparkles, RefreshCw, Beef } from 'lucide-react';
import type { Meal, ProteinFoodSuggestion } from '../types';
import { getProteinFoodSuggestions } from '../services/geminiService';

interface ProteinSuggestionModalProps {
  isOpen: boolean;
  onClose: () => void;
  meals: Meal[];
}

const SkeletonLoader: React.FC = () => (
    <div className="space-y-3 animate-pulse">
        {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-gray-70 dark:bg-dark-border p-4 rounded-lg flex items-center space-x-3">
                <div className="w-10 h-10 bg-gray-50 dark:bg-slate-700 rounded-full"></div>
                <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-50 dark:bg-slate-700 rounded w-1/2"></div>
                    <div className="h-3 bg-gray-50 dark:bg-slate-700 rounded w-full"></div>
                </div>
            </div>
        ))}
    </div>
);

const ProteinSuggestionModal: React.FC<ProteinSuggestionModalProps> = ({ isOpen, onClose, meals }) => {
  const [suggestions, setSuggestions] = useState<ProteinFoodSuggestion[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSuggestions = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await getProteinFoodSuggestions(meals);
      setSuggestions(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : '추천을 불러오는 데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [meals]);

  useEffect(() => {
    if (isOpen) {
      fetchSuggestions();
    }
  }, [isOpen, fetchSuggestions]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in" onClick={onClose}>
      <div className="bg-gray-100 dark:bg-dark-card rounded-2xl shadow-lg w-full max-w-sm p-6 animate-slide-up" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-start mb-4">
          <div>
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text flex items-center">
              <Sparkles size={22} className="mr-2 text-primary-blue" />
              AI 단백질 보충 추천
            </h2>
            <p className="text-sm text-gray-subtext dark:text-dark-subtext">단백질 섭취가 부족해요!</p>
          </div>
          <button onClick={onClose} className="text-gray-subtext dark:text-dark-subtext hover:text-gray-text dark:hover:text-dark-text">
            <X size={24} />
          </button>
        </div>
        
        <div className="space-y-3 min-h-[280px]">
          {isLoading ? <SkeletonLoader /> : error ? (
            <div className="text-center text-red-500 py-10">{error}</div>
          ) : (
            suggestions.map((item, index) => (
              <div key={index} className="bg-gray-90 dark:bg-dark-bg p-4 rounded-lg flex items-center space-x-4 animate-fade-in">
                <div className="bg-primary-blue/10 p-2.5 rounded-full">
                    <Beef size={20} className="text-primary-blue" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-text dark:text-dark-text">{item.name}</h3>
                  <p className="text-xs text-gray-subtext dark:text-dark-subtext">{item.reason}</p>
                </div>
              </div>
            ))
          )}
        </div>
        
        <div className="mt-6 grid grid-cols-2 gap-3">
            <button 
              onClick={fetchSuggestions}
              disabled={isLoading}
              className="w-full bg-gray-70 dark:bg-dark-border text-gray-subtext dark:text-dark-text py-3 rounded-xl font-semibold hover:bg-gray-200 dark:hover:bg-slate-600 transition flex items-center justify-center disabled:opacity-50"
            >
              <RefreshCw size={16} className={`mr-2 ${isLoading ? 'animate-spin' : ''}`} />
              다른 추천
            </button>
            <button 
              onClick={onClose}
              className="w-full bg-primary-blue text-white py-3 rounded-xl font-semibold hover:bg-blue-600 transition"
            >
              확인
            </button>
        </div>
      </div>
    </div>
  );
};

export default ProteinSuggestionModal;
