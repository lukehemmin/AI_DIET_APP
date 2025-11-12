

import { GoogleGenAI, Type, Chat } from "@google/genai";
import type { AnalysisResult, Meal, AIChallenge, Recipe, UserProfile, ProteinFoodSuggestion, MealPlan } from '../types';
import { fileToBase64 } from '../utils/fileUtils';
import { ACTIVITY_LEVELS } from "../constants";

if (!process.env.API_KEY) {
    throw new Error("API_KEY is not defined in environment variables");
}
const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });

const model = 'gemini-2.5-flash';

const analysisItemSchema = {
    type: Type.OBJECT,
    properties: {
        foodItem: {
            type: Type.STRING,
            description: "음식의 이름 (예: 김치찌개)",
        },
        servingSize: {
            type: Type.NUMBER,
            description: "1인분 기준 양(g)",
        },
        kcal: {
            type: Type.NUMBER,
            description: "총 칼로리 (kcal)",
        },
        macro: {
            type: Type.OBJECT,
            properties: {
                carbs: {
                    type: Type.NUMBER,
                    description: "탄수화물 함량 (g)",
                },
                protein: {
                    type: Type.NUMBER,
                    description: "단백질 함량 (g)",
                },
                fat: {
                    type: Type.NUMBER,
                    description: "지방 함량 (g)",
                },
            },
            required: ["carbs", "protein", "fat"],
        },
    },
    required: ["foodItem", "servingSize", "kcal", "macro"],
};

const multiFoodResponseSchema = {
    type: Type.ARRAY,
    items: analysisItemSchema,
};

export const analyzeImageWithGemini = async (file: File): Promise<AnalysisResult[]> => {
    const base64Data = await fileToBase64(file);

    const imagePart = {
        inlineData: {
            mimeType: file.type,
            data: base64Data,
        },
    };

    const textPart = {
        text: "이 음식 사진을 분석해서 사진에 보이는 모든 음식 각각의 칼로리와 영양 정보를 알려줘. 음식 이름, 1인분 기준 양(g), 칼로리(kcal), 그리고 탄수화물, 단백질, 지방 함량(g)을 포함해서 아래 JSON 스키마에 맞춰서 배열(array) 형태로 정확하게 응답해줘. 만약 음식이 없거나 분석이 불가능하면, 빈 배열 `[]`을 반환해줘.",
    };

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: { parts: [textPart, imagePart] },
            config: {
                systemInstruction: "당신은 전문 영양사입니다. 사용자가 업로드한 음식 사진을 보고, 사진에 있는 모든 음식의 정확한 영양 정보를 분석하고 제공하는 역할을 합니다. 모든 답변은 제시된 JSON 스키마를 엄격히 따라야 합니다.",
                responseMimeType: "application/json",
                responseSchema: multiFoodResponseSchema,
            },
        });

        const jsonString = response.text.trim();
        const parsedResult = JSON.parse(jsonString) as AnalysisResult[];

        if (!Array.isArray(parsedResult) || parsedResult.length === 0) {
            throw new Error('인식할 수 없는 음식입니다.');
        }

        return parsedResult;

    } catch (error) {
        console.error("Gemini API call failed:", error);
        throw new Error("AI 분석에 실패했습니다. 이미지 품질을 확인하거나 다른 사진으로 시도해 주세요.");
    }
};

