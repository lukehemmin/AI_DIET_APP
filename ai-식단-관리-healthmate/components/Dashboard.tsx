import React, { useMemo } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import type { Nutrients } from '../types';

interface DashboardProps {
  dailyTotals: {
    kcal: number;
  } & Nutrients;
  dailyGoal: number;
}

const COLORS = {
  carbs: '#0A84FF',   // Primary Blue
  protein: '#34C759', // Success Green
  fat: '#FF9500',    // Orange
};

const Dashboard: React.FC<DashboardProps> = ({ dailyTotals, dailyGoal }) => {
  const { kcal, carbs, protein, fat } = dailyTotals;

  const macroData = useMemo(() => {
    const totalMacros = carbs + protein + fat;
    if (totalMacros === 0) {
      return [
        { name: '탄수화물', value: 1, color: COLORS.carbs },
        { name: '단백질', value: 1, color: COLORS.protein },
        { name: '지방', value: 1, color: COLORS.fat },
      ];
    }
    return [
      { name: '탄수화물', value: carbs, color: COLORS.carbs },
      { name: '단백질', value: protein, color: COLORS.protein },
      { name: '지방', value: fat, color: COLORS.fat },
    ];
  }, [carbs, protein, fat]);
  
  const totalMacros = carbs + protein + fat;
  const progress = dailyGoal > 0 ? (kcal / dailyGoal) * 100 : 0;
  const isOverGoal = progress > 100;

  return (
    <div className="bg-gray-100 dark:bg-dark-card p-6 rounded-2xl shadow-card animate-fade-in">
      <h2 className="text-lg font-semibold text-gray-subtext dark:text-dark-subtext mb-1">오늘 총 섭취 칼로리</h2>
      <div className="flex justify-between items-baseline">
        <p className={`text-[32px] leading-[40px] font-semibold ${isOverGoal ? 'text-orange-500' : 'text-gray-text dark:text-dark-text'} mb-2 transition-colors`}>{Math.round(kcal).toLocaleString()} </p>
        <p className="text-gray-subtext dark:text-dark-subtext">/ {dailyGoal.toLocaleString()} <span className="text-sm">kcal</span></p>
      </div>
      
      <div className="w-full bg-gray-70 dark:bg-dark-border rounded-full h-2 mb-6">
        <div 
            className={`h-2 rounded-full transition-all duration-500 ${isOverGoal ? 'bg-orange-500' : 'bg-primary-blue'}`} 
            style={{ width: `${Math.min(progress, 100)}%` }}
        ></div>
      </div>

      <div className="h-48 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={macroData}
              cx="50%"
              cy="50%"
              labelLine={false}
              innerRadius={50}
              outerRadius={70}
              fill="#8884d8"
              paddingAngle={5}
              dataKey="value"
              stroke="none"
            >
              {macroData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip
              formatter={(value: number, name) => [`${value}g`, name]}
              contentStyle={{
                backgroundColor: 'rgba(255, 255, 255, 0.8)',
                backdropFilter: 'blur(5px)',
                border: '1px solid #E9EDF0',
                borderRadius: '12px',
                padding: '8px 12px',
              }}
            />
            <Legend
              iconType="circle"
              iconSize={10}
              verticalAlign="middle"
              align="right"
              layout="vertical"
              formatter={(value, entry) => {
                const percentage = totalMacros > 0 ? (entry.payload.value / totalMacros * 100).toFixed(0) : 0;
                return <span className="text-gray-subtext dark:text-dark-subtext ml-2">{`${value} (${percentage}%)`}</span>
              }}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default Dashboard;
