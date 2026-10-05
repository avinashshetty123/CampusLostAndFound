package com.example.lostandfoundfrontend.model

import java.util.UUID

enum class ItemStatus {
    LOST, FOUND
}

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val status: ItemStatus = ItemStatus.LOST,
    val category: String = "",
    val contactInfo: String = "",
    val reporterId: String = "",
    val reporterName: String = "Anonymous",
    val reporterEmail: String = "",
    val reporterPhone: String = "",
    val reporterDept: String = "Campus Student",
    val reportedAt: String = "",
    val imageUrl: String? = null,
    val isResolved: Boolean = false,
    val isSaved: Boolean = false,
    val claimedByName: String? = null,
    val claimMessage: String? = null
)
