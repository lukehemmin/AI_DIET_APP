import React, { useState, useEffect, useRef } from 'react';
import { Sun, Moon, ChevronLeft, ChevronRight } from 'lucide-react';
import type { Theme } from '../types';

interface HeaderProps {
  currentDate: Date;
  onDateChange: (direction: 'prev' | 'next') => void;
  onGoToToday: () => void;
  onOpenCalendar: () => void;
  theme: Theme;
  onSetTheme: (theme: Theme) => void;
}

const Header: React.FC<HeaderProps> = ({ currentDate, onDateChange, onGoToToday, onOpenCalendar, theme, onSetTheme }) => {
  const [isDark, setIsDark] = useState(false);

  // FIX: Use ReturnType<typeof setTimeout> for browser compatibility instead of NodeJS.Timeout.
  const longPressTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (theme === 'system') {
      setIsDark(window.matchMedia('(prefers-color-scheme: dark)').matches);
    } else {
      setIsDark(theme === 'dark');
    }
  }, [theme]);
  
  const toggleTheme = () => {
    const newTheme = isDark ? 'light' : 'dark';
    onSetTheme(newTheme);
    localStorage.setItem('theme', newTheme);
  };

  const formatDate = (date: Date): string => {
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return '오늘';
    }
    if (date.toDateString() === yesterday.toDateString()) {
      return '어제';
    }
    return `${date.getMonth() + 1}월 ${date.getDate()}일`;
  };

  const isTodayDate = currentDate.toDateString() === new Date().toDateString();

  const handlePressStart = () => {
    longPressTimer.current = setTimeout(() => {
      onOpenCalendar();
      longPressTimer.current = null; // Prevent click from firing after long press
    }, 500); // 500ms for long press
  };

  const handlePressEnd = () => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
      longPressTimer.current = null;
      // If it's not today, a short press navigates to today
      if (!isTodayDate) {
        onGoToToday();
      }
    }
  };

  return (
    <header className="bg-gray-100/80 dark:bg-dark-card/80 backdrop-blur-md sticky top-0 z-40 border-b border-gray-70 dark:border-dark-border transition-colors duration-300">
      <div className="max-w-3xl mx-auto px-4 sm:px-6">
        <div className="relative flex items-center justify-center h-16">
          <div className="flex items-center space-x-2">
            <button onClick={() => onDateChange('prev')} className="p-2 rounded-full hover:bg-gray-200 dark:hover:bg-slate-700 transition-colors">
              <ChevronLeft size={24} className="text-gray-text dark:text-dark-text" />
            </button>
            <div
              className="w-28 text-center"
              onMouseDown={handlePressStart}
              onMouseUp={handlePressEnd}
              onMouseLeave={handlePressEnd} // Cancel if mouse leaves
              onTouchStart={handlePressStart}
              onTouchEnd={handlePressEnd}
              role="button"
              tabIndex={0}
              aria-label="날짜 선택. 클릭하여 오늘로 이동, 길게 눌러 달력 열기"
            >
              <h2 className="text-lg font-semibold text-gray-text dark:text-dark-text tabular-nums cursor-pointer">
                {formatDate(currentDate)}
              </h2>
            </div>
            <button 
              onClick={() => onDateChange('next')} 
              disabled={isTodayDate} 
              className="p-2 rounded-full hover:bg-gray-200 dark:hover:bg-slate-700 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              aria-label="다음 날짜"
            >
              <ChevronRight size={24} className="text-gray-text dark:text-dark-text" />
            </button>
          </div>
          <div className="absolute right-0 top-1/2 -translate-y-1/2">
            <button onClick={toggleTheme} aria-label="Toggle dark mode" className="p-1 rounded-full text-gray-subtext dark:text-dark-subtext hover:bg-gray-200 dark:hover:bg-slate-700">
              {isDark ? <Sun className="w-6 h-6 text-yellow-400" /> : <Moon className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;