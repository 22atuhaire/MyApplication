package com.example.myapplication.models

data class ActiveCollection(
    val id: Int,
    val foodType: String,
    val quantity: Double,
    val donorName: String,
    val donorPhone: String,
    val address: String,
    val instructions: String?,
    val status: String // "accepted", "en_route", "arrived"
)