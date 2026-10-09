package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        ServiceItem::class,
        Booking::class,
        PromoOffer::class,
        GalleryItem::class,
        ReviewItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BalajiDatabase : RoomDatabase() {
    abstract fun balajiDao(): BalajiDao

    companion object {
        @Volatile
        private var INSTANCE: BalajiDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BalajiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BalajiDatabase::class.java,
                    "balaji_air_conditioners.db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.balajiDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: BalajiDao) {
            // Seed Services
            val defaultServices = listOf(
                ServiceItem(
                    id = "srv_1",
                    name = "Jet Pump Deep Chemical Service",
                    description = "High-pressure washer jet cleaning of cooling coils & condenser, antibacterial chemical foam wash, blower & drain tray decontamination with indoor unit protection bag.",
                    estimatedTime = "45 - 60 Min",
                    startingPrice = 499,
                    iconType = "JET_PUMP",
                    isActive = true
                ),
                ServiceItem(
                    id = "srv_2",
                    name = "AC Repair & Cooling Diagnostic",
                    description = "Complete diagnostic check: fan motor, capacitor testing, sensor calibration, water leakage fix, airflow blockage & noise reduction.",
                    estimatedTime = "30 - 45 Min",
                    startingPrice = 299,
                    iconType = "AC_SERVICE",
                    isActive = true
                ),
                ServiceItem(
                    id = "srv_3",
                    name = "100% Pure Gas Refill (R32 / R410A / R22)",
                    description = "Nitrogen high-pressure leak testing, electronic vacuuming with gauge manifold, pure OEM refrigerant charging with digital weighing scale.",
                    estimatedTime = "45 Min",
                    startingPrice = 1499,
                    iconType = "GAS_CHARGING",
                    isActive = true
                ),
                ServiceItem(
                    id = "srv_4",
                    name = "Split AC Precision Installation / Uninstallation",
                    description = "Heavy duty core drilling, copper pipe flaring with nitrogen seal, anti-vibration rubber pads, vacuuming and spirit-level alignment.",
                    estimatedTime = "60 - 90 Min",
                    startingPrice = 899,
                    iconType = "INSTALLATION",
                    isActive = true
                ),
                ServiceItem(
                    id = "srv_5",
                    name = "Fridge Repair & Maintenance",
                    description = "Single Door, Double Door, Inverter Refrigerator & Deep Freezer repair. Gas refill, thermostat replacement, defrost timer fix & rubber gasket seal.",
                    estimatedTime = "40 - 60 Min",
                    startingPrice = 349,
                    iconType = "FRIDGE",
                    isActive = true
                ),
                ServiceItem(
                    id = "srv_6",
                    name = "Inverter AC PCB & Compressor Repair",
                    description = "Advanced motherboard circuit repair, IPM & microcontroller troubleshooting, compressor overload protector replacement.",
                    estimatedTime = "60 Min",
                    startingPrice = 799,
                    iconType = "PCB_REPAIR",
                    isActive = true
                )
            )
            dao.insertServices(defaultServices)

            // Seed Promo Offers
            val defaultOffers = listOf(
                PromoOffer(
                    code = "BALAJI100",
                    title = "Flat ₹100 Off on First Service",
                    discountPercent = 10,
                    maxDiscount = 100,
                    description = "Applicable on any Jet Pump Service or Fridge repair.",
                    isActive = true
                ),
                PromoOffer(
                    code = "SUMMERCOOL20",
                    title = "20% Discount on Chemical Jet Wash",
                    discountPercent = 20,
                    maxDiscount = 250,
                    description = "High-pressure jet pump deep chemical service with 30-day cooling guarantee.",
                    isActive = true
                ),
                PromoOffer(
                    code = "GASREFILL50",
                    title = "₹150 Off Pure Gas Charging",
                    discountPercent = 15,
                    maxDiscount = 150,
                    description = "Pure OEM R32 / R410A / R22 gas refill with nitrogen leak testing.",
                    isActive = true
                )
            )
            dao.insertOffers(defaultOffers)

            // Seed Gallery
            val defaultGallery = listOf(
                GalleryItem(
                    id = "gal_1",
                    title = "Jet Pump Chemical Foam Cleaning",
                    category = "AC Jet Wash",
                    description = "High-pressure washer clearing choked cooling fins with water jacket protection.",
                    tag = "Before & After"
                ),
                GalleryItem(
                    id = "gal_2",
                    title = "Double Door Fridge Gas Refill",
                    category = "Fridge Repair",
                    description = "Samsung Frost Free refrigerator capillary tube cleaning & gas charging.",
                    tag = "Completed"
                ),
                GalleryItem(
                    id = "gal_3",
                    title = "Split AC Precision Copper Piping",
                    category = "Installation",
                    description = "Daikin Inverter AC outdoor stand mounting & vacuum leak test.",
                    tag = "Precision"
                ),
                GalleryItem(
                    id = "gal_4",
                    title = "Inverter AC PCB Micro Repair",
                    category = "PCB & Compressor",
                    description = "Voltas Inverter AC mainboard IPM chip repair & sensor replacement.",
                    tag = "Specialized"
                ),
                GalleryItem(
                    id = "gal_5",
                    title = "Deep Freezer Thermostat Calibration",
                    category = "Fridge Repair",
                    description = "Commercial Deep Freezer cooling restoration at -18°C.",
                    tag = "Verified"
                ),
                GalleryItem(
                    id = "gal_6",
                    title = "R32 Pure Refrigerant Charging",
                    category = "Gas Charging",
                    description = "Digital weighing scale OEM gas charging with vacuum gauge test.",
                    tag = "100% Genuine"
                )
            )
            dao.insertGalleryItems(defaultGallery)

            // Seed Reviews with nested Owner Ka Jawab
            val defaultReviews = listOf(
                ReviewItem(
                    id = "rev_1",
                    customerName = "Rajesh Sharma",
                    rating = 5.0f,
                    dateText = "2 Din Pehle",
                    comment = "Sanjay bhai ne 45 minute me AC ka jet pump chemical service kar diya. Daikin AC ekdum naye jaisa chilled hawa fenk raha hai! Bohot polite aur professional service.",
                    serviceUsed = "Jet Pump Deep Chemical Service",
                    ownerReply = "Dhanyawad Rajesh ji! Daikin AC ki cooling 100% restore ho gayi. Kisi bhi query ke liye aap direct mujhe call kar sakte hain. 30 din ki cooling guarantee active hai!",
                    ownerReplyBy = "Sanjay Prajapati (Owner)"
                ),
                ReviewItem(
                    id = "rev_2",
                    customerName = "Amit Patel",
                    rating = 5.0f,
                    dateText = "1 Week Pehle",
                    comment = "Fridge me bilkul cooling nahi ho rahi thi, Sandip bhai ne gas refill kiya aur thermostat calibrate kar diya. Same day repair ho gaya, reasonable price!",
                    serviceUsed = "Fridge Repair & Maintenance",
                    ownerReply = "Aapka bahut aabhar Amit bhai! Hum hamesha genuine parts aur best cooling quality dene ki koshish karte hain.",
                    ownerReplyBy = "Sandip Prajapati (Owner)"
                ),
                ReviewItem(
                    id = "rev_3",
                    customerName = "Pooja Verma",
                    rating = 5.0f,
                    dateText = "2 Weeks Pehle",
                    comment = "Super fast service! Form submit karne ke 15 minute ke andar Sanjay ji ka call aa gaya aur afternoon me technician ne aakar gas leak fix kar diya.",
                    serviceUsed = "AC Gas Refill (R32)",
                    ownerReply = "Thank you Pooja ji for trusting Balaji Air Conditioners! 'Cool Comfort, Always' hamara promise hai.",
                    ownerReplyBy = "Sanjay Prajapati (Owner)"
                )
            )
            dao.insertReviews(defaultReviews)

            // Seed Admin User
            val adminUser = User(
                phone = "9157896306",
                name = "Sanjay Prajapati",
                address = "Balaji Air Conditioners Workshop",
                area = "Main Road",
                role = "ADMIN"
            )
            dao.insertUser(adminUser)
        }
    }
}
