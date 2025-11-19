
import React, { useState, useEffect, useRef, FormEvent } from 'react';
import type { Meal, ChatMessage } from '../types';
import { createChatSession } from '../services/geminiService';
import type { Chat } from '@google/genai';
import { Send, Bot } from 'lucide-react';

interface ChatPageProps {
  meals: Meal[];
}

const ChatPage: React.FC<ChatPageProps> = ({ meals }) => {
  const [chat, setChat] = useState<Chat | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const chatSession = createChatSession(meals);
    setChat(chatSession);
    setMessages([{ 
        role: 'model', 
        text: '안녕하세요! AI 영양사 HealthMate입니다. 식단, 운동 등 건강에 대해 무엇이든 물어보세요.' 
    }]);
  }, [meals]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(scrollToBottom, [messages]);

  const handleSendMessage = async (e: FormEvent) => {
    e.preventDefault();
    const text = inputValue.trim();
    if (!text || isLoading || !chat) return;

    const newUserMessage: ChatMessage = { role: 'user', text };
    setMessages(prev => [...prev, newUserMessage]);
    setInputValue('');
    setIsLoading(true);

    try {
      const response = await chat.sendMessage({ message: text });
      const modelResponse: ChatMessage = { role: 'model', text: response.text };
      setMessages(prev => [...prev, modelResponse]);
    } catch (error) {
      console.error("Chat send message failed:", error);
      const errorMessage: ChatMessage = { role: 'model', text: '죄송합니다, 답변을 생성하는 중 오류가 발생했어요. 잠시 후 다시 시도해주세요.' };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-140px)] animate-fade-in">
        <div className="flex justify-between items-start mb-4">
            <div>
            <h1 className="text-2xl font-bold text-gray-text dark:text-dark-text">AI 영양사</h1>
            <p className="text-gray-subtext dark:text-dark-subtext">식단 기록을 바탕으로 답변해드려요.</p>
            </div>
        </div>
      <div className="flex-1 overflow-y-auto space-y-4 pr-2 -mr-2">
        {messages.map((msg, index) => (
          <div key={index} className={`flex items-start gap-3 ${msg.role === 'user' ? 'justify-end' : ''}`}>
            {msg.role === 'model' && (
              <div className="w-8 h-8 rounded-full bg-primary-blue/20 flex items-center justify-center flex-shrink-0 mt-1">
                <Bot size={20} className="text-primary-blue" />
              </div>
            )}
            <div className={`max-w-[80%] p-3 rounded-2xl ${msg.role === 'user' ? 'bg-primary-blue text-white rounded-br-none' : 'bg-gray-100 dark:bg-dark-card text-gray-text dark:text-dark-text rounded-bl-none'}`}>
              <p className="text-sm leading-relaxed whitespace-pre-wrap">{msg.text}</p>
            </div>
          </div>
        ))}
        {isLoading && (
            <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-primary-blue/20 flex items-center justify-center flex-shrink-0 mt-1">
                    <Bot size={20} className="text-primary-blue" />
                </div>
                <div className="max-w-[80%] p-3 rounded-2xl bg-gray-100 dark:bg-dark-card text-gray-text dark:text-dark-text rounded-bl-none">
                    <div className="flex items-center space-x-1">
                        <span className="w-2 h-2 bg-gray-500 rounded-full animate-pulse" style={{ animationDelay: '0s' }}></span>
                        <span className="w-2 h-2 bg-gray-500 rounded-full animate-pulse" style={{ animationDelay: '0.2s' }}></span>
                        <span className="w-2 h-2 bg-gray-500 rounded-full animate-pulse" style={{ animationDelay: '0.4s' }}></span>
                    </div>
                </div>
            </div>
        )}
        <div ref={messagesEndRef} />
      </div>
      <form onSubmit={handleSendMessage} className="mt-4 flex items-center space-x-2">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="AI 영양사에게 메시지 보내기..."
          className="flex-1 w-full bg-gray-100 dark:bg-dark-card border-none rounded-full p-3 px-5 focus:ring-2 focus:ring-primary-blue transition"
          disabled={isLoading}
        />
        <button
          type="submit"
          disabled={isLoading || !inputValue.trim()}
          className="bg-primary-blue text-white rounded-full w-12 h-12 flex items-center justify-center flex-shrink-0 transition-opacity hover:opacity-80 disabled:opacity-50 disabled:cursor-not-allowed"
          aria-label="메시지 전송"
        >
          <Send size={20} />
        </button>
      </form>
    </div>
  );
};

export default ChatPage;
