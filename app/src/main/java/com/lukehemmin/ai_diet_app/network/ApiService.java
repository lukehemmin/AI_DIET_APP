package com.lukehemmin.ai_diet_app.network;

import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.AuthResponse;
import com.lukehemmin.ai_diet_app.data.model.LoginRequest;

import com.lukehemmin.ai_diet_app.data.model.EmailVerificationRequest;
import com.lukehemmin.ai_diet_app.data.model.SignupRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    
    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/auth/send-code")
    Call<ApiResponse<Void>> sendVerificationCode(@Body EmailVerificationRequest request);

    @POST("api/auth/verify-code")
    Call<ApiResponse<Boolean>> verifyCode(@Body EmailVerificationRequest request);

    @POST("api/auth/signup")
    Call<ApiResponse<AuthResponse>> signup(@Body SignupRequest request);
}
