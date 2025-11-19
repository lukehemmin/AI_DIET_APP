import React, { useState, FormEvent } from 'react';
import { X, Plus, UserCheck } from 'lucide-react';
import type { Friend } from '../types';

interface GroupCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreate: (groupData: { name: string; description: string }, invitedFriends: number[]) => void;
  friends: Friend[];
}

const GroupCreateModal: React.FC<GroupCreateModalProps> = ({ isOpen, onClose, onCreate, friends }) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [selectedFriends, setSelectedFriends] = useState<number[]>([]);

  const handleFriendToggle = (friendId: number) => {
    setSelectedFriends(prev => 
      prev.includes(friendId) 
        ? prev.filter(id => id !== friendId)
        : [...prev, friendId]
    );
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (name.trim()) {
      onCreate({ name, description }, selectedFriends);
      setName('');
      setDescription('');
      setSelectedFriends([]);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in" onClick={onClose}>
      <div className="bg-gray-100 dark:bg-dark-card rounded-2xl shadow-lg w-full max-w-sm p-6 animate-slide-up" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">새로운 그룹 만들기</h2>
            <button onClick={onClose} className="text-gray-subtext dark:text-dark-subtext hover:text-gray-text dark:hover:text-dark-text">
                <X size={24} />
            </button>
        </div>
        
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
                <label htmlFor="groupName" className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">그룹 이름</label>
                <input
                    type="text"
                    id="groupName"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="예: 여름 준비 다이어트방"
                    className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition"
                    required
                />
            </div>
             <div>
                <label htmlFor="groupDescription" className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">한 줄 설명 (선택)</label>
                <input
                    type="text"
                    id="groupDescription"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="예: 함께 목표를 달성해봐요!"
                    className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition"
                />
            </div>

            <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext">친구 초대</label>
                <div className="max-h-32 overflow-y-auto space-y-2 bg-gray-90 dark:bg-dark-bg p-2 rounded-lg border border-gray-70 dark:border-dark-border">
                    {friends.length > 0 ? friends.map(friend => (
                        <label key={friend.id} className="flex items-center space-x-3 p-2 rounded-md hover:bg-gray-70 dark:hover:bg-dark-border cursor-pointer">
                            <input 
                                type="checkbox"
                                checked={selectedFriends.includes(friend.id)}
                                onChange={() => handleFriendToggle(friend.id)}
                                className="h-4 w-4 rounded border-gray-50 text-primary-blue focus:ring-primary-blue"
                            />
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
        </form>
      </div>
    </div>
  );
};

export default GroupCreateModal;
