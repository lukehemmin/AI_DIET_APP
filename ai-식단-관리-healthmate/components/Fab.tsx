import React, { useRef, useState } from 'react';
import { Camera, Plus, ImageUp } from 'lucide-react';

interface FabProps {
  onFileChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  onTakePhoto: () => void;
}

const Fab: React.FC<FabProps> = ({ onFileChange, onTakePhoto }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isOpen, setIsOpen] = useState(false);

  const handleUploadClick = () => {
    fileInputRef.current?.click();
    setIsOpen(false);
  };

  const handleCameraClick = () => {
    onTakePhoto();
    setIsOpen(false);
  }

  return (
    <div className="fixed bottom-24 right-6 z-50">
      {isOpen && (
        <div className="flex flex-col items-center space-y-3 mb-4 animate-slide-up">
          <button
            onClick={handleCameraClick}
            className="bg-gray-100 dark:bg-dark-card text-primary-blue w-14 h-14 rounded-full flex items-center justify-center shadow-md hover:bg-gray-200 dark:hover:bg-slate-600 transition-transform transform hover:scale-110"
            aria-label="카메라로 사진 촬영"
          >
            <Camera size={24} />
          </button>
          <button
            onClick={handleUploadClick}
            className="bg-gray-100 dark:bg-dark-card text-primary-blue w-14 h-14 rounded-full flex items-center justify-center shadow-md hover:bg-gray-200 dark:hover:bg-slate-600 transition-transform transform hover:scale-110"
            aria-label="갤러리에서 사진 선택"
          >
            <ImageUp size={24} />
          </button>
        </div>
      )}

      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`bg-primary-blue text-white w-16 h-16 rounded-full flex items-center justify-center shadow-lg dark:shadow-blue-900/50 hover:bg-blue-600 transition-transform transform hover:scale-110 ${isOpen ? 'rotate-45' : ''}`}
        aria-label={isOpen ? '옵션 닫기' : '식단 사진으로 추가'}
      >
        <Plus size={32} />
      </button>
      <input
        type="file"
        ref={fileInputRef}
        onChange={onFileChange}
        className="hidden"
        accept="image/*"
        multiple
      />
    </div>
  );
};

export default Fab;
