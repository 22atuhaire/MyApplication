package com.example.myapplication.models

import com.google.gson.annotations.SerializedName

data class CollectorLoginResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("user") val user: CollectorLoginUser? = null
) {
    // Backend may omit success but still return a token for successful login.
    val isSuccessfulLogin: Boolean
        get() = (success == true) || !token.isNullOrBlank()
}

data class CollectorLoginUser(
    val id: Int,
    val name: String,
    val phone: String?,
    val email: String?,
    val role: String,
    val status: String,
)