export const analyzeTextWithGemini = async (foodItem: string): Promise<AnalysisResult> => {
    const textPart = {
        text: `음식 '${foodItem}'에 대한 일반적인 1인분 기준 영양 정보를 알려줘. 음식 이름, 1인분 기준 양(g), 칼로리(kcal), 그리고 탄수화물, 단백질, 지방 함량(g)을 포함해서 아래 JSON 스키마에 맞춰서 정확하게 응답해줘. 만약 분석이 불가능하면, foodItem에 '분석 불가'라고 응답해줘.`,
    };

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: { parts: [textPart] },
            config: {
                systemInstruction: "당신은 전문 영양사입니다. 사용자가 제공한 음식 이름에 대해, 일반적인 영양 정보를 분석하고 제공하는 역할을 합니다. 모든 답변은 제시된 JSON 스키마를 엄격히 따라야 합니다.",
                responseMimeType: "application/json",
                responseSchema: analysisItemSchema,
            },
        });

        const jsonString = response.text.trim();
        const parsedResult = JSON.parse(jsonString) as AnalysisResult;

        if (parsedResult.foodItem === '분석 불가') {
            throw new Error(`'${foodItem}'에 대한 정보를 찾을 수 없습니다.`);
        }

        return parsedResult;

    } catch (error) {
        console.error("Gemini API call failed for text analysis:", error);
        throw new Error("AI 분석에 실패했습니다. 음식 이름을 확인하거나 다른 이름으로 시도해 주세요.");
    }
};


export const getExerciseSuggestion = async (surplusKcal: number, userWeight: number): Promise<string> => {
    const prompt = `
        사용자의 현재 체중은 ${userWeight}kg이고, 오늘 목표보다 ${Math.round(surplusKcal)}kcal를 초과하여 섭취했습니다.
        이 초과 칼로리를 소모할 수 있는 효과적인 운동 루틴을 추천해주세요.
        - 구체적인 운동 종류와 시간을 포함해주세요. (예: 30분 빠르게 걷기 + 15분 스쿼트)
        - 전문적이면서도 동기부여가 되는 친근한 말투로 작성해주세요.
        - 답변은 1-2 문장으로 간결하게 요약해주세요.
    `;
    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
            config: {
                systemInstruction: "당신은 사용자의 건강 데이터를 기반으로 맞춤형 운동 계획을 제안하는 전문 퍼스널 트레이너입니다.",
            }
        });
        return response.text;
    } catch (error) {
        console.error("AI exercise suggestion generation failed:", error);
        throw new Error("AI 운동 추천을 생성하는 데 실패했습니다.");
    }
};


export const getAIFeedback = async (meals: Meal[]): Promise<string> => {
    if (meals.length === 0) {
        return "아직 식단 기록이 없어서 분석할 수 없어요. 오늘 식단을 기록하고 다시 확인해주세요!";
    }

    const recentMeals = meals.slice(0, 15); // 최근 15개 기록으로 제한
    const mealHistory = recentMeals.map(m => 
        `- ${m.time} ${m.foodItem}: ${m.kcal}kcal (탄수화물 ${m.macro.carbs}g, 단백질 ${m.macro.protein}g, 지방 ${m.macro.fat}g)`
    ).join('\n');

    const prompt = `
        다음은 사용자의 최근 식단 기록입니다.
        ${mealHistory}

        이 기록을 바탕으로 전문 영양사의 관점에서 사용자의 식습관을 분석하고, 칭찬할 점과 개선할 점을 찾아 구체적인 조언을 해주세요.
        답변은 친근하고 이해하기 쉬운 말투로, 2-3문장으로 요약해서 제공해주세요.
        예: "점심에 단백질을 잘 챙겨 드시고 계시네요! 다만, 저녁 식사 칼로리가 다소 높은 경향이 있으니 샐러드를 곁들여보는 건 어떨까요?"
    `;

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
        });
        return response.text;
    } catch (error) {
        console.error("AI feedback generation failed:", error);
        throw new Error("AI 피드백을 생성하는 데 실패했습니다.");
    }
};

