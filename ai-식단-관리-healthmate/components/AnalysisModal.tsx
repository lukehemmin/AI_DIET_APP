

import React, { useState, useEffect, useMemo } from 'react';
import type { AnalysisResult, Nutrients, MealTime } from '../types';
import { X, CheckCircle2, Trash2 } from 'lucide-react';

interface AnalysisModalProps {
  isOpen: boolean;
  isAnalyzing: boolean;
  result: AnalysisResult[] | null;
  image: string | null;
  error: string | null;
  onClose: () => void;
  onConfirm: (result: AnalysisResult[], time: MealTime) => void;
  onSkip: () => void;
  queuePosition: string | null;
}

const SkeletonLoader: React.FC = () => (
  <div className="w-full">
    <div className="bg-gray-70 dark:bg-dark-border h-40 w-full rounded-lg animate-pulse mb-4"></div>
    <div className="space-y-3">
        <div className="bg-gray-70 dark:bg-dark-border h-4 w-1/3 rounded animate-pulse"></div>
        {[...Array(2)].map((_, i) => (
             <div key={i} className="bg-gray-70 dark:bg-dark-border h-16 w-full rounded animate-pulse"></div>
        ))}
    </div>
    <div className="bg-gray-70 dark:bg-dark-border h-20 w-full rounded-lg animate-pulse mt-4"></div>
  </div>
);

const EditableFoodItem: React.FC<{
    item: AnalysisResult;
    index: number;
    onResultChange: (index: number, field: keyof AnalysisResult | `macro.${keyof Nutrients}`, value: any) => void;
    onRemoveItem: (index: number) => void;
}> = ({ item, index, onResultChange, onRemoveItem }) => {
    
    const handleNumericChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        const numericValue = value === '' ? 0 : parseInt(value.replace(/[^0-9]/g, ''), 10);
        if (!isNaN(numericValue)) {
            onResultChange(index, name as any, numericValue);
        }
    };
    
    return (
        <div className="bg-gray-100 dark:bg-dark-bg p-3 rounded-lg border border-gray-70 dark:border-dark-border">
            <div className="grid grid-cols-[1fr_auto] gap-x-2 items-start">
                <input 
                  type="text" 
                  value={item.foodItem}
                  onChange={(e) => onResultChange(index, 'foodItem', e.target.value)}
                  className="font-semibold text-gray-text dark:text-dark-text bg-transparent w-full focus:outline-none focus:ring-1 focus:ring-primary-blue rounded px-1 -mx-1 text-base"
                />
                <button onClick={() => onRemoveItem(index)} className="text-gray-subtext hover:text-red-500">
                    <Trash2 size={18} />
                </button>
            </div>
             <div className="flex items-baseline justify-between text-sm mt-2">
                <div className="flex items-baseline">
                    <input 
                        type="text"
                        value={item.servingSize === 0 ? '' : item.servingSize}
                        name="servingSize" 
                        onChange={handleNumericChange} 
                        className="text-gray-subtext dark:text-dark-subtext bg-transparent focus:outline-none focus:ring-1 focus:ring-primary-blue rounded px-1 w-auto max-w-[6ch] font-semibold text-right"
                        placeholder="0"
                        size={4}
                    />
                    <span className="text-gray-500 dark:text-slate-500 ml-1">g</span>
                </div>
                <div className="flex items-center bg-primary-blue/10 rounded-full px-2 py-0.5">
                    <input 
                        type="text"
                        value={item.kcal === 0 ? '' : item.kcal} 
                        name="kcal" 
                        onChange={handleNumericChange} 
                        className="font-bold text-primary-blue bg-transparent focus:outline-none focus:ring-0 w-auto max-w-[7ch] text-right"
                        placeholder="0"
                        size={5}
                    />
                    <span className="font-semibold text-primary-blue text-xs">kcal</span>
                </div>
            </div>
        </div>
    );
};


const ResultDisplay: React.FC<{ 
    result: AnalysisResult[]; 
    image: string | null; 
    selectedTime: MealTime;
    onResultChange: (index: number, field: keyof AnalysisResult | `macro.${keyof Nutrients}`, value: any) => void;
    onTimeChange: (time: MealTime) => void;
    onRemoveItem: (index: number) => void;
}> = ({ result, image, selectedTime, onResultChange, onTimeChange, onRemoveItem }) => {

    const totalKcal = useMemo(() => {
        return result.reduce((sum, item) => sum + (Number(item.kcal) || 0), 0);
    }, [result]);

  return (
    <div className="animate-fade-in w-full text-left">
        {image && <img src={`data:image/jpeg;base64,${image}`} alt="분석된 음식" className="w-full h-40 object-cover rounded-lg mb-4" />}
        
        <p className="text-sm font-semibold text-gray-subtext dark:text-dark-subtext mb-2">분석된 음식 목록 ({result.length}개)</p>
        <div className="space-y-2 max-h-48 overflow-y-auto pr-2 -mr-2">
             {result.map((item, index) => (
                <EditableFoodItem 
                    key={index} 
                    item={item} 
                    index={index} 
                    onResultChange={onResultChange} 
                    onRemoveItem={onRemoveItem}
                />
            ))}
        </div>

        <div className="bg-gray-90 dark:bg-dark-bg p-4 rounded-lg mt-4">
            <div className="flex justify-between items-baseline mb-4">
                <span className="text-gray-subtext dark:text-dark-subtext">총 칼로리</span>
                 <span className="text-2xl font-bold text-primary-blue">{Math.round(totalKcal).toLocaleString()} kcal</span>
            </div>
            <div className="flex justify-between items-center">
                <span className="text-sm text-gray-subtext dark:text-dark-subtext">식사 시간</span>
                <select 
                    value={selectedTime} 
                    onChange={(e) => onTimeChange(e.target.value as MealTime)}
                    className="text-gray-subtext dark:text-dark-subtext bg-transparent focus:outline-none focus:ring-1 focus:ring-primary-blue rounded px-1 py-0.5 border-none"
                >
                    <option value="아침">아침</option>
                    <option value="점심">점심</option>
                    <option value="저녁">저녁</option>
                    <option value="간식">간식</option>
                    <option value="야식">야식</option>
                </select>
            </div>
        </div>
    </div>
);
};


