package dev.yash.warrantywise.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ServiceHistory(
    @DocumentId val serviceId: String = "",
    val productId: String = "",
    val serviceDate: Long = 0L,
    val description: String = "",
    val cost: Double = 0.0,
    val serviceCenter: String = "",
    val createdAt: Long = 0L
)
