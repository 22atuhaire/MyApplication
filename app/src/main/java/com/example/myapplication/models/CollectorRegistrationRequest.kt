package com.example.myapplication.models

data class CollectorRegistrationRequest(
    val name: String,
    val phone: String,
    val email: String?,
    val vehicle_type: String,
    val password: String
)