const AnalysisModal: React.FC<AnalysisModalProps> = ({ isOpen, isAnalyzing, result, image, error, onClose, onConfirm, onSkip, queuePosition }) => {
  const [editableResult, setEditableResult] = useState<AnalysisResult[] | null>(null);
  const [selectedTime, setSelectedTime] = useState<MealTime>('점심');

  useEffect(() => {
    setEditableResult(result);
     if (isOpen && !result) {
        const currentHour = new Date().getHours();
        if (currentHour >= 4 && currentHour < 10) setSelectedTime('아침');
        else if (currentHour >= 10 && currentHour < 16) setSelectedTime('점심');
        else if (currentHour >= 16 && currentHour < 21) setSelectedTime('저녁');
        else if (currentHour >= 21 || currentHour < 2) setSelectedTime('야식');
        else setSelectedTime('간식'); // 2-4 am as snack time or adjust as needed
    }
  }, [result, isOpen]);

  const handleResultChange = (index: number, field: keyof AnalysisResult | `macro.${keyof Nutrients}`, value: any) => {
    setEditableResult(prev => {
        if (!prev) return null;
        const newResults = [...prev];
        const itemToUpdate = JSON.parse(JSON.stringify(newResults[index]));

        if (typeof field === 'string' && field.startsWith('macro.')) {
            const macroField = field.split('.')[1] as keyof Nutrients;
            itemToUpdate.macro[macroField] = value;
        } else {
            (itemToUpdate as any)[field] = value;
        }
        newResults[index] = itemToUpdate;
        return newResults;
    });
  };
  
  const handleRemoveItem = (indexToRemove: number) => {
    setEditableResult(prev => prev ? prev.filter((_, index) => index !== indexToRemove) : null);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in" onClick={onClose}>
      <div className="bg-gray-100 dark:bg-dark-card rounded-2xl shadow-lg w-full max-w-sm p-6 text-center animate-slide-up" onClick={(e) => e.stopPropagation()}>
        <button onClick={onClose} className="absolute top-4 right-4 text-gray-subtext dark:text-dark-subtext hover:text-gray-text dark:hover:text-dark-text">
          <X size={24} />
        </button>
        
        <h2 className="text-xl font-semibold mb-4 text-gray-text dark:text-dark-text">
          {isAnalyzing 
            ? `음식 사진 분석 중... ${queuePosition ? `(${queuePosition})` : ''}` 
            : error 
            ? "오류 발생" 
            : `분석 완료! ${queuePosition ? `(${queuePosition})` : ''}`}
        </h2>

        <div className="min-h-[400px] flex items-center justify-center">
            {isAnalyzing && <SkeletonLoader />}
            {!isAnalyzing && editableResult && editableResult.length > 0 && (
              <ResultDisplay 
                result={editableResult} 
                image={image} 
                selectedTime={selectedTime}
                onResultChange={handleResultChange}
                onTimeChange={setSelectedTime}
                onRemoveItem={handleRemoveItem}
              />
            )}
            {!isAnalyzing && (error || !editableResult || editableResult.length === 0) && (
                <div className="text-red-500 flex flex-col items-center">
                    <p>{error || "분석된 음식이 없습니다."}</p>
                </div>
            )}
        </div>

        <div className="mt-6">
          {editableResult && editableResult.length > 0 && !isAnalyzing && (
            <button
              onClick={() => onConfirm(editableResult, selectedTime)}
              className="w-full bg-primary-blue text-white py-3 rounded-xl font-semibold hover:bg-blue-600 transition flex items-center justify-center"
              disabled={editableResult.length === 0}
            >
              <CheckCircle2 size={20} className="mr-2"/>
              {queuePosition ? '추가하고 다음 분석' : '식단에 추가하기'}
            </button>
          )}
          {queuePosition && !isAnalyzing && (result || error) && (
            <button
              onClick={onSkip}
              className="w-full text-center text-gray-subtext dark:text-dark-subtext py-3 mt-2 font-medium hover:text-gray-text dark:hover:text-dark-text transition"
            >
              이 사진 건너뛰기
            </button>
          )}
           {(error || (editableResult && editableResult.length === 0)) && !isAnalyzing && !queuePosition && (
            <button
              onClick={onClose}
              className="w-full bg-gray-50 dark:bg-dark-border text-gray-subtext dark:text-dark-subtext py-3 rounded-xl font-semibold hover:bg-gray-70 dark:hover:bg-slate-600 transition"
            >
              닫기
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default AnalysisModal;