package com.example.myapplication.models

data class WastePost(
    val id: Int? = null,
    val donor_id: Int? = null,
    val donor: DonorSummary? = null,
    val waste_types: List<String>? = null,
    val quantity: Double? = null,
    val notes: String? = null,
    val pickup_time: String? = null,
    val address: String? = null,
    val instructions: String? = null,
    val photos: List<String>? = null,
    val status: String? = null,
    val collector_id: Int? = null,
    val distance_km: Double? = null,
    val created_at: String? = null
)

data class DonorSummary(
    val id: Int? = null,
    val name: String? = null,
    val rating: Double? = null
)