export const getAIWeeklyReport = async (meals: Meal[]): Promise<string> => {
    if (meals.length < 3) { // 데이터가 너무 적으면 일반적인 메시지 반환
        return "지난 한 주도 수고 많으셨습니다! 꾸준히 식단을 기록하며 건강한 습관을 만들어가요. 다음 주도 화이팅!";
    }

    const mealHistory = meals.map(m => {
        const date = new Date(m.date);
        const day = ['일', '월', '화', '수', '목', '금', '토'][date.getDay()];
        return `- ${day}요일 ${m.time}: ${m.foodItem} (${m.kcal}kcal)`
    }).join('\n');

    const prompt = `
        다음은 사용자의 지난 7일간의 식단 기록입니다.
        ${mealHistory}

        이 기록을 바탕으로, 당신은 매우 친절하고 지지적인 건강 코치입니다. 사용자의 노력을 칭찬하고 격려하는 따뜻한 주간 총평을 작성해주세요.
        1. 가장 잘한 점을 구체적으로 칭찬해주세요. (예: 주말에도 꾸준히 아침을 챙겨드신 점)
        2. 딱 한 가지만 개선하면 좋을 점을 부드럽게 제안해주세요. (예: 저녁 식사 후 간식 섭취 빈도를 조금 줄여보는 건 어떨까요?)
        3. 전체적인 답변은 긍정적인 톤으로, 3~4문장 이내로 작성해주세요.
    `;

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
        });
        return response.text;
    } catch (error) {
        console.error("AI weekly report generation failed:", error);
        throw new Error("AI 주간 리포트를 생성하는 데 실패했습니다.");
    }
};

export const getAIPatternAnalysis = async (stats: {
  lateNightMeals: number;
  weekdayAvgKcal: number;
  weekendAvgKcal: number;
  dateRange: '7d' | '30d';
}): Promise<string> => {
  const { lateNightMeals, weekdayAvgKcal, weekendAvgKcal, dateRange } = stats;
  const period = dateRange === '7d' ? '최근 7일' : '최근 30일';

  let analysisRequest = `
    다음은 사용자의 ${period}간 식습관 패턴 데이터입니다.
    - 야식 횟수: ${lateNightMeals}회
    - 평일 평균 섭취 칼로리: ${Math.round(weekdayAvgKcal)} kcal
    - 주말 평균 섭취 칼로리: ${Math.round(weekendAvgKcal)} kcal

    이 데이터를 바탕으로 전문 영양사의 관점에서 사용자의 식습관 패턴을 심층 분석하고, 긍정적인 점과 개선이 필요한 점을 찾아 구체적인 조언을 해주세요.
    - 야식 습관이 있다면, 건강에 미치는 영향과 줄일 수 있는 현실적인 팁을 제안해주세요.
    - 주말과 평일의 칼로리 섭취량 차이에 대해 분석하고, 그 원인과 긍정적/부정적 측면을 설명해주세요.
    - 전체적인 답변은 **친근하고 격려하는 말투**로 작성하되, 분석 내용은 **구체적이고 명확**해야 합니다.
    - 답변은 2-3개의 문단으로 구성해주세요. 중요한 키워드는 **굵은 글씨**로 강조해주세요.
  `;

  if (lateNightMeals === 0 && Math.abs(weekdayAvgKcal - weekendAvgKcal) < 150) {
      analysisRequest += "\n\n 사용자는 야식도 먹지 않고 주중/주말 섭취량도 매우 일정합니다. 이 좋은 습관을 칭찬하고, 꾸준히 유지할 수 있도록 격려하는 메시지를 중심으로 작성해주세요."
  }

  const prompt = analysisRequest;

  try {
    const response = await ai.models.generateContent({
      model: model,
      contents: prompt,
      config: {
        systemInstruction: "당신은 사용자의 식단 데이터를 분석하여 라이프스타일 패턴을 파악하고 맞춤형 조언을 제공하는 전문 건강 컨설턴트입니다.",
      }
    });
    return response.text;
  } catch (error) {
    console.error("AI pattern analysis generation failed:", error);
    throw new Error("AI 패턴 분석 리포트를 생성하는 데 실패했습니다.");
  }
};


