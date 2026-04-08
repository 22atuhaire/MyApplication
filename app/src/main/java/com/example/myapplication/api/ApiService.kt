package com.example.myapplication.api

import com.example.myapplication.models.RegisterRequest
import com.example.myapplication.models.LoginRequest
import com.example.myapplication.models.AuthResponse
import com.example.myapplication.models.HealthResponse
import com.example.myapplication.models.CollectorLoginRequest
import com.example.myapplication.models.CollectorLoginResponse
import com.example.myapplication.models.CollectorRegistrationRequest
import com.example.myapplication.models.CollectorRegistrationResponse
import com.example.myapplication.models.StatusResponse
import com.example.myapplication.models.DashboardStatsResponse
import com.example.myapplication.models.AvailableJob
import com.example.myapplication.models.ActiveCollection
import com.example.myapplication.models.WastePostRequest
import com.example.myapplication.models.WastePostResponse
import com.example.myapplication.models.CollectorMatchResponse
import com.example.myapplication.models.WastePostListResponse

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {

    @GET("health")
    fun checkHealth(): Call<HealthResponse>

    // Regular user registration (donors)
    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    // Collector registration
    @POST("collector/register")
    fun collectorRegister(@Body request: CollectorRegistrationRequest): Call<CollectorRegistrationResponse>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @GET("collector/status/{phone}")
    fun checkCollectorStatus(@Path("phone") phone: String): Call<StatusResponse>

    @POST("collector/login")
    fun collectorLogin(@Body request: CollectorLoginRequest): Call<CollectorLoginResponse>

    // Protected endpoints below rely on RetrofitClient's auth interceptor.
    @GET("collector/dashboard/stats")
    fun getDashboardStats(): Call<DashboardStatsResponse>

    @GET("collector/jobs/available")
    fun getAvailableJobs(): Call<List<AvailableJob>>

    @GET("waste-posts")
    fun getAvailableDonorPosts(): Call<WastePostListResponse>

    @GET("collector/jobs/active")
    fun getActiveCollections(): Call<List<ActiveCollection>>

    @POST("waste-posts")
    fun createWastePost(@Body request: WastePostRequest): Call<WastePostResponse>

    // Check if collector has accepted this post
    @GET("waste-posts/{id}/match")
    fun checkCollectorMatch(
        @Path("id") postId: Int
    ): Call<CollectorMatchResponse>

    // Get estimated wait time
    @GET("waste-posts/{id}/eta")
    fun getEstimatedETA(
        @Path("id") postId: Int
    ): Call<Map<String, Int>>

}