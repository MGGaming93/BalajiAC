package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BalajiDao {

    // Services
    @Query("SELECT * FROM services")
    fun getAllServices(): Flow<List<ServiceItem>>

    @Query("SELECT * FROM services WHERE isActive = 1")
    fun getActiveServices(): Flow<List<ServiceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceItem)

    @Update
    suspend fun updateService(service: ServiceItem)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: String)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE customerPhone = :phone ORDER BY createdAt DESC")
    fun getBookingsForPhone(phone: String): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: String, status: String)

    @Query("UPDATE bookings SET billAmount = :bill, sparePartsCharge = :spare, discountAmount = :discount, status = 'COMPLETED' WHERE id = :id")
    suspend fun completeBookingWithBill(id: String, bill: Int, spare: Int, discount: Int)

    // Users
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM users WHERE phone = :phone")
    suspend fun deleteUser(phone: String)

    // Promo Offers
    @Query("SELECT * FROM promo_offers")
    fun getAllOffers(): Flow<List<PromoOffer>>

    @Query("SELECT * FROM promo_offers WHERE isActive = 1")
    fun getActiveOffers(): Flow<List<PromoOffer>>

    @Query("SELECT * FROM promo_offers WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getOfferByCode(code: String): PromoOffer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: PromoOffer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<PromoOffer>)

    @Query("DELETE FROM promo_offers WHERE code = :code")
    suspend fun deleteOffer(code: String)

    // Reviews
    @Query("SELECT * FROM reviews")
    fun getAllReviews(): Flow<List<ReviewItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewItem)

    @Query("UPDATE reviews SET ownerReply = :reply, ownerReplyBy = :replyBy WHERE id = :id")
    suspend fun addOwnerReply(id: String, reply: String, replyBy: String)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReview(id: String)

    // Gallery
    @Query("SELECT * FROM gallery_items")
    fun getAllGalleryItems(): Flow<List<GalleryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItems(items: List<GalleryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItem(item: GalleryItem)

    @Query("DELETE FROM gallery_items WHERE id = :id")
    suspend fun deleteGalleryItem(id: String)
}
