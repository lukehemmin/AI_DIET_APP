package com.lukehemmin.ai_diet_app.network;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Request;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Android Emulator uses 10.0.2.2 to access localhost of the host machine
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    public static Retrofit getClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging);

        if (context != null) {
            clientBuilder.addInterceptor(chain -> {
                SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                String token = prefs.getString("auth_token", null);

                Request.Builder requestBuilder = chain.request().newBuilder();
                if (token != null) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                }
                return chain.proceed(requestBuilder.build());
            });
        }

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build();
    }
}
