package com.example.myapplication.models

data class WastePostResponse(
    val success: Boolean,
    val message: String,
    val data: WastePostData?
)

data class WastePostData(
    val id: Int,
    val status: String,
    val estimated_pickup_time: String?
)