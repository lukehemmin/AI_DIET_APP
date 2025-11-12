import React from 'react';
import type { Badge } from '../types';

interface BadgeGridProps {
  allBadges: Badge[];
  unlockedBadgeIds: string[];
}

const BadgeItem: React.FC<{ badge: Badge; isUnlocked: boolean }> = ({ badge, isUnlocked }) => {
  const { icon: Icon, name, description } = badge;

  return (
    <div className={`bg-gray-100 dark:bg-dark-card p-4 rounded-xl flex flex-col items-center text-center space-y-2 transition-all duration-300 ${isUnlocked ? 'grayscale-0' : 'grayscale opacity-60'}`}>
        <div className={`w-16 h-16 rounded-full flex items-center justify-center ${isUnlocked ? 'bg-amber-400/20' : 'bg-gray-70 dark:bg-dark-border'}`}>
            <Icon size={32} className={isUnlocked ? 'text-amber-500' : 'text-gray-subtext dark:text-dark-subtext'} />
        </div>
        <div className="flex-1">
            <p className="font-semibold text-gray-text dark:text-dark-text">{name}</p>
            <p className="text-xs text-gray-subtext dark:text-dark-subtext">{description}</p>
        </div>
    </div>
  );
};

const BadgeGrid: React.FC<BadgeGridProps> = ({ allBadges, unlockedBadgeIds }) => {
  return (
    <div className="grid grid-cols-3 sm:grid-cols-4 gap-4">
      {allBadges.map(badge => (
        <BadgeItem 
          key={badge.id}
          badge={badge}
          isUnlocked={unlockedBadgeIds.includes(badge.id)}
        />
      ))}
    </div>
  );
};

export default BadgeGrid;
