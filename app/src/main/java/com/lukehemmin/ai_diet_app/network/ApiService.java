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
import com.lukehemmin.ai_diet_app.data.model.WeeklyReportResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @GET("api/analytics/weekly-report")
    Call<ApiResponse<WeeklyReportResponse>> getWeeklyReport();

    @retrofit2.http.PUT("api/members/me")
    Call<ApiResponse<UserProfile>> updateProfile(@Body UserProfile request);

    @Multipart
    @POST("api/members/me/profile-image")
    Call<ApiResponse<java.util.Map<String, String>>> uploadProfileImage(@Part MultipartBody.Part image);

    @DELETE("api/members/me/profile-image")
    Call<ApiResponse<java.util.Map<String, String>>> deleteProfileImage();

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

    @GET("api/groups/{groupId}")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.GroupDetailResponse>> getGroupDetail(@Path("groupId") String groupId);

    @GET("api/groups/my")
    Call<ApiResponse<java.util.Map<String, java.util.List<GroupResponse>>>> getMyGroups();

    @retrofit2.http.PUT("api/groups/{groupId}")
    Call<ApiResponse<java.util.Map<String, GroupResponse>>> updateGroup(@Path("groupId") String groupId, @Body GroupCreateRequest request);

    @POST("api/groups/{groupId}/leave")
    Call<ApiResponse<java.util.Map<String, String>>> leaveGroup(@Path("groupId") String groupId);

    @DELETE("api/groups/{groupId}")
    Call<ApiResponse<java.util.Map<String, String>>> deleteGroup(@Path("groupId") String groupId);

    @GET("api/groups/{groupId}/role")
    Call<ApiResponse<java.util.Map<String, String>>> getGroupRole(@Path("groupId") String groupId);

    // Group Feed
    @GET("api/groups/{groupId}/feeds")
    Call<ApiResponse<java.util.Map<String, Object>>> getGroupFeeds(
            @Path("groupId") String groupId,
            @retrofit2.http.Query("page") int page,
            @retrofit2.http.Query("size") int size);

    @POST("api/groups/share-meal")
    Call<ApiResponse<java.util.Map<String, Object>>> shareMealToGroups(@Body java.util.Map<String, Object> request);

    @POST("api/groups/feeds/{feedId}/comments")
    Call<ApiResponse<Object>> addFeedComment(@Path("feedId") String feedId, @Body java.util.Map<String, String> request);

    @GET("api/groups/feeds/{feedId}/comments")
    Call<ApiResponse<java.util.Map<String, Object>>> getFeedComments(@Path("feedId") String feedId);

    @POST("api/groups/feeds/{feedId}/reactions")
    Call<ApiResponse<java.util.Map<String, Object>>> toggleFeedReaction(@Path("feedId") String feedId, @Body java.util.Map<String, String> request);

    @DELETE("api/groups/feeds/{feedId}")
    Call<ApiResponse<java.util.Map<String, String>>> deleteFeed(@Path("feedId") String feedId);

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

    // Diet Analytics (칼로리 트렌드, 영양 균형, 식습관 분석 등)
    @GET("api/analytics/diet")
    Call<ApiResponse<com.lukehemmin.ai_diet_app.data.model.DietAnalyticsResponse>> getDietAnalytics(@retrofit2.http.Query("days") int days);

    // Chat History
    @GET("api/ai/chat/history")
    Call<ApiResponse<java.util.List<com.lukehemmin.ai_diet_app.data.model.ChatHistoryResponse>>> getChatHistory(@retrofit2.http.Query("limit") int limit);

    @DELETE("api/ai/chat/history")
    Call<ApiResponse<String>> clearChatHistory();
}
