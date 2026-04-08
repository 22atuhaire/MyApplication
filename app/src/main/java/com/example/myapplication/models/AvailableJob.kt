package com.example.myapplication.models

data class AvailableJob(
    val id: Int,
    val foodType: String,
    val quantity: Double,
    val distance: Double?,
    val address: String,
    val donorName: String,
    val donorRating: Double,
    val timeAgo: String,
    val instructions: String?,
    val photos: List<String>?
)