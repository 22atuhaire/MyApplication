package com.example.myapplication.models

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User?,
    val token: String?
)