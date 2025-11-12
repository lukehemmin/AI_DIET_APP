
import React, { useState, FormEvent, useEffect } from 'react';
import type { MealTime } from '../types';
import { X, Plus, LoaderCircle } from 'lucide-react';

interface ManualAddModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddMeal: (mealData: { foodItem: string, time: MealTime }) => Promise<void>;
  prefillValue?: string | null;
}

const InputField: React.FC<{ label: string; type: string; value: string; onChange: (e: React.ChangeEvent<HTMLInputElement>) => void; name: string; placeholder?: string; }> = 
({ label, type, value, onChange, name, placeholder }) => (
    <div>
        <label htmlFor={name} className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">{label}</label>
        <input
            type={type}
            id={name}
            name={name}
            value={value}
            onChange={onChange}
            placeholder={placeholder}
            className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition"
            required
        />
    </div>
);

const ManualAddModal: React.FC<ManualAddModalProps> = ({ isOpen, onClose, onAddMeal, prefillValue }) => {
  const [foodItem, setFoodItem] = useState('');
  const [time, setTime] = useState<MealTime>('점심');
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  useEffect(() => {
    if (isOpen) {
        setFoodItem(prefillValue || '');
        const currentHour = new Date().getHours();
        if (currentHour >= 4 && currentHour < 10) setTime('아침');
        else if (currentHour >= 10 && currentHour < 16) setTime('점심');
        else if (currentHour >= 16 && currentHour < 21) setTime('저녁');
        else if (currentHour >= 21 || currentHour < 2) setTime('야식');
        else setTime('간식');
    }
  }, [isOpen, prefillValue]);


  const resetForm = () => {
    setFoodItem('');
    setTime('점심');
    setIsAnalyzing(false);
    setError(null);
  };
  
  const handleClose = () => {
    resetForm();
    onClose();
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!foodItem.trim()) return;

    setIsAnalyzing(true);
    setError(null);
    try {
      await onAddMeal({ foodItem, time });
    } catch (err) {
      setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
    } finally {
      setIsAnalyzing(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in" onClick={handleClose}>
      <div className="bg-gray-100 dark:bg-dark-card rounded-2xl shadow-lg w-full max-w-sm p-6 animate-slide-up" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">식단 수동 추가</h2>
            <button onClick={handleClose} className="text-gray-subtext dark:text-dark-subtext hover:text-gray-text dark:hover:text-dark-text">
                <X size={24} />
            </button>
        </div>
        
        <form onSubmit={handleSubmit} className="space-y-4">
            <InputField 
              label="음식 이름" 
              type="text" 
              name="foodItem" 
              value={foodItem} 
              onChange={(e) => setFoodItem(e.target.value)} 
              placeholder="예: 닭가슴살 샐러드" 
            />
            <div>
                <label htmlFor="time" className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">식사 시간</label>
                <select id="time" name="time" value={time} onChange={(e) => setTime(e.target.value as MealTime)} className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition">
                    <option value="아침">아침</option>
                    <option value="점심">점심</option>
                    <option value="저녁">저녁</option>
                    <option value="간식">간식</option>
                    <option value="야식">야식</option>
                </select>
            </div>
            
            {error && <p className="text-red-500 text-center text-sm">{error}</p>}
            
            <button 
              type="submit" 
              className="w-full bg-primary-blue text-white py-3 mt-2 rounded-xl font-semibold hover:bg-blue-600 transition flex items-center justify-center disabled:opacity-75"
              disabled={isAnalyzing}
            >
                {isAnalyzing ? (
                    <LoaderCircle size={20} className="mr-2 animate-spin"/>
                ) : (
                    <Plus size={20} className="mr-2"/>
                )}
                {isAnalyzing ? 'AI가 분석 중...' : '기록에 추가하기'}
            </button>
        </form>
      </div>
    </div>
  );
};

export default ManualAddModal;