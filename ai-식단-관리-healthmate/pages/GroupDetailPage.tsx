import React from 'react';
import type { Group, GroupFeedItem } from '../types';
import { ChevronLeft, Users, Trophy } from 'lucide-react';
import FeedItem from '../components/FeedItem';

interface GroupDetailPageProps {
    group: Group;
    feed: GroupFeedItem[];
    onLeave: () => void;
    onToggleLike: (feedItemId: number) => void;
}

const GroupDetailPage: React.FC<GroupDetailPageProps> = ({ group, feed, onLeave, onToggleLike }) => {
    // This now correctly filters the feed for the specific group
    const groupFeed = feed.filter(item => item.groupId === group.id); 

    // Mock members for UI display
    const mockMembers = [
        { name: "박상호", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026704d" },
        { name: "안병은", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026705d" },
        { name: "김철수", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026706d" },
        { name: "이영희", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026707d" },
        { name: "최민준", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026708d" },
    ];

    return (
        <div className="space-y-6 animate-fade-in">
            <header className="flex items-center space-x-2">
                <button onClick={onLeave} className="p-2 rounded-full hover:bg-gray-70 dark:hover:bg-dark-border">
                    <ChevronLeft size={24} className="text-gray-text dark:text-dark-text" />
                </button>
                <div>
                    <h1 className="text-2xl font-bold text-gray-text dark:text-dark-text">{group.name}</h1>
                    <p className="text-gray-subtext dark:text-dark-subtext">{group.description}</p>
                </div>
            </header>

            {/* 멤버 및 챌린지 정보 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="bg-gray-100 dark:bg-dark-card p-4 rounded-2xl">
                    <h2 className="font-semibold text-gray-text dark:text-dark-text mb-2 flex items-center"><Users size={18} className="mr-2 text-primary-blue"/>멤버 ({group.members}명)</h2>
                    <div className="flex -space-x-2">
                        {mockMembers.slice(0, 5).map(member => (
                            <img key={member.name} src={member.avatar} alt={member.name} title={member.name} className="w-8 h-8 rounded-full ring-2 ring-gray-100 dark:ring-dark-card" />
                        ))}
                    </div>
                </div>
                <div className="bg-gray-100 dark:bg-dark-card p-4 rounded-2xl">
                    <h2 className="font-semibold text-gray-text dark:text-dark-text mb-2 flex items-center"><Trophy size={18} className="mr-2 text-primary-blue"/>진행중인 챌린지</h2>
                    <p className="text-sm font-semibold text-gray-text dark:text-dark-text mb-1">{group.challenge}</p>
                    <div className="w-full bg-gray-70 dark:bg-dark-border rounded-full h-2">
                        <div className="bg-primary-blue h-2 rounded-full" style={{ width: `${group.progress}%` }}></div>
                    </div>
                </div>
            </div>

            {/* 그룹 피드 */}
            <div className="space-y-4">
                <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text">그룹 피드</h2>
                <div className="space-y-0.5">
                    {groupFeed.length > 0 ? (
                        groupFeed.map(item => <FeedItem key={item.id} item={item} onToggleLike={onToggleLike} />)
                    ) : (
                        <div className="text-center py-10 bg-gray-100 dark:bg-dark-card rounded-xl">
                            <p className="text-gray-subtext dark:text-dark-subtext">그룹 활동이 없습니다.</p>
                        </div>
                    )}
                </div>
            </div>

        </div>
    );
};

export default GroupDetailPage;
