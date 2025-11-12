import React, { useMemo } from 'react';
import type { Meal } from '../../types';

interface MealTimeHeatmapProps {
  meals: Meal[];
}

const getColor = (value: number, max: number) => {
  if (value === 0) return 'bg-gray-70 dark:bg-dark-border';
  const intensity = Math.min(value / (max * 0.8), 1); // 80%를 최대 강도로 설정
  if (intensity < 0.25) return 'bg-primary-blue/20';
  if (intensity < 0.5) return 'bg-primary-blue/40';
  if (intensity < 0.75) return 'bg-primary-blue/60';
  return 'bg-primary-blue/80';
};

const MealTimeHeatmap: React.FC<MealTimeHeatmapProps> = ({ meals }) => {
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  const times: ('아침' | '점심' | '저녁')[] = ['아침', '점심', '저녁'];

  const { heatmapData, maxKcal } = useMemo(() => {
    const data: Record<string, Record<string, { totalKcal: number; count: number }>> = {};
    let maxKcal = 0;
    
    days.forEach(day => {
      data[day] = {};
      times.forEach(time => {
        data[day][time] = { totalKcal: 0, count: 0 };
      });
    });

    meals.forEach(meal => {
      const dayOfWeek = days[new Date(meal.date).getDay()];
      if (data[dayOfWeek] && data[dayOfWeek][meal.time]) {
        data[dayOfWeek][meal.time].totalKcal += meal.kcal;
        data[dayOfWeek][meal.time].count += 1;
      }
    });
    
    // Calculate average and find max for color scaling
    Object.values(data).forEach(dayData => {
        Object.values(dayData).forEach(timeData => {
            const avg = timeData.count > 0 ? timeData.totalKcal / timeData.count : 0;
            if (avg > maxKcal) {
                maxKcal = avg;
            }
        });
    });

    return { heatmapData: data, maxKcal };
  }, [meals]);


  return (
    <div className="bg-gray-100 dark:bg-dark-card p-6 rounded-2xl shadow-card">
      <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text mb-4">식사 시간 패턴</h2>
      <div className="grid grid-cols-8 gap-1.5 text-center text-xs">
        {/* Header (Time) */}
        <div />
        {days.map(day => <div key={day} className="font-semibold text-gray-subtext dark:text-dark-subtext">{day}</div>)}
        
        {/* Rows (Days) */}
        {times.map(time => (
          <React.Fragment key={time}>
            <div className="font-semibold text-gray-subtext dark:text-dark-subtext flex items-center justify-center">{time}</div>
            {days.map(day => {
              const cellData = heatmapData[day]?.[time];
              const avgKcal = cellData && cellData.count > 0 ? cellData.totalKcal / cellData.count : 0;
              
              return (
                <div key={`${day}-${time}`} className="relative group">
                   <div 
                      className={`w-full aspect-square rounded ${getColor(avgKcal, maxKcal)} transition-colors`}
                    />
                    <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-max px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10">
                      {Math.round(avgKcal)} kcal
                    </div>
                </div>
              );
            })}
          </React.Fragment>
        ))}
      </div>
       <div className="flex justify-end items-center mt-4 space-x-2">
        <span className="text-xs text-gray-500">적음</span>
        <div className="flex space-x-1">
            <div className="w-3 h-3 rounded-sm bg-primary-blue/20"></div>
            <div className="w-3 h-3 rounded-sm bg-primary-blue/40"></div>
            <div className="w-3 h-3 rounded-sm bg-primary-blue/60"></div>
            <div className="w-3 h-3 rounded-sm bg-primary-blue/80"></div>
        </div>
        <span className="text-xs text-gray-500">많음</span>
       </div>
    </div>
  );
};

export default MealTimeHeatmap;