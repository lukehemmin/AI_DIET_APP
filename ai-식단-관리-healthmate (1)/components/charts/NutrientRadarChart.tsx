import React from 'react';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Legend, Tooltip } from 'recharts';

interface NutrientRadarChartProps {
  dailyTotals: {
    carbs: number;
    protein: number;
    fat: number;
  };
}

const NutrientRadarChart: React.FC<NutrientRadarChartProps> = ({ dailyTotals }) => {
  const { carbs, protein, fat } = dailyTotals;
  const totalMacros = carbs + protein + fat;

  const userPercentage = {
    carbs: totalMacros > 0 ? (carbs / totalMacros) * 100 : 0,
    protein: totalMacros > 0 ? (protein / totalMacros) * 100 : 0,
    fat: totalMacros > 0 ? (fat / totalMacros) * 100 : 0,
  };

  const data = [
    { subject: '탄수화물', A: userPercentage.carbs, B: 50, fullMark: 100 },
    { subject: '단백질', A: userPercentage.protein, B: 30, fullMark: 100 },
    { subject: '지방', A: userPercentage.fat, B: 20, fullMark: 100 },
  ];

  return (
    <div className="bg-gray-100 dark:bg-dark-card p-6 rounded-2xl shadow-card">
      <h2 className="text-xl font-semibold text-gray-text dark:text-dark-text mb-4">주간 영양 균형</h2>
      <div className="h-72 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <RadarChart cx="50%" cy="50%" outerRadius="80%" data={data}>
            <PolarGrid stroke="var(--color-gray-70)" className="dark:stroke-dark-border" />
            <PolarAngleAxis dataKey="subject" tick={{ fill: 'var(--color-gray-subtext)' }} className="dark:fill-dark-subtext" />
            <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
            <Radar name="나의 섭취 비율" dataKey="A" stroke="#0A84FF" fill="#0A84FF" fillOpacity={0.5} />
            <Radar name="권장 섭취 비율" dataKey="B" stroke="#34C759" fill="none" strokeWidth={2.5} />
            <Legend wrapperStyle={{ bottom: -10 }} />
            <Tooltip 
                formatter={(value) => [`${(value as number).toFixed(1)}%`]}
                contentStyle={{
                    backgroundColor: 'rgba(255, 255, 255, 0.8)',
                    backdropFilter: 'blur(5px)',
                    border: '1px solid #E9EDF0',
                    borderRadius: '12px',
                }}
            />
          </RadarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default NutrientRadarChart;