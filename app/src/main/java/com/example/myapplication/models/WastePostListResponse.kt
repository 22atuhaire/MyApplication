package com.example.myapplication.models

data class WastePostListResponse(
    val data: List<WastePost> = emptyList(),
    val meta: WastePostsMeta? = null
)

data class WastePostsMeta(
    val current_page: Int? = null,
    val last_page: Int? = null,
    val per_page: Int? = null,
    val total: Int? = null
)

