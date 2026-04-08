package com.example.myapplication.models

data class GoogleLoginRequest(
    val email: String,
    val name: String,
    val google_id: String,
    val avatar: String?
)