const aiChallengeSchema = {
    type: Type.OBJECT,
    properties: {
        title: {
            type: Type.STRING,
            description: "도전 과제의 흥미로운 이름 (예: '수분 보충의 달인')",
        },
        description: {
            type: Type.STRING,
            description: "도전 과제에 대한 구체적이고 실행 가능한 설명 (예: '오늘 하루 물 8잔 마시기')",
        },
        icon: {
            type: Type.STRING,
            description: "도전 과제의 성격에 맞는 아이콘 이름. 'Zap', 'Target', 'Award' 중 하나를 선택하세요.",
        },
    },
    required: ["title", "description", "icon"],
};


export const getAIChallenge = async (meals: Meal[]): Promise<AIChallenge> => {
    const mealHistory = meals.slice(0, 10).map(m => 
        `- ${m.time} ${m.foodItem}: ${m.kcal}kcal (단백질 ${m.macro.protein}g)`
    ).join('\n');

    const prompt = `
        당신은 사용자가 건강한 습관을 만들도록 돕는 유능한 헬스 코치입니다.
        아래는 사용자의 최근 식단 기록입니다.
        ${mealHistory}

        이 기록을 분석하여, 사용자가 재미있게 시도해볼 만한 '단기적이고 구체적인' 건강 챌린지(퀘스트)를 하나 제안해주세요.
        - 식습관의 약점을 보완하거나, 잘하고 있는 점을 강화하는 방향으로 제안하세요. (예: 아침 거름 -> 아침 챙겨먹기, 단백질 부족 -> 단백질 간식 추가)
        - 지루한 목표가 아닌, 게임 퀘스트처럼 흥미로운 제목과 명확한 행동 지침을 포함해야 합니다.
        - 아래 JSON 스키마에 맞춰 응답해주세요.
    `;

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
            config: {
                responseMimeType: "application/json",
                responseSchema: aiChallengeSchema,
            }
        });

        const jsonString = response.text.trim();
        return JSON.parse(jsonString) as AIChallenge;

    } catch (error) {
        console.error("AI challenge generation failed:", error);
        throw new Error("AI 챌린지를 생성하는 데 실패했습니다.");
    }
};

export const getAIGroupChallengeSuggestions = async (theme: string): Promise<string[]> => {
    const prompt = `사용자가 그룹 챌린지 목표로 '${theme}'라는 키워드를 입력했습니다. 이 키워드를 바탕으로, 구체적이고(Specific), 측정 가능하며(Measurable), 달성 가능하고(Achievable), 관련 있으며(Relevant), 시간 기반의(Time-bound) 'SMART' 챌린지 3가지를 제안해주세요. 각 챌린지는 30자 이내의 짧고 흥미로운 문장으로 만들어주세요. JSON 형식의 배열로 응답해주세요. 예: ["그룹 총합 50,000 kcal 소모하기", "일주일간 저녁 식사는 샐러드 포함하기", "주 3회 운동 기록 인증하기"]`;

    const responseSchema = {
        type: Type.ARRAY,
        items: {
            type: Type.STRING,
            description: "A short, engaging group challenge suggestion."
        }
    };

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
            config: {
                systemInstruction: "당신은 사용자가 재미있게 참여할 수 있는 건강 관련 그룹 챌린지를 만들어주는 창의적인 아이디어 생성기입니다. 모든 답변은 제시된 JSON 스키마를 엄격히 따라야 합니다.",
                responseMimeType: "application/json",
                responseSchema: responseSchema,
            }
        });
        const jsonString = response.text.trim();
        const parsedResult = JSON.parse(jsonString) as string[];

        if (!Array.isArray(parsedResult) || parsedResult.length === 0) {
            throw new Error('No suggestions generated.');
        }

        return parsedResult;
    } catch (error) {
        console.error("AI group challenge suggestion generation failed:", error);
        throw new Error("AI 챌린지 추천을 생성하는 데 실패했습니다. 키워드를 확인하고 다시 시도해 주세요.");
    }
};


