package com.lukehemmin.ai_diet_app.network;

import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.AuthResponse;
import com.lukehemmin.ai_diet_app.data.model.ChatRequest;
import com.lukehemmin.ai_diet_app.data.model.ChatResponse;
import com.lukehemmin.ai_diet_app.data.model.LoginRequest;

import com.lukehemmin.ai_diet_app.data.model.EmailVerificationRequest;
import com.lukehemmin.ai_diet_app.data.model.FindIdRequest;
import com.lukehemmin.ai_diet_app.data.model.PasswordResetConfirmRequest;
import com.lukehemmin.ai_diet_app.data.model.SignupRequest;

import com.lukehemmin.ai_diet_app.data.model.ChallengeResponse;
import com.lukehemmin.ai_diet_app.data.model.FriendRequest;
import com.lukehemmin.ai_diet_app.data.model.FriendResponse;
import com.lukehemmin.ai_diet_app.data.model.GroupCreateRequest;
import com.lukehemmin.ai_diet_app.data.model.GroupResponse;
import com.lukehemmin.ai_diet_app.data.model.MealAnalysisResponse;
import com.lukehemmin.ai_diet_app.data.model.UserChallengeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import okhttp3.MultipartBody;

import com.lukehemmin.ai_diet_app.data.model.BadgeResponse;

import com.lukehemmin.ai_diet_app.data.model.UserProfile;

import java.util.List;

public interface ApiService {
    
    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/auth/google-login")
    Call<ApiResponse<AuthResponse>> googleLogin(@Body com.lukehemmin.ai_diet_app.data.model.GoogleLoginRequest request);

    @POST("api/auth/find-id")
    Call<ApiResponse<java.util.List<String>>> findId(@Body com.lukehemmin.ai_diet_app.data.model.FindIdRequest request);

    @POST("api/auth/password-reset/request")
    Call<ApiResponse<Void>> requestPasswordReset(@Body EmailVerificationRequest request);

    @POST("api/auth/password-reset/confirm")
    Call<ApiResponse<Void>> confirmPasswordReset(@Body com.lukehemmin.ai_diet_app.data.model.PasswordResetConfirmRequest request);

    @POST("api/auth/send-code")
    Call<ApiResponse<Void>> sendVerificationCode(@Body EmailVerificationRequest request);

    @POST("api/auth/verify-code")
    Call<ApiResponse<Boolean>> verifyCode(@Body EmailVerificationRequest request);


    @POST("api/auth/signup")
    Call<ApiResponse<AuthResponse>> signup(@Body SignupRequest request);

    @GET("api/members/me")
    Call<ApiResponse<UserProfile>> getProfile();

    @retrofit2.http.PUT("api/members/me")
    Call<ApiResponse<UserProfile>> updateProfile(@Body UserProfile request);

    @Multipart
    @POST("api/meals/analyze")
    Call<ApiResponse<MealAnalysisResponse>> analyzeMeal(@Part MultipartBody.Part image);

    @POST("api/meals")
    Call<ApiResponse<java.util.Map<String, java.util.List<com.lukehemmin.ai_diet_app.data.model.MealResponse>>>> createMeals(@Body com.lukehemmin.ai_diet_app.data.model.MealCreateRequest request);

    @GET("api/meals")
    Call<ApiResponse<java.util.Map<String, Object>>> getMeals(@retrofit2.http.Query("startDate") String date);

    @POST("api/ai/chat")
    Call<ApiResponse<ChatResponse>> chat(@Body ChatRequest request);

    @POST("api/groups")
    Call<ApiResponse<java.util.Map<String, GroupResponse>>> createGroup(@Body GroupCreateRequest request);

    @GET("api/groups")
    Call<ApiResponse<java.util.Map<String, java.util.List<GroupResponse>>>> getGroups();

    // Friends
    @POST("api/friends/request")
    Call<ApiResponse<Void>> sendFriendRequest(@Body FriendRequest request);

    @POST("api/friends/{friendshipId}/accept")
    Call<ApiResponse<Void>> acceptFriendRequest(@Path("friendshipId") String friendshipId);

    @GET("api/friends")
    Call<ApiResponse<java.util.Map<String, java.util.List<FriendResponse>>>> getFriends();

    @GET("api/friends/requests")
    Call<ApiResponse<java.util.Map<String, java.util.List<FriendResponse>>>> getFriendRequests();

    // Challenges
    @GET("api/challenges")
    Call<ApiResponse<java.util.Map<String, java.util.List<ChallengeResponse>>>> getAllChallenges();

    @GET("api/challenges/my")
    Call<ApiResponse<java.util.Map<String, java.util.List<UserChallengeResponse>>>> getMyChallenges();

    @POST("api/challenges/{challengeId}/join")
    Call<ApiResponse<java.util.Map<String, UserChallengeResponse>>> joinChallenge(@Path("challengeId") String challengeId);

    // Badges
    @GET("api/badges/my")
    Call<ApiResponse<List<BadgeResponse>>> getMyBadges();

    // Water Intake
    @GET("api/water")
    Call<ApiResponse<java.util.Map<String, Object>>> getWaterIntake(@retrofit2.http.Query("date") String date);

    @POST("api/water/add")
    Call<ApiResponse<java.util.Map<String, Object>>> addWaterGlass(@retrofit2.http.Query("date") String date);

    @POST("api/water/remove")
    Call<ApiResponse<java.util.Map<String, Object>>> removeWaterGlass(@retrofit2.http.Query("date") String date);

    // AI Analysis
    @GET("api/ai-analysis/exercise-plan")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse>> getExercisePlan();

    @POST("api/ai-analysis/exercise-plan/refresh")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse>> refreshExercisePlan(@retrofit2.http.Query("force") boolean force);

    @GET("api/ai-analysis/custom-recipe")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse>> getCustomRecipe();

    @POST("api/ai-analysis/custom-recipe/refresh")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse>> refreshCustomRecipe(@retrofit2.http.Query("force") boolean force);

    @POST("api/ai-analysis/fridge-recipe")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse>> generateFridgeRecipe(@Body java.util.Map<String, String> request);

    // 최근 냉장고 레시피 조회
    @GET("api/ai-analysis/fridge-recipe/latest")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.FridgeRecipeHistoryResponse>> getLatestFridgeRecipe();

    // 냉장고 레시피 히스토리 목록
    @GET("api/ai-analysis/fridge-recipe/history")
    Call<ApiResponse<java.util.List<com.lukehemmin.ai_diet_app.data.model.FridgeRecipeHistoryResponse>>> getFridgeRecipeHistory();

    // AI 하루 식단 계획 생성
    @POST("api/ai-analysis/daily-meal-plan")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.AiAnalysisResponse>> generateDailyMealPlan(@Body java.util.Map<String, String> request);

    // AI Analysis Preload (백그라운드 캐시 갱신)
    @POST("api/ai-analysis/preload")
    Call<ApiResponse<String>> preloadAiCache();
}
