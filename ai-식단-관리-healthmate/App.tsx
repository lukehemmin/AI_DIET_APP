

import React, { useState, useCallback, useMemo, useEffect, useRef } from 'react';

import type { Meal, AnalysisResult, Page, UserProfile, ChallengeProgress, Group, GroupFeedItem, Friend, MealTime, Theme } from './types';
import { analyzeImageWithGemini, getExerciseSuggestion, analyzeTextWithGemini } from './services/geminiService';
import { fileToBase64 } from './utils/fileUtils';
import { DAILY_PROTEIN_GOAL, ACTIVITY_LEVELS, DAILY_WATER_GOAL } from './constants';
import { calculateChallengesProgress } from './services/challengeService';
import { allBadges, checkNewBadges } from './services/badgeService';


import Header from './components/Header';
import AnalysisModal from './components/AnalysisModal';
import Fab from './components/Fab';
import Toast from './components/Toast';
import ManualAddModal from './components/ManualAddModal';
import BottomNav from './components/BottomNav';
import ProfileEditModal from './components/ProfileEditModal';
import GroupCreateModal from './components/GroupCreateModal';
import AddFriendModal from './components/AddFriendModal';
import CameraModal from './components/CameraModal';
import CalendarModal from './components/CalendarModal';
import ProteinSuggestionModal from './components/ProteinSuggestionModal';


// Page Components
import HomePage from './pages/HomePage';
import AnalysisPage from './pages/AnalysisPage';
import ProfilePage from './pages/ProfilePage';
import GroupsPage from './pages/GroupsPage';
import GroupDetailPage from './pages/GroupDetailPage';
import ReportPage from './pages/ReportPage';
import ChatPage from './pages/ChatPage';
import SettingsPage from './pages/SettingsPage';

// FIX: Define a specific type for the suggestion state to allow different icon types.
type SuggestionIcon = 'Dumbbell' | 'Target' | 'Zap';

interface SuggestionState {
  icon: SuggestionIcon;
  title: string;
  description: string;
  ctaText: string;
  isLoading: boolean;
}

interface ToastState {
  show: boolean;
  message: string;
  type: 'success' | 'achievement';
}