const recipeSchema = {
    type: Type.OBJECT,
    properties: {
        name: { type: Type.STRING, description: "레시피의 이름" },
        kcal: { type: Type.NUMBER, description: "1인분 기준 예상 칼로리" },
        ingredients: { type: Type.ARRAY, items: { type: Type.STRING }, description: "필요한 재료 목록" },
        instructions: { type: Type.ARRAY, items: { type: Type.STRING }, description: "조리 방법 단계별 설명" },
    },
    required: ["name", "kcal", "ingredients", "instructions"],
};

export const getAIRecipe = async (meals: Meal[]): Promise<Recipe> => {
    const mealHistory = meals.slice(0, 5).map(m => `${m.foodItem} (${m.macro.protein}g 단백질)`).join(', ');
    const prompt = `
        사용자의 최근 식단은 다음과 같습니다: ${mealHistory}.
        이 식단을 바탕으로, 사용자가 좋아할 만한 건강하고 맛있는 레시피를 하나 추천해주세요. 
        특히 단백질이 부족해 보인다면 단백질이 풍부한 레시피를, 채소가 부족하다면 채소가 많은 레시피를 추천해주세요.
        레시피는 간단하고 따라하기 쉬워야 합니다.
        아래 JSON 스키마에 맞춰 응답해주세요.
    `;

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
            config: {
                systemInstruction: "당신은 사용자의 식습관을 분석하여 맞춤형 건강 레시피를 제안하는 전문 요리사입니다.",
                responseMimeType: "application/json",
                responseSchema: recipeSchema,
            }
        });
        const jsonString = response.text.trim();
        return JSON.parse(jsonString) as Recipe;
    } catch (error) {
        console.error("AI recipe generation failed:", error);
        throw new Error("AI 레시피를 생성하는 데 실패했습니다.");
    }
};

export const getFridgeRecipe = async (ingredients: string, userProfile: UserProfile): Promise<Recipe> => {
    const { gender, age, height, weight, activityLevel } = userProfile;
    let bmr;
    if (gender === 'male') {
        bmr = 10 * weight + 6.25 * height - 5 * age + 5;
    } else {
        bmr = 10 * weight + 6.25 * height - 5 * age - 161;
    }
    const dailyGoal = Math.round(bmr * ACTIVITY_LEVELS[activityLevel]);

    const prompt = `
        사용자의 프로필 정보는 다음과 같습니다:
        - 나이: ${age}세
        - 성별: ${gender === 'male' ? '남성' : '여성'}
        - 일일 목표 칼로리: 약 ${dailyGoal}kcal

        사용자가 현재 가지고 있는 재료는 다음과 같습니다: "${ingredients}"

        위 재료들을 최대한 활용하여, 사용자의 건강 목표에 맞는 건강하고 맛있는 1인분 레시피를 하나 추천해주세요.
        - 만약 재료가 부족하다면, 최소한의 추가 재료(예: 소금, 후추 등 기본 양념)만 사용하도록 제안해주세요.
        - 레시피는 간단하고 따라하기 쉬워야 합니다.
        - 아래 JSON 스키마에 맞춰 응답해주세요. 재료가 너무 부족하여 레시피 생성이 불가능할 경우, name 필드에 "레시피 생성 불가"라고 응답해주세요.
    `;

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
            config: {
                systemInstruction: "당신은 사용자가 가진 재료를 바탕으로 맞춤형 건강 레시피를 만들어주는 창의적인 요리사입니다.",
                responseMimeType: "application/json",
                responseSchema: recipeSchema,
            }
        });
        const jsonString = response.text.trim();
        const parsedResult = JSON.parse(jsonString) as Recipe;
        if (parsedResult.name === "레시피 생성 불가") {
            throw new Error("주어진 재료만으로는 만들 수 있는 레시피를 찾기 어렵습니다. 재료를 추가해보세요.");
        }
        return parsedResult;
    } catch (error) {
        console.error("AI fridge recipe generation failed:", error);
        if (error instanceof Error && error.message.includes("레시피를 찾기 어렵습니다")) {
            throw error;
        }
        throw new Error("AI 레시피를 생성하는 데 실패했습니다.");
    }
};

