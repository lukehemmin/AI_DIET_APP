import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Camera, X, SwitchCamera } from 'lucide-react';

interface CameraModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCapture: (file: File) => void;
}

const CameraModal: React.FC<CameraModalProps> = ({ isOpen, onClose, onCapture }) => {
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [facingMode, setFacingMode] = useState<'user' | 'environment'>('environment');

  const stopStream = useCallback(() => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
    }
  }, []);

  const startStream = useCallback(async () => {
    stopStream(); // Stop any existing stream
    setError(null);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: facingMode
        }
      });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
    } catch (err) {
      console.error("Error accessing camera:", err);
      setError("카메라에 접근할 수 없습니다. 브라우저 설정을 확인해주세요.");
    }
  }, [facingMode, stopStream]);

  useEffect(() => {
    if (isOpen) {
      startStream();
    } else {
      stopStream();
    }
    // Cleanup on unmount
    return () => stopStream();
  }, [isOpen, startStream, stopStream]);
  
  const handleToggleFacingMode = () => {
      setFacingMode(prev => prev === 'user' ? 'environment' : 'user');
  };

  useEffect(() => {
      if(isOpen) {
          startStream();
      }
  }, [facingMode, isOpen, startStream]);


  const handleCapture = () => {
    if (videoRef.current && canvasRef.current) {
      const video = videoRef.current;
      const canvas = canvasRef.current;
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const context = canvas.getContext('2d');
      if (context) {
        context.drawImage(video, 0, 0, canvas.width, canvas.height);
        canvas.toBlob(blob => {
          if (blob) {
            const file = new File([blob], `capture-${Date.now()}.jpg`, { type: 'image/jpeg' });
            onCapture(file);
          }
        }, 'image/jpeg', 0.95);
      }
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black z-50 flex flex-col items-center justify-center animate-fade-in">
      <video ref={videoRef} autoPlay playsInline className="absolute top-0 left-0 w-full h-full object-cover"></video>
      <canvas ref={canvasRef} className="hidden"></canvas>
      
      {error && (
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-black/50 text-white p-4 rounded-lg text-center">
            <p>{error}</p>
            <button onClick={onClose} className="mt-4 bg-primary-blue px-4 py-2 rounded">닫기</button>
        </div>
      )}

      <button onClick={onClose} className="absolute top-5 right-5 text-white bg-black/30 p-2 rounded-full">
        <X size={28} />
      </button>

      <div className="absolute bottom-10 flex w-full justify-center items-center">
          <button onClick={handleToggleFacingMode} className="absolute left-10 text-white bg-black/30 p-3 rounded-full">
            <SwitchCamera size={24} />
          </button>
          <button onClick={handleCapture} className="w-20 h-20 rounded-full border-4 border-white bg-white/30" aria-label="사진 촬영"></button>
      </div>
    </div>
  );
};

export default CameraModal;
