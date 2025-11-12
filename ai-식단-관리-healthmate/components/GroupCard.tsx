import React, { useState } from 'react';
import type { Group } from '../types';
import { Users, MoreVertical, LogOut } from 'lucide-react';

interface GroupCardProps {
    group: Group;
    onDelete: (groupId: number) => void;
    onSelect: (groupId: number) => void;
}

const GroupCard: React.FC<GroupCardProps> = ({ group, onDelete, onSelect }) => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const handleDelete = (e: React.MouseEvent) => {
        e.stopPropagation(); // Prevent card click when clicking menu item
        if (window.confirm(`'${group.name}' 그룹에서 정말 탈퇴하시겠습니까?`)) {
            onDelete(group.id);
        }
        setIsMenuOpen(false);
    }

    const handleMenuToggle = (e: React.MouseEvent) => {
        e.stopPropagation();
        setIsMenuOpen(prev => !prev);
    }

    return (
        <button onClick={() => onSelect(group.id)} className="bg-gray-100 dark:bg-dark-card p-4 rounded-2xl shadow-card space-y-3 w-full text-left hover:ring-2 hover:ring-primary-blue transition">
            <div className="flex justify-between items-start">
                <div>
                    <h3 className="font-bold text-lg text-gray-text dark:text-dark-text">{group.name}</h3>
                    <p className="text-sm text-gray-subtext dark:text-dark-subtext flex items-center">
                        <Users size={14} className="mr-1.5" /> {group.members}명 참여중
                    </p>
                </div>
                <div className="relative">
                    <button onClick={handleMenuToggle} className="p-1 rounded-full hover:bg-gray-70 dark:hover:bg-dark-border z-10">
                        <MoreVertical size={20} className="text-gray-subtext dark:text-dark-subtext" />
                    </button>
                    {isMenuOpen && (
                         <div 
                            className="absolute right-0 mt-2 w-40 bg-gray-100 dark:bg-dark-card border border-gray-70 dark:border-dark-border rounded-lg shadow-lg z-20 animate-fade-in"
                         >
                            <button 
                                onClick={handleDelete}
                                className="w-full text-left px-4 py-2 text-sm text-red-500 hover:bg-red-500/10 flex items-center"
                            >
                                <LogOut size={16} className="mr-2" />
                                그룹 탈퇴
                            </button>
                        </div>
                    )}
                </div>
            </div>
            <div>
                <p className="text-sm font-semibold text-gray-text dark:text-dark-text mb-1">{group.challenge}</p>
                <div className="w-full bg-gray-70 dark:bg-dark-border rounded-full h-2">
                    <div className="bg-primary-blue h-2 rounded-full" style={{ width: `${group.progress}%` }}></div>
                </div>
                <p className="text-right text-xs mt-1 text-gray-subtext dark:text-dark-subtext">{group.progress}% 달성</p>
            </div>
        </button>
    );
};

export default GroupCard;