export const getAIWorkoutPlan = async (meals: Meal[], userProfile: UserProfile): Promise<string> => {
    const today = new Date();
    const oneWeekAgo = new Date(today);
    oneWeekAgo.setDate(today.getDate() - 7);

    const weeklyMeals = meals.filter(m => new Date(m.date) >= oneWeekAgo);
    const totalKcal = weeklyMeals.reduce((sum, meal) => sum + meal.kcal, 0);
    const dayCount = new Set(weeklyMeals.map(m => new Date(m.date).toDateString())).size || 1;
    const avgKcal = Math.round(totalKcal / dayCount);

    const prompt = `
        사용자의 프로필: ${userProfile.age}세 ${userProfile.gender === 'male' ? '남성' : '여성'}, 체중 ${userProfile.weight}kg.
        지난 주 평균 섭취 칼로리: ${avgKcal}kcal.

        이 정보를 바탕으로, 이번 주에 실천할 수 있는 현실적이고 효과적인 주간 운동 계획을 제안해주세요.
        - 전문 트레이너처럼 친근하고 동기를 부여하는 말투로 작성해주세요.
        - 월요일부터 일요일까지의 간단한 계획을 포함해주세요. (예: 월: 가벼운 조깅 30분, 수: 근력 운동, 금: 스트레칭)
        - 전체 답변은 3-4문장으로 간결하게 요약해주세요.
    `;
    
    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
        });
        return response.text;
    } catch (error) {
        console.error("AI workout plan generation failed:", error);
        throw new Error("AI 운동 계획을 생성하는 데 실패했습니다.");
    }
};

const proteinSuggestionSchema = {
    type: Type.ARRAY,
    items: {
        type: Type.OBJECT,
        properties: {
            name: {
                type: Type.STRING,
                description: "추천하는 단백질이 풍부한 음식 또는 간식의 이름 (예: 구운 닭가슴살, 그릭 요거트)",
            },
            reason: {
                type: Type.STRING,
                description: "이 음식을 추천하는 간략한 이유 (예: 간편하게 단백질을 보충할 수 있어요.)",
            },
        },
        required: ["name", "reason"],
    },
};

export const getProteinFoodSuggestions = async (meals: Meal[]): Promise<ProteinFoodSuggestion[]> => {
    const mealHistory = meals.map(m => m.foodItem).join(', ');
    const prompt = `
        사용자의 오늘 식단은 다음과 같습니다: ${mealHistory || '아직 기록 없음'}.
        현재 단백질 섭취가 부족한 상황입니다.
        사용자가 식단에 간단하게 추가할 수 있는, 단백질이 풍부한 음식이나 간식 4가지를 추천해주세요.
        너무 복잡한 요리가 아닌, 편의점이나 마트에서 쉽게 구할 수 있거나 간단히 조리 가능한 것으로 제안해주세요.
        아래 JSON 스키마에 맞춰 응답해주세요.
    `;

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
            config: {
                systemInstruction: "당신은 사용자의 식단 데이터를 기반으로 개인화된 음식 추천을 제공하는 영양 전문가입니다.",
                responseMimeType: "application/json",
                responseSchema: proteinSuggestionSchema,
            }
        });
        const jsonString = response.text.trim();
        return JSON.parse(jsonString) as ProteinFoodSuggestion[];
    } catch (error) {
        console.error("AI protein suggestion generation failed:", error);
        throw new Error("AI 단백질 추천을 생성하는 데 실패했습니다.");
    }
};


