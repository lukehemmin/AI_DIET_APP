import React, { useState, FormEvent, useEffect } from 'react';
import type { UserProfile, ActivityLevel } from '../types';
import { X, Save } from 'lucide-react';

interface ProfileEditModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentProfile: UserProfile;
  onSave: (profile: UserProfile) => void;
}

const activityLabels: Record<ActivityLevel, string> = {
  sedentary: '거의 없음 (좌식 생활)',
  light: '가벼움 (주 1-3일)',
  moderate: '보통 (주 3-5일)',
  active: '많음 (주 6-7일)',
  veryActive: '매우 많음 (고강도, 매일)',
};

const InputField: React.FC<{ label: string; type: string; value: string | number; onChange: (e: React.ChangeEvent<HTMLInputElement>) => void; name: string; }> = 
({ label, type, value, onChange, name }) => (
    <div>
        <label htmlFor={name} className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">{label}</label>
        <input
            type={type}
            id={name}
            name={name}
            value={value}
            onChange={onChange}
            className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition"
            required
        />
    </div>
);

const ProfileEditModal: React.FC<ProfileEditModalProps> = ({ isOpen, onClose, currentProfile, onSave }) => {
  const [profile, setProfile] = useState<UserProfile>(currentProfile);

  useEffect(() => {
    setProfile(currentProfile);
  }, [currentProfile, isOpen]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: name === 'age' || name === 'height' || name === 'weight' ? Number(value) : value }));
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    onSave(profile);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in" onClick={onClose}>
      <div className="bg-gray-100 dark:bg-dark-card rounded-2xl shadow-lg w-full max-w-sm p-6 animate-slide-up" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">프로필 수정</h2>
            <button onClick={onClose} className="text-gray-subtext dark:text-dark-subtext hover:text-gray-text dark:hover:text-dark-text">
                <X size={24} />
            </button>
        </div>
        
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="gender" className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">성별</label>
              <select id="gender" name="gender" value={profile.gender} onChange={handleChange} className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition">
                  <option value="male">남성</option>
                  <option value="female">여성</option>
              </select>
            </div>

            <div className="grid grid-cols-3 gap-3">
                <InputField label="나이" type="number" name="age" value={profile.age} onChange={handleChange} />
                <InputField label="키(cm)" type="number" name="height" value={profile.height} onChange={handleChange} />
                <InputField label="체중(kg)" type="number" name="weight" value={profile.weight} onChange={handleChange} />
            </div>

             <div>
                <label htmlFor="activityLevel" className="block text-sm font-medium text-gray-subtext dark:text-dark-subtext mb-1">활동량</label>
                <select id="activityLevel" name="activityLevel" value={profile.activityLevel} onChange={handleChange} className="w-full bg-gray-90 dark:bg-dark-bg border border-gray-70 dark:border-dark-border rounded-lg p-2.5 focus:ring-primary-blue focus:border-primary-blue transition">
                    {Object.entries(activityLabels).map(([key, label]) => (
                        <option key={key} value={key}>{label}</option>
                    ))}
                </select>
            </div>
            
            <button type="submit" className="w-full bg-primary-blue text-white py-3 mt-4 rounded-xl font-semibold hover:bg-blue-600 transition flex items-center justify-center">
                <Save size={20} className="mr-2"/>
                저장하기
            </button>
        </form>
      </div>
    </div>
  );
};

export default ProfileEditModal;
