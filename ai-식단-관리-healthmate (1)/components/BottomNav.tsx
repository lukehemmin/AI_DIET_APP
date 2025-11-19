
import React from 'react';
import { Home, BarChart3, Users, UserCircle, MessageCircle } from 'lucide-react';
import type { Page } from '../types';

interface BottomNavProps {
  activePage: Page;
  onNavigate: (page: Page) => void;
}

const navItems = [
  { id: 'home' as Page, label: '홈', icon: Home },
  { id: 'analysis' as Page, label: '분석', icon: BarChart3 },
  { id: 'chat' as Page, label: 'AI 채팅', icon: MessageCircle },
  { id: 'groups' as Page, label: '그룹', icon: Users },
  { id: 'profile' as Page, label: '프로필', icon: UserCircle },
];

const BottomNav: React.FC<BottomNavProps> = ({ activePage, onNavigate }) => {
  return (
    <nav className="fixed bottom-0 left-0 right-0 h-20 bg-gray-100/80 dark:bg-dark-card/80 backdrop-blur-md border-t border-gray-70 dark:border-dark-border z-40">
      <div className="max-w-3xl mx-auto flex justify-around items-center h-full">
        {navItems.map(item => (
          <button
            key={item.id}
            onClick={() => onNavigate(item.id)}
            className="flex flex-col items-center justify-center w-full h-full space-y-1 transition-colors"
            aria-current={activePage === item.id ? 'page' : undefined}
          >
            <item.icon className={`w-6 h-6 ${activePage === item.id ? 'text-primary-blue' : 'text-gray-subtext dark:text-dark-subtext'}`} />
            <span className={`text-xs font-semibold ${activePage === item.id ? 'text-primary-blue' : 'text-gray-subtext dark:text-dark-subtext'}`}>
              {item.label}
            </span>
          </button>
        ))}
      </div>
    </nav>
  );
};

export default BottomNav;
