package com.example.myapplication.models

data class DashboardData(
    val collectorName: String,
    val todayJobs: Int,
    val todayEarnings: Double,
    val rating: Double,
    val totalJobs: Int,
    val totalKg: Double,
    val totalEarnings: Double
)