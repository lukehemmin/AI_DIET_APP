import React from 'react';
import type { Group } from '../types';
import { Plus } from 'lucide-react';
import GroupCard from '../components/GroupCard';

interface GroupsPageProps {
  groups: Group[];
  onOpenCreateModal: () => void;
  onDeleteGroup: (groupId: number) => void;
  onSelectGroup: (groupId: number) => void;
}

const GroupsPage: React.FC<GroupsPageProps> = ({ groups, onOpenCreateModal, onDeleteGroup, onSelectGroup }) => {
    return (
        <div className="space-y-8 animate-fade-in">
            <div>
                <h1 className="text-2xl font-bold text-gray-text dark:text-dark-text">그룹</h1>
                <p className="text-gray-subtext dark:text-dark-subtext">친구들과 함께 챌린지에 도전해보세요.</p>
            </div>

            {/* 내 그룹 섹션 */}
            <div className="space-y-4">
                 <div className="flex justify-between items-center">
                    <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">내 그룹</h2>
                    <button 
                        onClick={onOpenCreateModal}
                        className="flex items-center text-sm font-semibold text-primary-blue hover:opacity-80 transition-opacity"
                    >
                        <Plus size={18} className="mr-1.5" />
                        그룹 만들기
                    </button>
                </div>
                {groups.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {groups.map(group => 
                            <GroupCard 
                                key={group.id} 
                                group={group} 
                                onDelete={onDeleteGroup} 
                                onSelect={onSelectGroup} 
                            />
                        )}
                    </div>
                ) : (
                    <div className="text-center py-10 bg-gray-100 dark:bg-dark-card rounded-xl">
                        <p className="text-gray-subtext dark:text-dark-subtext">아직 참여중인 그룹이 없습니다.</p>
                        <p className="text-sm text-gray-500 dark:text-slate-500">'그룹 만들기'를 통해 친구들을 초대해보세요!</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default GroupsPage;
