import React, { useState, useMemo } from 'react';
import { X, ChevronLeft, ChevronRight } from 'lucide-react';

interface CalendarModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectDate: (date: Date) => void;
  selectedDate: Date;
}

const CalendarModal: React.FC<CalendarModalProps> = ({ isOpen, onClose, onSelectDate, selectedDate }) => {
  const [viewDate, setViewDate] = useState(new Date(selectedDate));
  
  const today = useMemo(() => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return d;
  }, []);

  const calendarGrid = useMemo(() => {
    const year = viewDate.getFullYear();
    const month = viewDate.getMonth();

    const firstDayOfMonth = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const grid: (Date | null)[] = [];
    
    // Fill with empty cells for days before the 1st
    for (let i = 0; i < firstDayOfMonth; i++) {
      grid.push(null);
    }
    
    // Fill with actual days
    for (let day = 1; day <= daysInMonth; day++) {
      grid.push(new Date(year, month, day));
    }
    
    return grid;
  }, [viewDate]);

  const handleMonthChange = (direction: 'prev' | 'next') => {
    setViewDate(prev => {
      const newDate = new Date(prev);
      newDate.setDate(1); // Avoid issues with end-of-month dates
      newDate.setMonth(newDate.getMonth() + (direction === 'prev' ? -1 : 1));
      return newDate;
    });
  };
  
  const handleDateClick = (date: Date) => {
    if (date > today) return; // Don't select future dates
    onSelectDate(date);
  };
  
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50 p-4 animate-fade-in" onClick={onClose}>
      <div className="bg-gray-100 dark:bg-dark-card rounded-2xl shadow-lg w-full max-w-sm p-6 animate-slide-up" onClick={(e) => e.stopPropagation()}>
        <div className="flex justify-between items-center mb-4">
          <div className="flex items-center space-x-2">
            <button onClick={() => handleMonthChange('prev')} className="p-1 rounded-full hover:bg-gray-200 dark:hover:bg-slate-700 transition-colors">
              <ChevronLeft size={20} />
            </button>
            <h2 className="text-lg font-semibold text-gray-text dark:text-dark-text w-32 text-center">
              {viewDate.getFullYear()}년 {viewDate.getMonth() + 1}월
            </h2>
            <button onClick={() => handleMonthChange('next')} className="p-1 rounded-full hover:bg-gray-200 dark:hover:bg-slate-700 transition-colors">
              <ChevronRight size={20} />
            </button>
          </div>
          <button onClick={onClose} className="text-gray-subtext dark:text-dark-subtext hover:text-gray-text dark:hover:text-dark-text">
            <X size={24} />
          </button>
        </div>
        
        <div className="grid grid-cols-7 gap-y-2 text-center text-sm">
          {['일', '월', '화', '수', '목', '금', '토'].map(day => (
            <div key={day} className="font-semibold text-gray-subtext dark:text-dark-subtext">{day}</div>
          ))}
          {calendarGrid.map((date, index) => {
            if (!date) return <div key={`empty-${index}`} />;
            
            const isSelected = date.toDateString() === selectedDate.toDateString();
            const isToday = date.toDateString() === today.toDateString();
            const isFuture = date > today;
            
            const buttonClasses = [
              'w-9 h-9 flex items-center justify-center rounded-full transition-colors',
              isFuture ? 'text-gray-50 dark:text-slate-600 cursor-not-allowed' : 'hover:bg-gray-200 dark:hover:bg-slate-700',
              isSelected ? 'bg-primary-blue text-white font-bold' : '',
              !isSelected && isToday ? 'ring-1 ring-primary-blue text-primary-blue' : '',
              !isSelected && !isToday && !isFuture ? 'text-gray-text dark:text-dark-text' : ''
            ].filter(Boolean).join(' ');

            return (
              <div key={date.toString()} className="flex justify-center">
                <button 
                  onClick={() => handleDateClick(date)}
                  disabled={isFuture}
                  className={buttonClasses}
                >
                  {date.getDate()}
                </button>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default CalendarModal;