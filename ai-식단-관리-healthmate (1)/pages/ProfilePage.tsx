import React, { useMemo, useState } from 'react';
import { User, Settings, Flame, Users, UserPlus, Trophy } from 'lucide-react';
import type { UserProfile, Friend } from '../types';
import { ACTIVITY_LEVELS } from '../constants';
import { allBadges } from '../services/badgeService';
import BadgeGrid from '../components/BadgeGrid';

interface ProfilePageProps {
  userProfile: UserProfile;
  friends: Friend[];
  onEdit: () => void;
  onOpenAddFriendModal: () => void;
  onOpenSettings: () => void;
}

const InfoRow: React.FC<{ label: string; value: string; unit?: string }> = ({ label, value, unit }) => (
    <div className="flex justify-between items-center py-3">
        <span className="text-gray-subtext dark:text-dark-subtext">{label}</span>
        <span className="font-semibold text-gray-text dark:text-dark-text">{value} <span className="text-sm font-normal">{unit}</span></span>
    </div>
);

const ProfilePage: React.FC<ProfilePageProps> = ({ userProfile, friends, onEdit, onOpenAddFriendModal, onOpenSettings }) => {
  const [activeTab, setActiveTab] = useState<'info' | 'achievements' | 'friends'>('info');
  const { gender, age, height, weight, activityLevel, unlockedBadgeIds } = userProfile;

  const bmr = useMemo(() => {
    if (gender === 'male') {
        return Math.round(10 * weight + 6.25 * height - 5 * age + 5);
    }
    return Math.round(10 * weight + 6.25 * height - 5 * age - 161);
  }, [gender, age, height, weight]);
  
  const dailyGoal = Math.round(bmr * ACTIVITY_LEVELS[activityLevel]);

  return (
    <div className="space-y-8 animate-fade-in">
      <div className="pt-8">
          <div className="flex justify-between items-start">
            <div className="w-1/4"></div>
            <div className="flex flex-col items-center">
                <div className="relative">
                    <img 
                        src="https://i.pravatar.cc/150?u=a042581f4e29026704d" 
                        alt="프로필 사진"
                        className="w-24 h-24 rounded-full object-cover ring-4 ring-primary-blue/30"
                    />
                    <button onClick={onEdit} className="absolute bottom-0 right-0 bg-primary-blue p-1.5 rounded-full text-white hover:bg-blue-600 transition">
                        <User size={16} />
                    </button>
                </div>
                <h2 className="text-xl font-bold mt-4 text-gray-text dark:text-dark-text">박상호</h2>
                <p className="text-gray-subtext dark:text-dark-subtext">since 2024. 05. 21</p>
            </div>
            <div className="w-1/4 flex justify-end">
                <button onClick={onOpenSettings} className="p-2 rounded-full text-gray-subtext dark:text-dark-subtext hover:bg-gray-200 dark:hover:bg-slate-700">
                    <Settings size={22} />
                </button>
            </div>
          </div>
      </div>

      <div>
        <div className="border-b border-gray-70 dark:border-dark-border mb-4">
          <nav className="-mb-px flex space-x-6">
            <button
              onClick={() => setActiveTab('info')}
              className={`py-3 px-1 border-b-2 font-semibold ${
                activeTab === 'info'
                  ? 'border-primary-blue text-primary-blue'
                  : 'border-transparent text-gray-subtext dark:text-dark-subtext hover:border-gray-50 dark:hover:border-slate-600'
              }`}
            >
              내 정보
            </button>
            <button
              onClick={() => setActiveTab('achievements')}
              className={`py-3 px-1 border-b-2 font-semibold ${
                activeTab === 'achievements'
                  ? 'border-primary-blue text-primary-blue'
                  : 'border-transparent text-gray-subtext dark:text-dark-subtext hover:border-gray-50 dark:hover:border-slate-600'
              }`}
            >
              업적
            </button>
            <button
              onClick={() => setActiveTab('friends')}
              className={`py-3 px-1 border-b-2 font-semibold ${
                activeTab === 'friends'
                  ? 'border-primary-blue text-primary-blue'
                  : 'border-transparent text-gray-subtext dark:text-dark-subtext hover:border-gray-50 dark:hover:border-slate-600'
              }`}
            >
              친구 ({friends.length})
            </button>
          </nav>
        </div>

        {activeTab === 'info' && (
          <div className="space-y-4 animate-fade-in">
            <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
                <h3 className="font-semibold text-lg text-gray-text dark:text-dark-text mb-2 flex items-center"><User size={20} className="mr-2 text-primary-blue"/>기본 정보</h3>
                <div className="divide-y divide-gray-70 dark:divide-dark-border">
                    <InfoRow label="성별" value={gender === 'male' ? '남성' : '여성'} />
                    <InfoRow label="나이" value={String(age)} unit="세" />
                    <InfoRow label="키" value={String(height)} unit="cm" />
                    <InfoRow label="현재 체중" value={String(weight)} unit="kg" />
                </div>
            </div>
             <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
                <h3 className="font-semibold text-lg text-gray-text dark:text-dark-text mb-2 flex items-center"><Flame size={20} className="mr-2 text-primary-blue"/>나의 목표</h3>
                 <div className="divide-y divide-gray-70 dark:divide-dark-border">
                    <InfoRow label="기초대사량(BMR)" value={bmr.toLocaleString()} unit="kcal" />
                    <InfoRow label="일일 목표 섭취량" value={dailyGoal.toLocaleString()} unit="kcal" />
                </div>
            </div>
          </div>
        )}

        {activeTab === 'achievements' && (
            <div className="space-y-4 animate-fade-in">
                <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">나의 업적 ({unlockedBadgeIds.length}/{allBadges.length})</h2>
                <BadgeGrid allBadges={allBadges} unlockedBadgeIds={unlockedBadgeIds} />
            </div>
        )}

        {activeTab === 'friends' && (
            <div className="space-y-4 animate-fade-in">
                <div className="flex justify-between items-center">
                    <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">친구 목록</h2>
                    <button 
                        onClick={onOpenAddFriendModal}
                        className="flex items-center text-sm font-semibold text-primary-blue hover:opacity-80 transition-opacity"
                    >
                        <UserPlus size={18} className="mr-1.5" />
                        친구 추가
                    </button>
                </div>
                {friends.length > 0 ? (
                    friends.map(friend => (
                        <div key={friend.id} className="bg-gray-100 dark:bg-dark-card p-4 rounded-xl flex items-center space-x-4">
                            <img src={friend.avatar} alt={friend.name} className="w-10 h-10 rounded-full"/>
                            <p className="font-semibold text-gray-text dark:text-dark-text">{friend.name}</p>
                        </div>
                    ))
                ) : (
                    <div className="text-center py-10 bg-gray-100 dark:bg-dark-card rounded-xl">
                        <p className="text-gray-subtext dark:text-dark-subtext">아직 친구가 없습니다.</p>
                    </div>
                )}
            </div>
        )}
      </div>
    </div>
  );
};

export default ProfilePage;