package com.example.myapplication.models

data class StatusResponse(
    val success: Boolean,
    val status: String?,
    val user: StatusUser?,
    val message: String?
)

data class StatusUser(
    val name: String,
    val phone: String,
    val email: String?,
    val vehicle_type: String,
    val registered_at: String
)