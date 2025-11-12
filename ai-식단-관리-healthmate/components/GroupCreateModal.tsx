
import React, { useState, FormEvent, useEffect } from 'react';
import { X, Plus, Sparkles, LoaderCircle } from 'lucide-react';
import type { Friend } from '../types';
import { getAIGroupChallengeSuggestions } from '../services/geminiService';

interface GroupCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreate: (groupData: { name: string; description: string }, challenge: string, invitedFriends: number[]) => void;
  friends: Friend[];
}

const GroupCreateModal: React.FC<GroupCreateModalProps> = ({ isOpen, onClose, onCreate, friends }) => {
  const [step, setStep] = useState(1);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [goalTheme, setGoalTheme] = useState('');
  
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [selectedChallenge, setSelectedChallenge] = useState('');
  const [customChallenge, setCustomChallenge] = useState('');
  const [isCustom, setIsCustom] = useState(false);
  
  const [selectedFriends, setSelectedFriends] = useState<number[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const resetState = () => {
    setStep(1);
    setName('');
    setDescription('');
    setGoalTheme('');
    setSuggestions([]);
    setSelectedChallenge('');
    setCustomChallenge('');
    setIsCustom(false);
    setSelectedFriends([]);
    setIsGenerating(false);
    setError(null);
  };
  
  useEffect(() => {
    if (isOpen) {
      resetState();
    }
  }, [isOpen]);

  const handleFriendToggle = (friendId: number) => {
    setSelectedFriends(prev => 
      prev.includes(friendId) 
        ? prev.filter(id => id !== friendId)
        : [...prev, friendId]
    );
  };

  const handleGenerateSuggestions = async (e: FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('그룹 이름을 입력해주세요.');
      return;
    }
    if (!goalTheme.trim()) {
      setError('그룹 목표의 주제를 입력해주세요.');
      return;
    }
    
    setIsGenerating(true);
    setError(null);
    try {
      const result = await getAIGroupChallengeSuggestions(goalTheme);
      setSuggestions(result);
      if (result.length > 0) {
        setSelectedChallenge(result[0]);
      } else {
        setIsCustom(true); // If no suggestions, default to custom input
      }
      setStep(2);
    } catch (err) {
      // Fallback to custom input on error
      setError('AI 추천 생성에 실패했어요. 직접 목표를 설정해주세요.');
      setIsCustom(true);
      setStep(2);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const finalChallenge = isCustom ? customChallenge : selectedChallenge;
    if (name.trim() && finalChallenge.trim()) {
      onCreate({ name, description }, finalChallenge, selectedFriends);
    } else {
      alert("그룹 이름과 챌린지 목표를 설정해주세요.");
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in" onClick={onClose}>
      <div className="bg-gray-100 dark:bg-dark-card rounded-2xl shadow-lg w-full max-w-sm p-6 animate-slide-up" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">
              {step === 1 ? '새로운 그룹 만들기' : '챌린지 선택 및 친구 초대'}
            </h2>
            <button onClick={onClose} className="text-gray-subtext dark:text-dark-subtext hover:text-gray-text dark:hover:text-dark-text">
                <X size={24} />
            </button>
        </div>
        
        {isGenerating ? (
            <div className="flex flex-col items-center justify-center min-h-[300px]">
                <LoaderCircle size={32} className="animate-spin text-primary-blue" />
                <p className="mt-4 text-gray-subtext dark:text-dark-subtext">AI가 멋진 챌린지를 만들고 있어요...</p>
            </div>
        ) : (
          <form onSubmit={step === 1 ? handleGenerateSuggestions : handleSubmit} className="space-y-4">
              {step === 1 && (
                <>
                  <div>
                      <label htmlFor="groupName" className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">그룹 이름</label>
                      <input type="text" id="groupName" value={name} onChange={(e) => setName(e.target.value)} placeholder="예: 여름 준비 다이어트방" className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition" required />
                  </div>
                  <div>
                      <label htmlFor="groupDescription" className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">한 줄 설명 (선택)</label>
                      <input type="text" id="groupDescription" value={description} onChange={(e) => setDescription(e.target.value)} placeholder="예: 함께 목표를 달성해봐요!" className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition" />
                  </div>
                  <div>
                      <label htmlFor="goalTheme" className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">그룹 목표 주제</label>
                      <input type="text" id="goalTheme" value={goalTheme} onChange={(e) => setGoalTheme(e.target.value)} placeholder="예: 건강한 식습관, 다이어트" className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition" required />
                  </div>
                  {error && <p className="text-red-500 text-center text-sm">{error}</p>}
                  <button type="submit" className="w-full bg-primary-blue text-white py-3 mt-2 rounded-xl font-semibold hover:bg-blue-600 transition flex items-center justify-center">
                      <Sparkles size={20} className="mr-2"/>
                      AI 챌린지 추천받기
                  </button>
                </>
              )}
  
              {step === 2 && (
                <>
                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext">그룹 챌린지 목표</label>
                    {error && <p className="text-orange-500 text-center text-xs mb-2">{error}</p>}
                    <div className="space-y-2 bg-gray-90 dark:bg-dark-bg p-3 rounded-lg border border-gray-70 dark:border-dark-border">
                      {suggestions.map((s, i) => (
                        <label key={i} className="flex items-center space-x-3 p-2 rounded-md hover:bg-gray-70 dark:hover:bg-dark-border cursor-pointer">
                          <input type="radio" name="challenge" value={s} checked={!isCustom && selectedChallenge === s} onChange={() => {setSelectedChallenge(s); setIsCustom(false);}} className="h-4 w-4 text-primary-blue focus:ring-primary-blue border-gray-50" />
                          <span className="text-sm text-gray-text dark:text-dark-text">{s}</span>
                        </label>
                      ))}
                      <label className="flex items-center space-x-3 p-2 rounded-md hover:bg-gray-70 dark:hover:bg-dark-border cursor-pointer">
                        <input type="radio" name="challenge" checked={isCustom} onChange={() => setIsCustom(true)} className="h-4 w-4 text-primary-blue focus:ring-primary-blue border-gray-50" />
                        <span className="text-sm text-gray-text dark:text-dark-text">직접 입력</span>
                      </label>
                      {isCustom && (
                        <input type="text" value={customChallenge} onChange={(e) => setCustomChallenge(e.target.value)} placeholder="챌린지 목표를 입력하세요" className="w-full bg-gray-70 dark:bg-dark-border border border-gray-50 dark:border-slate-600 rounded-md p-2 focus:ring-primary-blue focus:border-primary-blue transition text-sm ml-8 -mt-1" />
                      )}
                    </div>
                  </div>
  
                  <div className="space-y-2">
                      <label className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext">친구 초대</label>
                      <div className="max-h-32 overflow-y-auto space-y-2 bg-gray-90 dark:bg-dark-bg p-2 rounded-lg border border-gray-70 dark:border-dark-border">
                          {friends.length > 0 ? friends.map(friend => (
                              <label key={friend.id} className="flex items-center space-x-3 p-2 rounded-md hover:bg-gray-70 dark:hover:bg-dark-border cursor-pointer">
                                  <input type="checkbox" checked={selectedFriends.includes(friend.id)} onChange={() => handleFriendToggle(friend.id)} className="h-4 w-4 rounded border-gray-50 text-primary-blue focus:ring-primary-blue" />
                                  <img src={friend.avatar} alt={friend.name} className="w-8 h-8 rounded-full" />
                                  <span className="text-gray-text dark:text-dark-text">{friend.name}</span>
                              </label>
                          )) : <p className="text-xs text-center text-gray-subtext dark:text-dark-subtext p-2">초대할 친구가 없습니다.</p>}
                      </div>
                  </div>
                  
                  <button type="submit" className="w-full bg-primary-blue text-white py-3 mt-4 rounded-xl font-semibold hover:bg-blue-600 transition flex items-center justify-center">
                      <Plus size={20} className="mr-2"/>
                      생성하기
                  </button>
                </>
              )}
          </form>
        )}
      </div>
    </div>
  );
};

export default GroupCreateModal;