const App: React.FC = () => {
  const [meals, setMeals] = useState<Meal[]>([]);
  const [activePage, setActivePage] = useState<Page>('home');
  const [activeGroupId, setActiveGroupId] = useState<number | null>(null);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [waterIntake, setWaterIntake] = useState<Record<string, number>>({});

  const [userProfile, setUserProfile] = useState<UserProfile>({
    gender: 'male',
    age: 30,
    height: 178,
    weight: 75,
    activityLevel: 'moderate',
    unlockedBadgeIds: [],
  });
  
  const [friends, setFriends] = useState<Friend[]>([
    { id: 1, name: "안병은", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026705d" },
    { id: 2, name: "김철수", avatar: "https://i.pravatar.cc/150?u=a042581f4e29026706d" },
  ]);

  const [groups, setGroups] = useState<Group[]>([
    { id: 1, name: "여름 준비 다이어트방", description: "여름까지 함께 달려봐요!", members: 5, challenge: "그룹 칼로리 50,000kcal 소모", progress: 78 },
    { id: 2, name: "단백질 용사들", description: "근손실은 절대 못 참지", members: 3, challenge: "이번 주 단백질 섭취 1등하기", progress: 45 },
  ]);

  const [groupFeed, setGroupFeed] = useState<GroupFeedItem[]>([]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState<AnalysisResult[] | null>(null);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<ToastState>({ show: false, message: '', type: 'success' });
  const [isManualAddModalOpen, setIsManualAddModalOpen] = useState(false);
  const [manualAddPrefill, setManualAddPrefill] = useState<string | null>(null);
  const [isProfileEditModalOpen, setIsProfileEditModalOpen] = useState(false);
  const [isGroupCreateModalOpen, setIsGroupCreateModalOpen] = useState(false);
  const [isAddFriendModalOpen, setIsAddFriendModalOpen] = useState(false);
  const [isReportOpen, setIsReportOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isCameraModalOpen, setIsCameraModalOpen] = useState(false);
  const [isCalendarModalOpen, setIsCalendarModalOpen] = useState(false);
  const [isProteinModalOpen, setIsProteinModalOpen] = useState(false);
  const [proteinModalShownForDate, setProteinModalShownForDate] = useState<string | null>(null);

  // Multiple photo analysis state
  const [analysisQueue, setAnalysisQueue] = useState<File[]>([]);
  const [currentAnalysisIndex, setCurrentAnalysisIndex] = useState(0);

  const [theme, setTheme] = useState<Theme>(() => {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light' || savedTheme === 'dark') {
      return savedTheme;
    }
    return 'system';
  });
  
  // FIX: Provide the explicit `SuggestionState` type to `useState` to resolve type inference issues.
  const [suggestion, setSuggestion] = useState<SuggestionState>({
    icon: 'Zap',
    title: '로드 중...',
    description: 'AI가 맞춤 제안을 생성하고 있습니다.',
    ctaText: '잠시만 기다려주세요',
    isLoading: true,
  });

  useEffect(() => {
    const root = window.document.documentElement;
    const isDark = theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches);
    
    root.classList.toggle('dark', isDark);

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = () => {
        if (theme === 'system') {
            root.classList.toggle('dark', mediaQuery.matches);
        }
    };
    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, [theme]);

  const showToast = (message: string, type: 'success' | 'achievement' = 'success') => {
    setToast({ show: true, message, type });
  };

  const checkForNewBadges = useCallback((updatedMeals: Meal[], updatedChallenges: ChallengeProgress[], currentWaterIntake: number) => {
    const newBadgeIds = checkNewBadges(updatedMeals, updatedChallenges, currentWaterIntake, userProfile.unlockedBadgeIds);
    if (newBadgeIds.length > 0) {
      setUserProfile(prev => ({ ...prev, unlockedBadgeIds: [...prev.unlockedBadgeIds, ...newBadgeIds] }));
      
      setTimeout(() => {
        newBadgeIds.forEach(id => {
          const badgeInfo = allBadges.find(b => b.id === id);
          if (badgeInfo) {
            showToast(`업적 달성: ${badgeInfo.name}!`, 'achievement');
          }
        });
      }, 500); // Give a slight delay for visual feedback
    }
  }, [userProfile.unlockedBadgeIds]);

  const handleDateChange = (direction: 'prev' | 'next') => {
    setCurrentDate(prevDate => {
      const newDate = new Date(prevDate);
      if (direction === 'prev') {
        newDate.setDate(newDate.getDate() - 1);
      } else {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        newDate.setDate(newDate.getDate() + 1);
        
        const checkDate = new Date(newDate);
        checkDate.setHours(0, 0, 0, 0);
        
        if (checkDate > today) {
          return prevDate; // Don't go to the future
        }
      }
      return newDate;
    });
  };

  const handleGoToToday = useCallback(() => {
    setCurrentDate(new Date());
  }, []);

  const handleDateSelect = useCallback((date: Date) => {
    setCurrentDate(date);
    setIsCalendarModalOpen(false);
  }, []);

  const handleOpenManualAddModal = useCallback((prefillFoodItem?: string) => {
    setManualAddPrefill(prefillFoodItem || null);
    setIsManualAddModalOpen(true);
  }, []);

  const displayedMeals = useMemo(() => {
    return meals.filter(meal => new Date(meal.date).toDateString() === currentDate.toDateString());
  }, [meals, currentDate]);

  const dailyTotals = useMemo(() => {
    return displayedMeals
      .reduce((acc, meal) => {
        acc.kcal += meal.kcal;
        acc.carbs += meal.macro.carbs;
        acc.protein += meal.macro.protein;
        acc.fat += meal.macro.fat;
        return acc;
      }, { kcal: 0, carbs: 0, protein: 0, fat: 0 });
  }, [displayedMeals]);


  const dailyCalorieGoal = useMemo(() => {
    const { gender, age, height, weight, activityLevel } = userProfile;
    // Mifflin-St Jeor Equation for BMR
    let bmr;
    if (gender === 'male') {
      bmr = 10 * weight + 6.25 * height - 5 * age + 5;
    } else {
      bmr = 10 * weight + 6.25 * height - 5 * age - 161;
    }
    const tdee = bmr * ACTIVITY_LEVELS[activityLevel];
    return Math.round(tdee);
  }, [userProfile]);
  
  const challengesProgress: ChallengeProgress[] = useMemo(() => {
    return calculateChallengesProgress(meals, dailyCalorieGoal);
  }, [meals, dailyCalorieGoal]);
  
  const prevDateString = useRef(currentDate.toDateString());

  useEffect(() => {
      const newDateString = currentDate.toDateString();
      if (prevDateString.current !== newDateString) {
          setProteinModalShownForDate(null);
          prevDateString.current = newDateString;
      }
  }, [currentDate]);

  useEffect(() => {
    const dateKey = currentDate.toDateString();
    const waterToday = waterIntake[dateKey] || 0;
    checkForNewBadges(meals, challengesProgress, waterToday);
  }, [challengesProgress, meals, waterIntake, currentDate, checkForNewBadges]);

  // Effect to trigger the protein suggestion modal
  useEffect(() => {
      const todayStr = currentDate.toDateString();
      // Check if conditions are met AND the modal hasn't been shown for the current date
      if (
          activePage === 'home' &&
          displayedMeals.length > 0 &&
          dailyTotals.protein > 0 &&
          dailyTotals.protein < (DAILY_PROTEIN_GOAL * 0.8) && // less than 80% of goal
          proteinModalShownForDate !== todayStr
      ) {
          // Show the modal after a short delay to not be too intrusive
          const timer = setTimeout(() => {
              setIsProteinModalOpen(true);
              setProteinModalShownForDate(todayStr); // Mark as shown for today
          }, 2000); // 2-second delay

          return () => clearTimeout(timer);
      }
  }, [activePage, displayedMeals, dailyTotals.protein, currentDate, proteinModalShownForDate]);


  const handleFileChange = useCallback(async (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (files && files.length > 0) {
      const fileList = Array.from(files);
      setAnalysisQueue(fileList);
      setCurrentAnalysisIndex(0);
    }
    if (event.target) event.target.value = '';
  }, []);

  useEffect(() => {
    const processFile = async () => {
      if (analysisQueue.length === 0 || currentAnalysisIndex >= analysisQueue.length) {
        return;
      }
  
      const file = analysisQueue[currentAnalysisIndex];
      setIsModalOpen(true);
      setIsAnalyzing(true);
      setError(null);
      setAnalysisResult(null);
      setSelectedImage(null);
  
      try {
        const base64Image = await fileToBase64(file);
        setSelectedImage(base64Image);
        const result = await analyzeImageWithGemini(file);
        setAnalysisResult(result);
      } catch (err) {
        setError('음식 분석 중 오류가 발생했습니다. 이 사진을 건너뛸 수 있습니다.');
        console.error(err);
      } finally {
        setIsAnalyzing(false);
      }
    };
  
    processFile();
  }, [analysisQueue, currentAnalysisIndex]);

  const processNextInQueue = () => {
    if (currentAnalysisIndex + 1 < analysisQueue.length) {
        setCurrentAnalysisIndex(prev => prev + 1);
    } else {
        // This was the last one, reset everything.
        setIsModalOpen(false);
        setAnalysisQueue([]);
        setCurrentAnalysisIndex(0);
        setAnalysisResult(null);
        setSelectedImage(null);
        setError(null);
    }
  };

  const handleAddToLog = useCallback((results: AnalysisResult[], time: MealTime) => {
    if (!results || results.length === 0) {
        processNextInQueue();
        return;
    }

    const timestamp = new Date();
    const newMealItems: Meal[] = results.map((result, index) => ({
        id: timestamp.getTime() + index,
        time: time,
        date: new Date(),
        image: index === 0 ? selectedImage ?? undefined : undefined,
        ...result,
    }));

    const updatedMeals = [...newMealItems, ...meals];
    setMeals(updatedMeals);

    const mealFeedItems: GroupFeedItem[] = newMealItems.map(newMeal => ({
        id: newMeal.id,
        groupId: 1, // Mock group Id
        type: 'meal',
        userName: '박상호', // Mock user
        content: `${newMeal.time}으로 ${newMeal.foodItem} (${newMeal.kcal}kcal)를 기록했습니다.`,
        timestamp: new Date(),
        likes: 0,
        likedByMe: false,
    }));
    
    let newFeedItems: GroupFeedItem[] = [...mealFeedItems];

    const oldChallenges = challengesProgress;
    const newChallenges = calculateChallengesProgress(updatedMeals, dailyCalorieGoal);

    newChallenges.forEach((newChallenge, index) => {
        const oldChallenge = oldChallenges[index];
        if (oldChallenge?.status !== '완료' && newChallenge.status === '완료') {
            const challengeFeedItem: GroupFeedItem = {
                id: Date.now() + index + 1000,
                groupId: 1, // Mock group Id
                type: 'challenge',
                userName: '박상호', // Mock user
                content: `'${newChallenge.title}' 챌린지를 완료했습니다! 🎉`,
                timestamp: new Date(),
                likes: 0,
                likedByMe: false,
            };
            newFeedItems.push(challengeFeedItem);
        }
    });

    setGroupFeed(prevFeed => [...newFeedItems, ...prevFeed]);

    showToast(`${results.length}개의 항목이 식단에 추가되었습니다!`);
    processNextInQueue();
  }, [meals, selectedImage, challengesProgress, dailyCalorieGoal]);
  
  const handleAddManualMeal = useCallback(async (mealData: { foodItem: string, time: MealTime }) => {
    try {
        const result = await analyzeTextWithGemini(mealData.foodItem);
        const newMeal: Meal = {
            id: Date.now(),
            date: new Date(), // Add meal with today's date, regardless of view
            time: mealData.time,
            ...result,
        };
        
        const updatedMeals = [newMeal, ...meals];
        setMeals(updatedMeals);

        const mealFeedItem: GroupFeedItem = {
            id: newMeal.id,
            groupId: 1,
            type: 'meal',
            userName: '박상호',
            content: `${newMeal.time}으로 ${newMeal.foodItem} (${newMeal.kcal}kcal)를 기록했습니다.`,
            timestamp: new Date(),
            likes: 0,
            likedByMe: false,
        };
        
        const newFeedItems = [mealFeedItem];
        const oldChallenges = challengesProgress;
        const newChallenges = calculateChallengesProgress(updatedMeals, dailyCalorieGoal);
        
        newChallenges.forEach((newChallenge, index) => {
            const oldChallenge = oldChallenges[index];
            if (oldChallenge?.status !== '완료' && newChallenge.status === '완료') {
                const challengeFeedItem: GroupFeedItem = {
                    id: Date.now() + index + 1001,
                    groupId: 1,
                    type: 'challenge',
                    userName: '박상호',
                    content: `'${newChallenge.title}' 챌린지를 완료했습니다! 🎉`,
                    timestamp: new Date(),
                    likes: 0,
                    likedByMe: false,
                };
                newFeedItems.push(challengeFeedItem);
            }
        });

        setGroupFeed(prevFeed => [...newFeedItems, ...prevFeed]);

        setIsManualAddModalOpen(false);
        showToast('수동으로 추가되었습니다!');
    } catch (error) {
        console.error(error);
        // Propagate error to be displayed in the modal
        throw error;
    }
  }, [meals, challengesProgress, dailyCalorieGoal]);

  const handleDeleteMeal = useCallback((mealId: number) => {
    setMeals(prevMeals => prevMeals.filter(meal => meal.id !== mealId));
    showToast('기록이 삭제되었습니다.');
  }, []);

  const handleUpdateWaterIntake = useCallback((amount: number) => {
    const dateKey = currentDate.toDateString();
    setWaterIntake(prev => {
      const currentAmount = prev[dateKey] || 0;
      const newAmount = Math.max(0, currentAmount + amount);
      return { ...prev, [dateKey]: newAmount };
    });
  }, [currentDate]);
  
  const handleCreateGroup = useCallback((groupData: { name: string; description: string }, challenge: string, invitedFriends: number[]) => {
    const newGroup: Group = {
      id: Date.now(),
      members: 1 + invitedFriends.length,
      challenge: challenge,
      progress: 0,
      ...groupData
    };
    setGroups(prev => [newGroup, ...prev]);
    setIsGroupCreateModalOpen(false);
    showToast('그룹이 생성되었습니다!');
  }, []);

  const handleDeleteGroup = useCallback((groupId: number) => {
    setGroups(prev => prev.filter(g => g.id !== groupId));
    showToast('그룹에서 탈퇴했습니다.');
  }, []);
  
    const handleAddFriend = useCallback((friendName: string) => {
    const newFriend: Friend = {
      id: Date.now(),
      name: friendName,
      avatar: `https://i.pravatar.cc/150?u=${Date.now()}`
    };
    setFriends(prev => [newFriend, ...prev]);
    setIsAddFriendModalOpen(false);
    showToast(`${friendName}님과 친구가 되었습니다!`);
  }, []);

  const handleSelectGroup = (groupId: number) => {
    setActiveGroupId(groupId);
  };
  
  const handleLeaveGroupDetail = () => {
    setActiveGroupId(null);
  };
  
  const handleToggleFeedLike = (feedItemId: number) => {
    setGroupFeed(prevFeed => 
      prevFeed.map(item => 
        item.id === feedItemId 
          ? { ...item, likedByMe: !item.likedByMe, likes: item.likedByMe ? item.likes - 1 : item.likes + 1 }
          : item
      )
    );
  };


  const handleUpdateProfile = useCallback((newProfile: UserProfile) => {
    setUserProfile(newProfile);
    setIsProfileEditModalOpen(false);
    showToast('프로필이 업데이트되었습니다!');
  }, []);

  const handleCloseModal = () => {
    // This now cancels the entire queue
    setIsModalOpen(false);
    setAnalysisQueue([]);
    setCurrentAnalysisIndex(0);
    setAnalysisResult(null);
    setSelectedImage(null);
    setError(null);
  };

  const handleSkip = () => {
    processNextInQueue();
  };

  const handleDeleteAllData = useCallback(() => {
    setMeals([]);
    setWaterIntake({});
    setGroupFeed([]);
    // Optionally reset other states like badges
    setUserProfile(prev => ({...prev, unlockedBadgeIds: []}));
    setIsSettingsOpen(false);
    showToast('모든 데이터가 삭제되었습니다.');
  }, []);
  
  const handleOpenReport = () => setIsReportOpen(true);
  const handleCloseReport = () => setIsReportOpen(false);

  const handleOpenSettings = () => setIsSettingsOpen(true);
  const handleCloseSettings = () => setIsSettingsOpen(false);

  const handleTakePhoto = () => {
    setIsCameraModalOpen(true);
  };

  const handleCapture = (file: File) => {
      setIsCameraModalOpen(false);
      if (file) {
          setAnalysisQueue([file]);
          setCurrentAnalysisIndex(0);
      }
  };

  useEffect(() => {
    const generateSuggestion = async () => {
      setSuggestion(prev => ({ ...prev, isLoading: true }));
      const surplusKcal = dailyTotals.kcal - dailyCalorieGoal;
  
      if (surplusKcal > 100) {
        try {
          const exerciseDesc = await getExerciseSuggestion(surplusKcal, userProfile.weight);
          // FIX: The icon type is now correctly handled by the `SuggestionState` type.
          setSuggestion({
            icon: 'Dumbbell',
            title: 'AI 운동 추천',
            description: exerciseDesc,
            ctaText: '운동 기록하기',
            isLoading: false,
          });
        } catch (e) {
          console.error(e);
          // AI 호출 실패 시 fallback
          // FIX: The icon type is now correctly handled by the `SuggestionState` type.
          setSuggestion({
            icon: 'Dumbbell',
            title: '오늘의 운동 추천',
            description: `섭취 칼로리가 목표를 ${Math.round(surplusKcal)}kcal 초과했어요. ${Math.round(surplusKcal / 8.2)}분 조깅으로 소모해보세요!`,
            ctaText: '운동 기록하기',
            isLoading: false,
          });
        }
      } else if (displayedMeals.length > 0 && dailyTotals.protein < DAILY_PROTEIN_GOAL) {
        // FIX: The icon type is now correctly handled by the `SuggestionState` type.
        setSuggestion({
          icon: 'Target',
          title: '단백질 보충 제안',
          description: `단백질 섭취가 부족해요. 닭가슴살, 두부, 계란 등으로 보충해 건강한 근육을 만드세요.`,
          ctaText: '식단팁 보기',
          isLoading: false,
        });
      } else {
        setSuggestion({
          icon: 'Zap',
          title: '잘하고 있어요!',
          description: '균형 잡힌 식단을 유지하고 있습니다. 이대로 꾸준히 관리해 보세요.',
          ctaText: '건강 리포트 보기',
          isLoading: false,
        });
      }
    };
  
    generateSuggestion();
  }, [dailyTotals, dailyCalorieGoal, displayedMeals.length, userProfile.weight]);
  
  const renderPage = () => {
    const waterToday = waterIntake[currentDate.toDateString()] || 0;
    switch(activePage) {
      case 'home':
        return <HomePage 
                  meals={displayedMeals}
                  dailyTotals={dailyTotals}
                  suggestion={suggestion}
                  onDeleteMeal={handleDeleteMeal}
                  onOpenManualAddModal={() => handleOpenManualAddModal()}
                  dailyCalorieGoal={dailyCalorieGoal}
                  onViewReport={handleOpenReport}
                  waterIntake={waterToday}
                  onUpdateWater={handleUpdateWaterIntake}
                />;
      case 'analysis':
        return <AnalysisPage meals={meals} dailyTotals={dailyTotals} dailyCalorieGoal={dailyCalorieGoal} challenges={challengesProgress} userProfile={userProfile} onLogMeal={handleOpenManualAddModal} />;
      case 'chat':
        return <ChatPage meals={meals} />;
      case 'groups':
        if (activeGroupId) {
          const selectedGroup = groups.find(g => g.id === activeGroupId);
          if (selectedGroup) {
            return <GroupDetailPage 
                      group={selectedGroup} 
                      feed={groupFeed.filter(f => f.groupId === activeGroupId)} 
                      onLeave={handleLeaveGroupDetail} 
                      onToggleLike={handleToggleFeedLike}
                   />;
          }
        }
        return <GroupsPage 
                  groups={groups} 
                  onOpenCreateModal={() => setIsGroupCreateModalOpen(true)}
                  onDeleteGroup={handleDeleteGroup}
                  onSelectGroup={handleSelectGroup}
                />;
      case 'profile':
        return <ProfilePage 
                  userProfile={userProfile} 
                  friends={friends}
                  onEdit={() => setIsProfileEditModalOpen(true)} 
                  onOpenAddFriendModal={() => setIsAddFriendModalOpen(true)}
                  onOpenSettings={handleOpenSettings}
                />;
      default:
        return <HomePage 
                  meals={displayedMeals}
                  dailyTotals={dailyTotals}
                  suggestion={suggestion}
                  onDeleteMeal={handleDeleteMeal}
                  onOpenManualAddModal={() => handleOpenManualAddModal()}
                  dailyCalorieGoal={dailyCalorieGoal}
                  onViewReport={handleOpenReport}
                  waterIntake={waterToday}
                  onUpdateWater={handleUpdateWaterIntake}
                />;
    }
  }

  return (
    <div className="min-h-screen bg-gray-90 dark:bg-dark-bg text-gray-text dark:text-dark-text antialiased transition-colors duration-300">
      {(activePage === 'home' || activePage === 'chat') && (
        <Header 
          currentDate={currentDate} 
          onDateChange={handleDateChange}
          onGoToToday={handleGoToToday}
          onOpenCalendar={() => setIsCalendarModalOpen(true)}
          theme={theme}
          onSetTheme={setTheme}
        />
      )}
      <main className="p-4 md:p-6 pb-28 max-w-3xl mx-auto">
        {renderPage()}
      </main>
      
      {activePage === 'home' && <Fab onFileChange={handleFileChange} onTakePhoto={handleTakePhoto} />}
      
      <BottomNav activePage={activePage} onNavigate={setActivePage} />

      <AnalysisModal 
        isOpen={isModalOpen}
        isAnalyzing={isAnalyzing}
        result={analysisResult}
        image={selectedImage}
        error={error}
        onClose={handleCloseModal}
        onConfirm={handleAddToLog}
        onSkip={handleSkip}
        queuePosition={analysisQueue.length > 1 ? `${currentAnalysisIndex + 1}/${analysisQueue.length}` : null}
      />
      <ManualAddModal
        isOpen={isManualAddModalOpen}
        onClose={() => {
          setIsManualAddModalOpen(false);
          setManualAddPrefill(null);
        }}
        onAddMeal={handleAddManualMeal}
        prefillValue={manualAddPrefill}
      />
      <ProfileEditModal
        isOpen={isProfileEditModalOpen}
        onClose={() => setIsProfileEditModalOpen(false)}
        currentProfile={userProfile}
        onSave={handleUpdateProfile}
      />
      <GroupCreateModal 
        isOpen={isGroupCreateModalOpen}
        onClose={() => setIsGroupCreateModalOpen(false)}
        onCreate={handleCreateGroup}
        friends={friends}
      />
      <AddFriendModal
        isOpen={isAddFriendModalOpen}
        onClose={() => setIsAddFriendModalOpen(false)}
        onAddFriend={handleAddFriend}
      />
       <CameraModal
        isOpen={isCameraModalOpen}
        onClose={() => setIsCameraModalOpen(false)}
        onCapture={handleCapture}
      />
      <CalendarModal
        isOpen={isCalendarModalOpen}
        onClose={() => setIsCalendarModalOpen(false)}
        onSelectDate={handleDateSelect}
        selectedDate={currentDate}
      />
       <ProteinSuggestionModal
          isOpen={isProteinModalOpen}
          onClose={() => setIsProteinModalOpen(false)}
          meals={displayedMeals}
        />
       {isReportOpen && (
        <ReportPage 
          meals={meals} 
          challenges={challengesProgress}
          onClose={handleCloseReport} 
        />
      )}
       {isSettingsOpen && (
        <SettingsPage
          onClose={handleCloseSettings}
          theme={theme}
          onThemeChange={setTheme}
          onDeleteAllData={handleDeleteAllData}
        />
      )}
      <Toast 
        show={toast.show}
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ ...toast, show: false })}
      />
    </div>
  );
};

export default App;