package com.example.myapplication.models

data class WastePostRequest(
    val waste_types: List<String>,
    val quantity: Double,
    val notes: String?,
    val pickup_time: String,
    val address: String,
    val instructions: String?,
    val photos: List<String>? // Optional
)