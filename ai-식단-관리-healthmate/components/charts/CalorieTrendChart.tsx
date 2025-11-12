import React, { useMemo } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend, ReferenceLine } from 'recharts';
import type { Meal } from '../../types';

interface CalorieTrendChartProps {
  meals: Meal[];
  dailyGoal: number;
}

const CalorieTrendChart: React.FC<CalorieTrendChartProps> = ({ meals, dailyGoal }) => {
  const chartData = useMemo(() => {
    const dailyKcal: { [date: string]: number } = {};
    
    meals.forEach(meal => {
      const dateStr = new Date(meal.date).toLocaleDateString('ko-KR', { month: '2-digit', day: '2-digit' }).replace(/\.$/, '');
      if (!dailyKcal[dateStr]) {
        dailyKcal[dateStr] = 0;
      }
      dailyKcal[dateStr] += meal.kcal;
    });

    return Object.entries(dailyKcal)
      .map(([date, kcal]) => ({ date, kcal }))
      .sort((a, b) => {
          const dateA = new Date(new Date().getFullYear(), parseInt(a.date.split('.')[0]) - 1, parseInt(a.date.split('.')[1]));
          const dateB = new Date(new Date().getFullYear(), parseInt(b.date.split('.')[0]) - 1, parseInt(b.date.split('.')[1]));
          return dateA.getTime() - dateB.getTime();
      });
  }, [meals]);

  return (
    <div className="bg-gray-100 dark:bg-dark-card p-6 rounded-2xl shadow-card">
      <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text mb-4">칼로리 섭취 트렌드</h2>
      <div className="h-72 w-full">
        {chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <LineChart
              data={chartData}
              margin={{
                top: 5,
                right: 20,
                left: -10,
                bottom: 5,
              }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="var(--color-gray-70)" className="dark:stroke-dark-border" />
              <XAxis dataKey="date" tick={{ fill: 'var(--color-gray-subtext)' }} className="dark:fill-dark-subtext text-xs" />
              <YAxis tick={{ fill: 'var(--color-gray-subtext)' }} className="dark:fill-dark-subtext text-xs" />
              <Tooltip 
                contentStyle={{
                    backgroundColor: 'rgba(255, 255, 255, 0.8)',
                    backdropFilter: 'blur(5px)',
                    border: '1px solid #E9EDF0',
                    borderRadius: '12px',
                }}
                formatter={(value) => [`${Math.round(value as number)} kcal`, '섭취량']}
              />
              <Legend wrapperStyle={{ bottom: -10 }}/>
              <ReferenceLine y={dailyGoal} label={{ value: '목표', position: 'insideTopLeft', fill: '#FF9500' }} stroke="#FF9500" strokeDasharray="4 4" />
              <Line type="monotone" dataKey="kcal" name="섭취 칼로리" stroke="#0A84FF" strokeWidth={2} activeDot={{ r: 8 }} />
            </LineChart>
          </ResponsiveContainer>
        ) : (
            <div className="flex items-center justify-center h-full">
                <p className="text-gray-subtext dark:text-dark-subtext">선택된 기간에 데이터가 없습니다.</p>
            </div>
        )}
      </div>
    </div>
  );
};

export default CalorieTrendChart;