export const createChatSession = (meals: Meal[]): Chat => {
    const mealHistory = meals.slice(0, 20).map(m =>
        `- ${new Date(m.date).toLocaleDateString()} ${m.time} ${m.foodItem}: ${m.kcal}kcal`
    ).join('\n');

    const systemInstruction = `
        당신은 사용자의 건강 데이터를 잘 아는 전문 영양사 'HealthMate'입니다.
        친절하고 과학적인 근거를 바탕으로 대화해주세요.
        사용자의 질문에 답변하고, 식단 개선을 위한 구체적이고 실천 가능한 조언을 제공하세요.
        
        아래는 사용자의 최근 식단 기록입니다. 이 정보를 적극적으로 활용하여 개인화된 답변을 해주세요.
        ---
        ${mealHistory || "아직 식단 기록이 없습니다."}
        ---
    `;

    const chat = ai.chats.create({
        model: model,
        config: {
            systemInstruction: systemInstruction,
        },
    });
    return chat;
};

const plannedMealSchema = {
    type: Type.OBJECT,
    properties: {
        name: { type: Type.STRING, description: "음식 이름" },
        kcal: { type: Type.NUMBER, description: "예상 칼로리" },
        description: { type: Type.STRING, description: "간단한 설명이나 구성 요소 (예: 닭가슴살, 현미밥, 샐러드)" },
    },
    required: ["name", "kcal", "description"],
};

const mealPlanSchema = {
    type: Type.OBJECT,
    properties: {
        breakfast: { ...plannedMealSchema, description: "아침 식사" },
        lunch: { ...plannedMealSchema, description: "점심 식사" },
        dinner: { ...plannedMealSchema, description: "저녁 식사" },
        snacks: {
            type: Type.OBJECT,
            properties: plannedMealSchema.properties,
            description: "간식 (선택 사항)"
        },
        totalKcal: { type: Type.NUMBER, description: "하루 총 예상 칼로리" }
    },
    required: ["breakfast", "lunch", "dinner", "totalKcal"],
};


export const getAIMealPlan = async (userProfile: UserProfile, preferences: string): Promise<MealPlan> => {
    const { gender, age, height, weight, activityLevel } = userProfile;
    let bmr;
    if (gender === 'male') {
        bmr = 10 * weight + 6.25 * height - 5 * age + 5;
    } else {
        bmr = 10 * weight + 6.25 * height - 5 * age - 161;
    }
    const dailyGoal = Math.round(bmr * ACTIVITY_LEVELS[activityLevel]);

    const prompt = `
        사용자의 프로필 정보는 다음과 같습니다:
        - 나이: ${age}세
        - 성별: ${gender === 'male' ? '남성' : '여성'}
        - 키: ${height}cm
        - 체중: ${weight}kg
        - 일일 목표 칼로리: 약 ${dailyGoal}kcal

        사용자의 추가적인 식단 요구사항: "${preferences || '특별한 요구사항 없음'}"

        위 정보를 바탕으로, 사용자를 위한 건강하고 균형 잡힌 하루 식단(아침, 점심, 저녁, 그리고 선택적으로 간식 포함)을 계획해주세요.
        각 식사는 구체적인 음식 이름, 예상 칼로리, 간단한 설명을 포함해야 합니다.
        총 칼로리는 사용자의 일일 목표 칼로리에 근접해야 합니다.
        아래 JSON 스키마에 맞춰 정확하게 응답해주세요.
    `;

    try {
        const response = await ai.models.generateContent({
            model: model,
            contents: prompt,
            config: {
                systemInstruction: "당신은 사용자의 프로필과 요구사항에 맞춰 개인화된 하루 식단을 계획해주는 전문 영양사입니다. 모든 답변은 제공된 JSON 스키마를 엄격히 준수해야 합니다.",
                responseMimeType: "application/json",
                responseSchema: mealPlanSchema,
            }
        });
        const jsonString = response.text.trim();
        return JSON.parse(jsonString) as MealPlan;
    } catch (error) {
        console.error("AI meal plan generation failed:", error);
        throw new Error("AI 식단 계획을 생성하는 데 실패했습니다. 다시 시도해 주세요.");
    }
};