import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface WeeklyReportProps {
  todayKcal: number;
}

const WeeklyReport: React.FC<WeeklyReportProps> = ({ todayKcal }) => {
  // 실제 앱에서는 API로부터 받아올 데이터의 목업입니다.
  const mockData = [
      { name: '3일 전', kcal: 2150 },
      { name: '2일 전', kcal: 1800 },
      { name: '어제', kcal: 1950 },
      { name: '오늘', kcal: Math.round(todayKcal) },
  ];
  
  const avgKcal = mockData.length > 0 ? Math.round(mockData.reduce((sum, day) => sum + day.kcal, 0) / mockData.length) : 0;

  return (
    <div className="bg-gray-100 dark:bg-dark-card p-6 rounded-2xl shadow-card animate-slide-up">
      <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text mb-1">주간 칼로리 분석</h2>
      <p className="text-sm text-gray-subtext dark:text-dark-subtext mb-4">최근 4일간의 섭취 칼로리 변화입니다.</p>
      
      <div className="h-60 w-full mb-4">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={mockData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--color-gray-70)" className="dark:stroke-dark-border" />
            <XAxis dataKey="name" tick={{ fill: 'var(--color-gray-subtext)' }} className="text-xs dark:fill-dark-subtext" />
            <YAxis tick={{ fill: 'var(--color-gray-subtext)' }} className="text-xs dark:fill-dark-subtext" />
            <Tooltip
              cursor={{ fill: 'rgba(10, 132, 255, 0.1)' }}
              contentStyle={{
                backgroundColor: 'rgba(255, 255, 255, 0.8)',
                backdropFilter: 'blur(5px)',
                border: '1px solid #E9EDF0',
                borderRadius: '12px',
                padding: '8px 12px',
              }}
              labelStyle={{ color: '#334155', fontWeight: 'bold' }}
              formatter={(value) => [`${value} kcal`, '섭취량']}
            />
            <Bar dataKey="kcal" fill="#0A84FF" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-gray-90 dark:bg-dark-bg p-4 rounded-lg">
        <h3 className="font-semibold text-gray-text dark:text-dark-text mb-1">Insight</h3>
        <p className="text-sm text-gray-subtext dark:text-dark-subtext">
          최근 4일 평균 섭취량은 <span className="font-bold text-primary-blue">{avgKcal.toLocaleString()}kcal</span> 입니다. 꾸준히 목표에 가깝게 잘 관리하고 계십니다!
        </p>
      </div>
    </div>
  );
};

export default WeeklyReport;