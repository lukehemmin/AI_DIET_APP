import React from 'react';
import { Sparkles, RefreshCw, Zap, Target, Award } from 'lucide-react';
import type { AIChallenge } from '../types';

type IconName = 'Zap' | 'Target' | 'Award';

const icons: Record<IconName, React.ElementType> = { Zap, Target, Award };

const SkeletonLoader: React.FC = () => (
    <div className="flex-1 space-y-2 animate-pulse">
        <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-1/3"></div>
        <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-full"></div>
    </div>
);

const MarkdownRenderer: React.FC<{ text: string }> = ({ text }) => {
  return (
    <>
      {text.split('\n').map((line, lineIndex) => {
        const parts = line.split(/(\*\*.*?\*\*)/g).filter(Boolean);
        return (
          <React.Fragment key={lineIndex}>
            {parts.map((part, partIndex) => {
              if (part.startsWith('**') && part.endsWith('**')) {
                return <strong key={partIndex}>{part.slice(2, -2)}</strong>;
              }
              return <React.Fragment key={partIndex}>{part}</React.Fragment>;
            })}
            {lineIndex < text.split('\n').length - 1 && <br />}
          </React.Fragment>
        );
      })}
    </>
  );
};

interface AIChallengeCardProps {
  challenge: AIChallenge | null;
  isLoading: boolean;
  onGenerateNew: () => void;
}

const AIChallengeCard: React.FC<AIChallengeCardProps> = ({ challenge, isLoading, onGenerateNew }) => {
  const IconComponent = challenge ? icons[challenge.icon] : Zap;

  return (
    <div className="bg-gradient-to-br from-blue-100 to-indigo-100 dark:from-slate-800 dark:to-blue-900/50 p-5 rounded-2xl shadow-card space-y-4">
        <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text flex items-center">
                <Sparkles size={22} className="mr-2 text-primary-blue" />
                AI 맞춤 챌린지
            </h2>
             <button
                onClick={onGenerateNew}
                disabled={isLoading}
                className="flex items-center text-sm font-semibold text-primary-blue hover:opacity-80 transition-opacity disabled:opacity-50 disabled:cursor-wait"
            >
                <RefreshCw size={14} className={`mr-1.5 ${isLoading ? 'animate-spin' : ''}`} />
                다른 챌린지 제안받기
            </button>
        </div>
       
        <div className="flex items-center space-x-4 min-h-[48px]">
            <div className="p-3 bg-white/60 dark:bg-dark-card/50 rounded-full">
                {isLoading ? (
                    <div className="w-6 h-6 bg-gray-70 dark:bg-dark-border rounded-full animate-pulse"></div>
                ) : (
                    <IconComponent className="w-6 h-6 text-primary-blue" />
                )}
            </div>
            
            {isLoading ? (
                <SkeletonLoader />
            ) : challenge ? (
                 <div className="flex-1">
                    <h3 className="font-semibold text-gray-text dark:text-dark-text">{challenge.title}</h3>
                    <p className="text-sm text-gray-subtext dark:text-dark-subtext"><MarkdownRenderer text={challenge.description} /></p>
                </div>
            ) : null}
        </div>
    </div>
  );
};

export default AIChallengeCard;
