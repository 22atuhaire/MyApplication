package com.example.myapplication.models

data class DashboardStatsResponse(
    val success: Boolean,
    val data: DashboardStats
)

data class DashboardStats(
    val todayJobs: Int,
    val todayEarnings: Double,
    val rating: Double,
    val totalJobs: Int,
    val totalKg: Double,
    val totalEarnings: Double
)