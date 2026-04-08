package com.example.myapplication.models

data class CollectorRegistrationResponse(
    val success: Boolean,
    val message: String,
    val user: CollectorUser?
)

data class CollectorUser(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String?,
    val vehicle_type: String,
    val collector_status: String
)