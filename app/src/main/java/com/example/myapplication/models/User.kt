package com.example.myapplication.models

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val avatar: String?,
    val created_at: String
)