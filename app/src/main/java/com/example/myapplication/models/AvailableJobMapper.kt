package com.example.myapplication.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun WastePost.toAvailableJob(): AvailableJob {
    val readableTypes = if (!waste_types.isNullOrEmpty()) {
        waste_types.joinToString("/") { it.toReadableWasteType() }
    } else {
        "Mixed Food"
    }

    val fallbackDonorName = donor_id?.let { "Donor #$it" } ?: "Donor"

    return AvailableJob(
        id = id ?: 0,
        foodType = readableTypes,
        quantity = quantity ?: 0.0,
        distance = distance_km,
        address = address?.ifBlank { "Address not provided" } ?: "Address not provided",
        donorName = donor?.name?.takeIf { it.isNotBlank() } ?: fallbackDonorName,
        donorRating = donor?.rating ?: 0.0,
        timeAgo = formatTimeAgo(created_at),
        instructions = instructions?.takeIf { it.isNotBlank() } ?: notes?.takeIf { it.isNotBlank() },
        photos = photos
    )
}

private fun String.toReadableWasteType(): String {
    return when (lowercase(Locale.US)) {
        "cooked" -> "Cooked Food"
        "vegetables" -> "Vegetables"
        "bakery" -> "Bakery"
        "meat" -> "Meat/Dairy"
        "mixed" -> "Mixed Food"
        else -> replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }
}

private fun formatTimeAgo(createdAt: String?): String {
    val createdMillis = parseCreatedAtMillis(createdAt) ?: return "Just now"
    val diff = (System.currentTimeMillis() - createdMillis).coerceAtLeast(0L)

    val minutes = diff / 60_000L
    if (minutes < 1) return "Just now"
    if (minutes < 60) return "$minutes min ago"

    val hours = minutes / 60
    if (hours < 24) return "$hours hr ago"

    val days = hours / 24
    return "$days day ago" + if (days == 1L) "" else "s"
}

private fun parseCreatedAtMillis(createdAt: String?): Long? {
    if (createdAt.isNullOrBlank()) return null

    val normalized = createdAt.replace("Z", "")

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val parsed: Date? = parser.parse(normalized)
            if (parsed != null) {
                return parsed.time
            }
        } catch (_: Exception) {
            // Try the next pattern.
        }
    }

    return null
}

