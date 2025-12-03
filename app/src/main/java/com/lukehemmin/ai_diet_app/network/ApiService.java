package com.lukehemmin.ai_diet_app.network;

import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.AuthResponse;
import com.lukehemmin.ai_diet_app.data.model.ChatRequest;
import com.lukehemmin.ai_diet_app.data.model.ChatResponse;
import com.lukehemmin.ai_diet_app.data.model.LoginRequest;

import com.lukehemmin.ai_diet_app.data.model.EmailVerificationRequest;
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

import java.util.List;

public interface ApiService {
    
    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/auth/send-code")
    Call<ApiResponse<Void>> sendVerificationCode(@Body EmailVerificationRequest request);

    @POST("api/auth/verify-code")
    Call<ApiResponse<Boolean>> verifyCode(@Body EmailVerificationRequest request);

    @POST("api/auth/signup")
    Call<ApiResponse<AuthResponse>> signup(@Body SignupRequest request);

    @Multipart
    @POST("api/meals/analyze")
    Call<ApiResponse<MealAnalysisResponse>> analyzeMeal(@Part MultipartBody.Part image);

    @POST("api/meals")
    Call<ApiResponse<java.util.Map<String, java.util.List<com.lukehemmin.ai_diet_app.data.model.MealResponse>>>> createMeals(@Body com.lukehemmin.ai_diet_app.data.model.MealCreateRequest request);

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
}
