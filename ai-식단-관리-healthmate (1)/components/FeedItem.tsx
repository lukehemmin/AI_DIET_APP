import React from 'react';
import type { GroupFeedItem } from '../types';
import { Award, UtensilsCrossed, ThumbsUp } from 'lucide-react';

const formatRelativeTime = (date: Date) => {
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const diffSeconds = Math.floor(diff / 1000);
    const diffMinutes = Math.floor(diffSeconds / 60);
    const diffHours = Math.floor(diffMinutes / 60);

    if (diffSeconds < 60) return "방금 전";
    if (diffMinutes < 60) return `${diffMinutes}분 전`;
    if (diffHours < 24) return `${diffHours}시간 전`;
    return `${date.toLocaleDateString('ko-KR')}`;
}

interface FeedItemProps {
    item: GroupFeedItem;
    onToggleLike: (id: number) => void;
}

const FeedItem: React.FC<FeedItemProps> = ({ item, onToggleLike }) => {
    const Icon = item.type === 'challenge' ? Award : UtensilsCrossed;
    
    return (
        <div className="bg-gray-100 dark:bg-dark-card p-4 rounded-xl">
            <div className="flex items-start space-x-3">
                <div className="bg-gray-70 dark:bg-dark-border p-2.5 rounded-full mt-1">
                    <Icon size={18} className="text-gray-subtext dark:text-dark-subtext" />
                </div>
                <div className="flex-1">
                    <p className="text-sm text-gray-text dark:text-dark-text">
                        <strong>{item.userName}</strong>님이 {item.content}
                    </p>
                    <p className="text-xs text-gray-500 dark:text-slate-500 mt-1">{formatRelativeTime(item.timestamp)}</p>
                </div>
            </div>
            <div className="mt-2 pl-12 flex items-center space-x-4">
                 <button 
                    onClick={() => onToggleLike(item.id)}
                    className={`flex items-center space-x-1.5 text-xs font-semibold rounded-full px-2.5 py-1 transition-colors ${
                        item.likedByMe 
                        ? 'text-primary-blue bg-primary-blue/10'
                        : 'text-gray-subtext dark:text-dark-subtext hover:bg-gray-70 dark:hover:bg-dark-border'
                    }`}
                >
                    <ThumbsUp size={14} />
                    <span>응원하기</span>
                </button>
                {item.likes > 0 && (
                     <p className="text-xs text-gray-subtext dark:text-dark-subtext">
                        <strong>{item.likes}명</strong>이 응원합니다.
                    </p>
                )}
            </div>
        </div>
    );
};

export default FeedItem;