package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.local.BalajiDao
import com.example.data.local.UserPreferences
import com.example.data.model.*
import com.example.data.remote.FirebaseManager
import com.example.data.remote.GoogleSheetsApiClient
import kotlinx.coroutines.flow.Flow
import java.net.URLEncoder
import java.util.UUID

class BalajiRepository(
    private val dao: BalajiDao,
    val userPreferences: UserPreferences,
    private val sheetsClient: GoogleSheetsApiClient = GoogleSheetsApiClient(),
    val firebaseManager: FirebaseManager? = null
) {
    // Phone & WhatsApp constants
    companion object {
        const val BUSINESS_PHONE = "+919157896306"
        const val BUSINESS_PHONE_CLEAN = "919157896306"
        const val BUSINESS_EMAIL = "sandipprajapati0906@gmail.com"
        const val BUSINESS_MAPS_URL = "https://maps.app.goo.gl/2x3fx1iMLMrsg8am7"
        const val ADMIN_PIN = "1099"
    }

    // Services
    val allServices: Flow<List<ServiceItem>> = dao.getAllServices()
    val activeServices: Flow<List<ServiceItem>> = dao.getActiveServices()

    suspend fun addService(service: ServiceItem) {
        dao.insertService(service)
    }

    suspend fun updateService(service: ServiceItem) {
        dao.updateService(service)
    }

    suspend fun deleteService(id: String) {
        dao.deleteService(id)
    }

    // Bookings
    val allBookings: Flow<List<Booking>> = dao.getAllBookings()

    fun getBookingsForCustomer(phone: String): Flow<List<Booking>> {
        return dao.getBookingsForPhone(phone)
    }

    suspend fun createBooking(
        customerName: String,
        customerPhone: String,
        address: String,
        area: String,
        serviceName: String,
        units: Int,
        modelType: String,
        issueNotes: String,
        couponCode: String,
        discountAmount: Int
    ): Booking {
        val bookingId = "BK-" + (100000 + (Math.random() * 900000).toInt())
        val booking = Booking(
            id = bookingId,
            customerName = customerName,
            customerPhone = customerPhone,
            address = address,
            area = area,
            serviceName = serviceName,
            units = units,
            modelType = modelType,
            issueNotes = issueNotes,
            couponCode = couponCode,
            discountAmount = discountAmount,
            status = "PENDING"
        )
        dao.insertBooking(booking)

        // Sync with Google Sheets in background if URL configured
        val url = userPreferences.getAppsScriptUrl()
        if (url.isNotBlank()) {
            try {
                sheetsClient.addBookingToSheet(
                    webAppUrl = url,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    address = address,
                    area = area,
                    serviceName = serviceName,
                    units = units,
                    modelType = modelType,
                    issueNotes = issueNotes,
                    couponCode = couponCode,
                    discountAmount = discountAmount
                )
            } catch (_: Exception) {}
        }
        // Sync with Firebase Firestore
        try {
            firebaseManager?.syncBookingToFirestore(booking)
        } catch (_: Exception) {}

        return booking
    }

    suspend fun updateBookingStatus(bookingId: String, newStatus: String) {
        dao.updateBookingStatus(bookingId, newStatus)
        val url = userPreferences.getAppsScriptUrl()
        if (url.isNotBlank()) {
            try {
                sheetsClient.updateBookingStatusInSheet(url, bookingId, newStatus)
            } catch (_: Exception) {}
        }
    }

    suspend fun completeBookingWithInvoice(
        bookingId: String,
        serviceCharge: Int,
        spareParts: Int,
        discount: Int
    ) {
        val total = (serviceCharge + spareParts) - discount
        dao.completeBookingWithBill(bookingId, total, spareParts, discount)
        val url = userPreferences.getAppsScriptUrl()
        if (url.isNotBlank()) {
            try {
                sheetsClient.updateBookingStatusInSheet(
                    webAppUrl = url,
                    bookingId = bookingId,
                    status = "COMPLETED",
                    billAmount = total,
                    spareParts = spareParts
                )
            } catch (_: Exception) {}
        }
    }

    // Users & Auth
    val allUsers: Flow<List<User>> = dao.getAllUsers()

    suspend fun getUserByPhone(phone: String): User? = dao.getUserByPhone(phone)

    suspend fun registerOrUpdateUser(
        phone: String,
        name: String,
        address: String = "",
        area: String = ""
    ): User {
        val user = User(
            phone = phone,
            name = name,
            address = address,
            area = area,
            role = if (phone == "9157896306") "ADMIN" else "CUSTOMER"
        )
        dao.insertUser(user)
        userPreferences.saveUserSession(phone, name, address, area)

        val url = userPreferences.getAppsScriptUrl()
        if (url.isNotBlank()) {
            try {
                sheetsClient.syncUserToSheet(url, phone, name, address, area)
            } catch (_: Exception) {}
        }
        // Sync with Firebase Firestore
        try {
            firebaseManager?.syncUserToFirestore(user)
        } catch (_: Exception) {}

        return user
    }

    suspend fun deleteUser(phone: String) {
        dao.deleteUser(phone)
    }

    // Offers
    val allOffers: Flow<List<PromoOffer>> = dao.getAllOffers()
    val activeOffers: Flow<List<PromoOffer>> = dao.getActiveOffers()

    suspend fun getOfferByCode(code: String): PromoOffer? = dao.getOfferByCode(code.trim().uppercase())

    suspend fun addOffer(offer: PromoOffer) {
        dao.insertOffer(offer)
    }

    suspend fun deleteOffer(code: String) {
        dao.deleteOffer(code)
    }

    // Reviews
    val allReviews: Flow<List<ReviewItem>> = dao.getAllReviews()

    suspend fun addReview(review: ReviewItem) {
        dao.insertReview(review)
    }

    suspend fun addOwnerReply(reviewId: String, reply: String, replyBy: String = "Balaji Air Conditioners") {
        dao.addOwnerReply(reviewId, reply, replyBy)
    }

    suspend fun deleteReview(reviewId: String) {
        dao.deleteReview(reviewId)
    }

    // Gallery
    val allGalleryItems: Flow<List<GalleryItem>> = dao.getAllGalleryItems()

    suspend fun addGalleryItem(item: GalleryItem) {
        dao.insertGalleryItem(item)
    }

    suspend fun deleteGalleryItem(id: String) {
        dao.deleteGalleryItem(id)
    }

    // WhatsApp Helpers
    fun formatBookingWhatsAppText(booking: Booking): String {
        return buildString {
            appendLine("❄️ *BALAJI AIR CONDITIONERS - NEW BOOKING* ❄️")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📌 *Booking ID:* ${booking.id}")
            appendLine("👤 *Customer Name:* ${booking.customerName}")
            appendLine("📞 *Contact Number:* ${booking.customerPhone}")
            appendLine("🏠 *Service Address:* ${booking.address}")
            if (booking.area.isNotBlank()) {
                appendLine("📍 *Area / Landmark:* ${booking.area}")
            }
            appendLine("🛠️ *Service Selected:* ${booking.serviceName}")
            appendLine("🔢 *Units:* ${booking.units} | *Type:* ${booking.modelType}")
            if (booking.issueNotes.isNotBlank()) {
                appendLine("📝 *Issue / Problem:* ${booking.issueNotes}")
            }
            if (booking.couponCode.isNotBlank()) {
                appendLine("🏷️ *Coupon Applied:* ${booking.couponCode} (Saved ₹${booking.discountAmount})")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("✨ *Payment:* Pay After Service (Cooling Guaranteed!)")
            appendLine("🚀 Sanjay & Sandip Prajapati: +91 9157896306")
        }
    }

    fun formatInvoiceWhatsAppText(
        booking: Booking,
        serviceCharge: Int,
        spareParts: Int,
        discount: Int
    ): String {
        val total = (serviceCharge + spareParts) - discount
        val invNo = "INV-${System.currentTimeMillis() % 100000}"
        return buildString {
            appendLine("🧾 *BALAJI AIR CONDITIONERS & REPAIR*")
            appendLine("📍 *AC & Fridge Repair Service Guarantee*")
            appendLine("📞 Sanjay & Sandip Prajapati: +91 9157896306")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📄 *Invoice No:* $invNo")
            appendLine("👤 *Customer:* ${booking.customerName} (${booking.customerPhone})")
            appendLine("🏠 *Location:* ${booking.address}, ${booking.area}")
            appendLine("🛠️ *Work Done:* ${booking.serviceName} (${booking.modelType})")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("💵 *Service / Labor Charge:* ₹$serviceCharge")
            if (spareParts > 0) {
                appendLine("🔩 *Genuine Spare Parts:* ₹$spareParts")
            }
            if (discount > 0) {
                appendLine("🎉 *Special Discount:* -₹$discount")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("💰 *TOTAL PAYABLE:* ₹$total")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("✅ *Warranty:* 30 Days Cooling Guarantee on Service & 100% Genuine Parts")
            appendLine("⭐ *Review us on Google Maps:* https://maps.app.goo.gl/2x3fx1iMLMrsg8am7")
            appendLine("🙏 *Balaji Air Conditioners - Cool Comfort, Always!*")
        }
    }

    fun launchWhatsApp(context: Context, phoneNumber: String, messageText: String) {
        try {
            val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
            val encodedMsg = URLEncoder.encode(messageText, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to generic view
            try {
                val encodedMsg = URLEncoder.encode(messageText, "UTF-8")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber?text=$encodedMsg")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun launchDialer(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun launchMaps(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BUSINESS_MAPS_URL)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
