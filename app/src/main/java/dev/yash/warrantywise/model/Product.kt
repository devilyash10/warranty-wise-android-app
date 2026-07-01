package dev.yash.warrantywise.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Product(
    @DocumentId val productId: String = "",
    val userId: String = "",
    val productName: String = "",
    val brand: String = "",
    val category: String = "",
    val purchaseDate: Long = 0L,
    val warrantyPeriod: Int = 0,       // in months
    val invoiceImageUrl: String = "",
    val notes: String = "",
    val createdAt: Long = 0L
) {
    val warrantyExpiryDate: Long
        get() = purchaseDate + (warrantyPeriod.toLong() * 30L * 24L * 60L * 60L * 1000L)

    val warrantyStatus: WarrantyStatus
        get() {
            val now = System.currentTimeMillis()
            val thirtyDaysMs = 30L * 24L * 60L * 60L * 1000L
            return when {
                warrantyExpiryDate < now -> WarrantyStatus.EXPIRED
                warrantyExpiryDate - now <= thirtyDaysMs -> WarrantyStatus.EXPIRING_SOON
                else -> WarrantyStatus.ACTIVE
            }
        }

    val daysUntilExpiry: Long
        get() = (warrantyExpiryDate - System.currentTimeMillis()) / (24L * 60L * 60L * 1000L)
}

enum class WarrantyStatus { ACTIVE, EXPIRING_SOON, EXPIRED }

val productCategories = listOf(
    "Smartphone", "Laptop", "Television", "Refrigerator",
    "Washing Machine", "Air Conditioner", "Camera", "Tablet",
    "Smartwatch", "Headphones", "Other"
)
