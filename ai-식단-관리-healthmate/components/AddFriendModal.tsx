import React, { useState } from 'react';
import { X, Search, UserPlus } from 'lucide-react';

interface AddFriendModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddFriend: (name: string) => void;
}

// Mock search results for demonstration
const mockSearchResults = [
  { id: 3, name: "이영희", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026707d" },
  { id: 4, name: "최민준", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026708d" },
];

const AddFriendModal: React.FC<AddFriendModalProps> = ({ isOpen, onClose, onAddFriend }) => {
  const [searchTerm, setSearchTerm] = useState('');

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in" onClick={onClose}>
      <div className="bg-gray-100 dark:bg-dark-card rounded-2xl shadow-lg w-full max-w-sm p-6 animate-slide-up" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">친구 추가</h2>
            <button onClick={onClose} className="text-gray-subtext dark:text-dark-subtext hover:text-gray-text dark:hover:text-dark-text">
                <X size={24} />
            </button>
        </div>
        
        <div className="relative mb-4">
            <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="친구 닉네임 검색"
                className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 pl-10 focus:ring-primary-blue focus:border-primary-blue transition"
            />
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-subtext dark:text-dark-subtext" size={20} />
        </div>

        <div className="space-y-2">
            <h3 className="text-sm font-semibold text-gray-subtext dark:text-dark-subtext">검색 결과</h3>
            {searchTerm && mockSearchResults.map(user => (
                <div key={user.id} className="flex items-center justify-between p-2 rounded-lg hover:bg-gray-90 dark:hover:bg-dark-bg">
                    <div className="flex items-center space-x-3">
                        <img src={user.avatar} alt={user.name} className="w-8 h-8 rounded-full" />
                        <span className="font-semibold text-gray-text dark:text-dark-text">{user.name}</span>
                    </div>
                    <button 
                        onClick={() => onAddFriend(user.name)}
                        className="text-primary-blue hover:opacity-80 transition-opacity p-1"
                    >
                        <UserPlus size={20} />
                    </button>
                </div>
            ))}
            {!searchTerm && (
                <p className="text-xs text-center text-gray-500 dark:text-slate-500 pt-4">검색어를 입력해주세요.</p>
            )}
        </div>
      </div>
    </div>
  );
};

export default AddFriendModal;
