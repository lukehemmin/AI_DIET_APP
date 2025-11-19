import React, { useEffect } from 'react';
import { CheckCircle, Trophy } from 'lucide-react';

interface ToastProps {
  message: string;
  show: boolean;
  type: 'success' | 'achievement';
  onClose: () => void;
}

const toastStyles = {
  success: {
    bg: 'bg-state-success',
    icon: <CheckCircle size={20} />,
  },
  achievement: {
    bg: 'bg-gradient-to-r from-orange-500 to-amber-500',
    icon: <Trophy size={20} />,
  },
};

const Toast: React.FC<ToastProps> = ({ message, show, type, onClose }) => {
  useEffect(() => {
    if (show) {
      const timer = setTimeout(() => {
        onClose();
      }, 3000); // 3초 후 자동으로 사라집니다.
      return () => clearTimeout(timer);
    }
  }, [show, onClose]);

  if (!show) return null;

  const { bg, icon } = toastStyles[type];

  return (
    <div className={`fixed bottom-24 left-1/2 -translate-x-1/2 ${bg} text-white py-3 px-6 rounded-full shadow-lg flex items-center space-x-2 animate-slide-up z-50`}>
      {icon}
      <span>{message}</span>
    </div>
  );
};

export default Toast;
