package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phone: String, // Normalized 10-digit number
    val name: String,
    val address: String = "",
    val area: String = "",
    val role: String = "CUSTOMER", // "CUSTOMER", "ADMIN"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "services")
data class ServiceItem(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val estimatedTime: String,
    val startingPrice: Int = 0,
    val iconType: String = "AC_SERVICE", // "AC_SERVICE", "JET_PUMP", "GAS_CHARGING", "FRIDGE", "INSTALLATION", "PCB_REPAIR"
    val isActive: Boolean = true
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val area: String,
    val serviceName: String,
    val units: Int = 1,
    val modelType: String = "Split AC", // "Split AC", "Window AC", "Inverter AC", "Single Door Fridge", "Double Door Fridge", "Deep Freezer"
    val issueNotes: String = "",
    val couponCode: String = "",
    val discountAmount: Int = 0,
    val billAmount: Int = 0,
    val sparePartsCharge: Int = 0,
    val status: String = "PENDING", // "PENDING", "TECHNICIAN_ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    val technicianName: String = "Sanjay Prajapati",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "promo_offers")
data class PromoOffer(
    @PrimaryKey val code: String,
    val title: String,
    val discountPercent: Int,
    val maxDiscount: Int,
    val description: String,
    val isActive: Boolean = true
)

@Entity(tableName = "gallery_items")
data class GalleryItem(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "All", "AC Jet Wash", "Installation", "Fridge Repair", "Gas Charging", "PCB & Compressor"
    val description: String,
    val imageUrl: String = "",
    val tag: String = "Verified Work"
)

@Entity(tableName = "reviews")
data class ReviewItem(
    @PrimaryKey val id: String,
    val customerName: String,
    val rating: Float = 5.0f,
    val dateText: String = "Recent",
    val comment: String,
    val serviceUsed: String,
    val ownerReply: String? = null,
    val ownerReplyBy: String = "Sanjay & Sandip Prajapati"
)

data class Invoice(
    val bookingId: String,
    val customerName: String,
    val customerPhone: String,
    val serviceName: String,
    val serviceCharge: Int,
    val sparePartsCharge: Int,
    val discountAmount: Int,
    val totalAmount: Int,
    val invoiceNumber: String,
    val dateText: String,
    val technicianName: String
)
