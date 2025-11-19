import React, { useState } from 'react';
import type { Theme } from '../types';
import { X, Palette, Bell, Database, Trash2 } from 'lucide-react';

interface SettingsPageProps {
  onClose: () => void;
  theme: Theme;
  onThemeChange: (theme: Theme) => void;
  onDeleteAllData: () => void;
}

const SettingsPage: React.FC<SettingsPageProps> = ({ onClose, theme, onThemeChange, onDeleteAllData }) => {
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);

  const handleThemeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newTheme = e.target.value as Theme;
    onThemeChange(newTheme);
    localStorage.setItem('theme', newTheme);
  };
  
  const handleDeleteData = () => {
    if (window.confirm('정말로 모든 식단 기록과 데이터를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      onDeleteAllData();
    }
  };

  return (
    <div className="fixed inset-0 bg-gray-90 dark:bg-dark-bg z-50 animate-fade-in overflow-y-auto">
      <div className="max-w-3xl mx-auto p-4 md:p-6">
        <header className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-text dark:text-dark-text">설정</h1>
            <p className="text-gray-subtext dark:text-dark-subtext">앱의 동작을 제어하세요.</p>
          </div>
          <button onClick={onClose} className="p-2 rounded-full hover:bg-gray-70 dark:hover:bg-dark-border">
            <X size={24} />
          </button>
        </header>

        <div className="space-y-6">
          {/* 테마 설정 */}
          <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
            <h2 className="text-lg font-semibold text-gray-text dark:text-dark-text mb-4 flex items-center">
              <Palette size={20} className="mr-3 text-primary-blue" />
              테마 설정
            </h2>
            <div className="flex space-x-4">
              {(['light', 'dark', 'system'] as Theme[]).map((t) => (
                <label key={t} className="flex items-center space-x-2 cursor-pointer">
                  <input
                    type="radio"
                    name="theme"
                    value={t}
                    checked={theme === t}
                    onChange={handleThemeChange}
                    className="h-4 w-4 text-primary-blue focus:ring-primary-blue border-gray-50"
                  />
                  <span className="capitalize text-gray-subtext dark:text-dark-subtext">{t === 'light' ? '라이트' : t === 'dark' ? '다크' : '시스템'}</span>
                </label>
              ))}
            </div>
          </div>
          
          {/* 알림 설정 */}
          <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
            <h2 className="text-lg font-semibold text-gray-text dark:text-dark-text mb-2 flex items-center">
              <Bell size={20} className="mr-3 text-primary-blue" />
              알림
            </h2>
            <div className="flex justify-between items-center">
                <p className="text-gray-subtext dark:text-dark-subtext">운동 추천 및 챌린지 알림</p>
                <button
                    onClick={() => setNotificationsEnabled(!notificationsEnabled)}
                    className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                        notificationsEnabled ? 'bg-primary-blue' : 'bg-gray-70 dark:bg-dark-border'
                    }`}
                >
                    <span
                        className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                        notificationsEnabled ? 'translate-x-6' : 'translate-x-1'
                        }`}
                    />
                </button>
            </div>
          </div>

          {/* 데이터 관리 */}
          <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card">
            <h2 className="text-lg font-semibold text-gray-text dark:text-dark-text mb-4 flex items-center">
              <Database size={20} className="mr-3 text-primary-blue" />
              데이터 관리
            </h2>
            <div className="space-y-3">
                <button className="w-full text-left text-gray-subtext dark:text-dark-subtext p-3 rounded-lg hover:bg-gray-70 dark:hover:bg-dark-border transition-colors">
                    데이터 내보내기 (CSV)
                </button>
                 <button 
                    onClick={handleDeleteData}
                    className="w-full text-left text-red-500 font-semibold p-3 rounded-lg hover:bg-red-500/10 transition-colors flex items-center"
                >
                    <Trash2 size={18} className="mr-2"/>
                    모든 기록 삭제하기
                </button>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};

export default SettingsPage;