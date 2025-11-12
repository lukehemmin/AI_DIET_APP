import React from 'react';
import type { ChallengeProgress, AIChallenge } from '../types';
import AIChallengeCard from '../components/AIChallengeCard';

const ChallengeCard: React.FC<{ icon: React.ElementType; title: string; description: string; progress: number; status: '진행중' | '완료' }> = ({ icon: Icon, title, description, progress, status }) => {
    const isCompleted = status === '완료';
    return (
        <div className="bg-gray-100 dark:bg-dark-card p-5 rounded-2xl shadow-card flex flex-col space-y-3">
            <div className="flex items-center space-x-4">
                <div className={`p-3 rounded-full ${isCompleted ? 'bg-state-success/20' : 'bg-primary-blue/20'}`}>
                    <Icon className={`w-6 h-6 ${isCompleted ? 'text-state-success' : 'text-primary-blue'}`} />
                </div>
                <div className="flex-1">
                    <h3 className="font-semibold text-gray-text dark:text-dark-text">{title}</h3>
                    <p className="text-sm text-gray-subtext dark:text-dark-subtext">{description}</p>
                </div>
                <span className={`text-xs font-bold px-2 py-1 rounded-full ${isCompleted ? 'bg-state-success/20 text-state-success' : 'bg-orange-500/20 text-orange-500'}`}>{status}</span>
            </div>
            <div>
                <div className="w-full bg-gray-70 dark:bg-dark-border rounded-full h-2">
                    <div 
                        className={`h-2 rounded-full transition-all duration-500 ${isCompleted ? 'bg-state-success' : 'bg-primary-blue'}`} 
                        style={{ width: `${progress}%` }}
                    ></div>
                </div>
                <p className="text-right text-xs mt-1 text-gray-subtext dark:text-dark-subtext">{progress}% 달성</p>
            </div>
        </div>
    );
};

interface ChallengesPageProps {
  challenges: ChallengeProgress[];
  aiChallenge: AIChallenge | null;
  isFetchingAiChallenge: boolean;
  onFetchAiChallenge: () => void;
}

const ChallengesPage: React.FC<ChallengesPageProps> = ({ challenges, aiChallenge, isFetchingAiChallenge, onFetchAiChallenge }) => {
  return (
    <div className="space-y-8 animate-fade-in">
       <div>
        <h1 className="text-2xl font-bold text-gray-text dark:text-dark-text">AI 챌린지 및 개인 목표</h1>
        <p className="text-gray-subtext dark:text-dark-subtext">AI가 제안하는 맞춤 퀘스트와 장기 목표를 달성해보세요.</p>
       </div>

      <AIChallengeCard
        challenge={aiChallenge}
        isLoading={isFetchingAiChallenge}
        onGenerateNew={onFetchAiChallenge}
      />

      <div className="space-y-4">
         <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">나의 장기 목표</h2>
        {challenges.length > 0 ? (
          challenges.map((challenge) => (
            <ChallengeCard 
              key={challenge.id} 
              icon={challenge.icon}
              title={challenge.title}
              description={`${challenge.description} (${challenge.current}/${challenge.goal}일)`}
              progress={challenge.progress}
              status={challenge.status}
            />
          ))
        ) : (
          <div className="text-center py-10 bg-gray-100 dark:bg-dark-card rounded-xl">
            <p className="text-gray-subtext dark:text-dark-subtext">챌린지를 불러오는 중입니다...</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default ChallengesPage;