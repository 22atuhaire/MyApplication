package com.example.myapplication.models

data class CollectorMatchResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: CollectorMatchData? = null
)

data class CollectorMatchData(
    val post_id: Int? = null,
    val collector: CollectorInfo? = null
)

data class CollectorInfo(
    val id: Int? = null,
    val name: String = "",
    val phone: String? = null,
    val rating: Double? = null,
    val vehicle_type: String? = null,
    val eta_minutes: Int? = null
)