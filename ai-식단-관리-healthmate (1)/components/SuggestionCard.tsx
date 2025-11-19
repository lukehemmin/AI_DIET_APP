
import React from 'react';
import { Dumbbell, Target, Zap, ChevronRight } from 'lucide-react';

type IconName = 'Dumbbell' | 'Target' | 'Zap';

const icons: Record<IconName, React.ElementType> = {
  Dumbbell,
  Target,
  Zap,
};

const iconStyles: Record<IconName, { bg: string, text: string }> = {
  Dumbbell: { bg: 'bg-orange-100 dark:bg-orange-500/20', text: 'text-orange-500' },
  Target: { bg: 'bg-blue-100 dark:bg-primary-blue/20', text: 'text-primary-blue' },
  Zap: { bg: 'bg-green-100 dark:bg-state-success/20', text: 'text-state-success' },
};


interface SuggestionCardProps {
  icon: IconName;
  title: string;
  description: string;
  ctaText: string;
  onViewReport?: () => void;
  isLoading: boolean;
}

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

const SuggestionCard: React.FC<SuggestionCardProps> = ({ icon, title, description, ctaText, onViewReport, isLoading }) => {
  const IconComponent = icons[icon];
  const styles = iconStyles[icon];

  const handleClick = () => {
    if (ctaText === '건강 리포트 보기' && onViewReport) {
      onViewReport();
    }
    // 다른 CTA에 대한 동작은 여기에 추가할 수 있습니다.
  };

  return (
    <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card flex items-center space-x-4 animate-slide-up">
      <div className={`${styles.bg} p-3 rounded-full`}>
        <IconComponent className={`w-6 h-6 ${styles.text}`} />
      </div>

      {isLoading ? (
        <div className="flex-1 space-y-2 animate-pulse">
            <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-1/3"></div>
            <div className="h-4 bg-gray-70 dark:bg-dark-border rounded w-full"></div>
        </div>
      ) : (
        <div className="flex-1">
            <h3 className="font-semibold text-gray-text dark:text-dark-text">{title}</h3>
            <p className="text-sm text-gray-subtext dark:text-dark-subtext"><MarkdownRenderer text={description} /></p>
        </div>
      )}
      
      <button 
        onClick={handleClick}
        className="flex items-center text-primary-blue font-semibold text-sm hover:opacity-80 transition-opacity"
        disabled={isLoading}
      >
        <span>{ctaText}</span>
        <ChevronRight className="w-4 h-4 ml-1" />
      </button>
    </div>
  );
};

export default SuggestionCard;
