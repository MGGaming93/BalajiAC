package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.BalajiRepository
import com.example.ui.components.BalajiLogo
import com.example.ui.components.ElevatedWhiteCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: BalajiRepository,
    onNavigateToMyBookings: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Auth state from preferences
    val isLoggedIn by repository.userPreferences.isLoggedIn.collectAsState()
    val currentPhone by repository.userPreferences.currentUserPhone.collectAsState()
    val currentName by repository.userPreferences.currentUserName.collectAsState()
    val currentAddress by repository.userPreferences.currentUserAddress.collectAsState()
    val currentArea by repository.userPreferences.currentUserArea.collectAsState()

    // Dialogs state
    var showAuthDialog by remember { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var showBookingSuccessDialog by remember { mutableStateOf<Booking?>(null) }
    var showAddReviewDialog by remember { mutableStateOf(false) }

    // Dynamic data from Room database
    val activeServices by repository.activeServices.collectAsState(initial = emptyList())
    val activeOffers by repository.activeOffers.collectAsState(initial = emptyList())
    val galleryItems by repository.allGalleryItems.collectAsState(initial = emptyList())
    val reviews by repository.allReviews.collectAsState(initial = emptyList())

    // Booking form state
    var selectedServiceName by remember { mutableStateOf("") }
    var unitsCount by remember { mutableIntStateOf(1) }
    var selectedModelType by remember { mutableStateOf("Split AC") }
    var bookingName by remember { mutableStateOf("") }
    var bookingPhone by remember { mutableStateOf("") }
    var bookingAddress by remember { mutableStateOf("") }
    var bookingArea by remember { mutableStateOf("") }
    var issueNotes by remember { mutableStateOf("") }
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<PromoOffer?>(null) }
    var couponError by remember { mutableStateOf<String?>(null) }
    var isBookingSubmitting by remember { mutableStateOf(false) }
    var bookingFormYPosition by remember { mutableIntStateOf(0) }

    // Pre-fill booking fields when logged in
    LaunchedEffect(isLoggedIn, currentName, currentPhone, currentAddress, currentArea) {
        if (isLoggedIn) {
            bookingName = currentName ?: ""
            bookingPhone = currentPhone ?: ""
            bookingAddress = currentAddress ?: ""
            bookingArea = currentArea ?: ""
        }
    }

    // Set initial service selection once services load
    LaunchedEffect(activeServices) {
        if (selectedServiceName.isEmpty() && activeServices.isNotEmpty()) {
            selectedServiceName = activeServices.first().name
        }
    }

    // Gallery category filter
    var selectedGalleryCategory by remember { mutableStateOf("All") }
    val galleryCategories = listOf("All", "AC Jet Wash", "Installation", "Fridge Repair", "Gas Charging", "PCB & Compressor")

    // Filtered gallery items
    val filteredGallery = remember(selectedGalleryCategory, galleryItems) {
        if (selectedGalleryCategory == "All") galleryItems
        else galleryItems.filter { it.category == selectedGalleryCategory }
    }

    Scaffold(
        containerColor = BalajiNavyDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // ==========================================
            // 1. TOP HEADER (Logo with 5s Admin hold, Phone, Auth)
            // ==========================================
            Surface(
                color = BalajiNavySurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo with EXACT 5-SECOND LONG PRESS TRIGGER FOR ADMIN PIN!
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            BalajiLogo(
                                size = 58.dp,
                                showSubtext = false,
                                enableAdminLongPress = true,
                                onAdminTrigger = {
                                    showAdminPinDialog = true
                                }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "BALAJI",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = BalajiCardWhite,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "AIR CONDITIONERS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = BalajiTealPrimary,
                                    letterSpacing = 0.3.sp
                                )
                                Text(
                                    text = "AC & Fridge Service",
                                    fontSize = 10.sp,
                                    color = BalajiTextSubtle
                                )
                            }
                        }

                        // Right header: Emergency dial button + Auth actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Emergency Call Button (+91 9157896306)
                            IconButton(
                                onClick = {
                                    repository.launchDialer(context, BalajiRepository.BUSINESS_PHONE)
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BalajiOrangeFlame)
                                    .testTag("emergency_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call Emergency +91 9157896306",
                                    tint = BalajiCardWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Conditional UI: Guest vs Authenticated
                            if (!isLoggedIn) {
                                Button(
                                    onClick = { showAuthDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BalajiTealPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("header_login_button")
                                ) {
                                    Text(
                                        text = "Login / Sign Up",
                                        color = BalajiNavyDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                // User Logged In: My Bookings & Profile button
                                Button(
                                    onClick = onNavigateToMyBookings,
                                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyElevated),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("header_my_bookings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventNote,
                                        contentDescription = "My Bookings",
                                        tint = BalajiTealPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Bookings",
                                        color = BalajiCardWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = onNavigateToProfile,
                                    colors = ButtonDefaults.buttonColors(containerColor = BalajiTealPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("header_profile_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = BalajiNavyDark,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Profile",
                                        color = BalajiNavyDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Logged in greeting banner
                    if (isLoggedIn && !currentName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Namaste, $currentName! (+91 $currentPhone)",
                            fontSize = 12.sp,
                            color = BalajiTealSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ==========================================
            // 2. PROMOTIONAL BANNER (Special Discount)
            // ==========================================
            if (activeOffers.isNotEmpty()) {
                val bannerOffer = activeOffers.first()
                Surface(
                    color = BalajiOrangeFlame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("promotional_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "🎉",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = bannerOffer.title,
                                    color = BalajiCardWhite,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Code: ${bannerOffer.code} • ${bannerOffer.description}",
                                    color = Color(0xFFFFF1E6),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            color = BalajiCardWhite,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                couponInput = bannerOffer.code
                                appliedCoupon = bannerOffer
                                Toast.makeText(context, "Coupon ${bannerOffer.code} Applied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "USE CODE",
                                color = BalajiOrangeFlame,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 3. HERO SECTION
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("hero_section")
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BalajiNavySurface,
                    border = BorderStroke(1.dp, BalajiNavyElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Surface(
                            color = BalajiNavyElevated,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ 100% Guaranteed Cooling Service",
                                    color = BalajiTealPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Thandak Ka Bharosa\nBalaji Air Conditioners",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BalajiCardWhite,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Complete AC & Fridge service, high-pressure jet pump deep chemical service, gas refill aur precision installation. 100% Genuine Parts aur 30 Din Cooling Guarantee ke sath!",
                            fontSize = 13.sp,
                            color = BalajiTextLight,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trust Badges Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TrustBadge(
                                icon = Icons.Default.Verified,
                                label = "30-Din Guarantee",
                                modifier = Modifier.weight(1f)
                            )
                            TrustBadge(
                                icon = Icons.Default.Build,
                                label = "Genuine Spares",
                                modifier = Modifier.weight(1f)
                            )
                            TrustBadge(
                                icon = Icons.Default.Engineering,
                                label = "Expert Technicians",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Quick Call action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    repository.launchDialer(context, BalajiRepository.BUSINESS_PHONE)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BalajiTealPrimary)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = BalajiNavyDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call +91 9157896306", color = BalajiNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val msg = "Namaste, mujhe Balaji Air Conditioners service ke baare me inquiry karni hai."
                                    repository.launchWhatsApp(context, BalajiRepository.BUSINESS_PHONE, msg)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = BalajiCardWhite, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp Karein", color = BalajiCardWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 4. DYNAMIC SERVICES LIST (White Elevated Cards)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("services_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hamari Services",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = BalajiCardWhite
                        )
                        Text(
                            text = "AC & Fridge Expert Solutions",
                            fontSize = 12.sp,
                            color = BalajiTealSecondary
                        )
                    }

                    Surface(
                        color = BalajiNavyElevated,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${activeServices.size} Active Services",
                            color = BalajiTealPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Render dynamic services as Elevated White Cards
                activeServices.forEach { service ->
                    ElevatedWhiteCard(
                        modifier = Modifier
                            .padding(bottom = 14.dp)
                            .testTag("service_card_${service.id}"),
                        onClick = {
                            selectedServiceName = service.name
                            if (!isLoggedIn) {
                                showAuthDialog = true
                            } else {
                                coroutineScope.launch {
                                    val targetY = (bookingFormYPosition - 80).coerceAtLeast(0)
                                    scrollState.animateScrollTo(targetY)
                                }
                                Toast.makeText(context, "${service.name} selected! Form par scroll ho gaya.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BalajiTextDark
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        color = Color(0xFFE0F2FE),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = "Time",
                                                tint = BalajiTealDeep,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = service.estimatedTime,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BalajiTealDeep
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Pay After Service",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Technician Notes: ${service.description}",
                                    fontSize = 12.sp,
                                    color = BalajiTextMuted,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    selectedServiceName = service.name
                                    if (!isLoggedIn) {
                                        showAuthDialog = true
                                    } else {
                                        coroutineScope.launch {
                                            val targetY = (bookingFormYPosition - 80).coerceAtLeast(0)
                                            scrollState.animateScrollTo(targetY)
                                        }
                                        Toast.makeText(context, "${service.name} selected! Form par scroll ho gaya.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Book Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 5. BOOKING FORM ("Ghar Baithe Service Book Karein")
            // CONDITIONAL: Only if User IS logged in!
            // If Guest: Display prompt "Please Login or Sign Up to book an appointment"
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { coordinates ->
                        bookingFormYPosition = coordinates.positionInParent().y.toInt()
                    }
                    .testTag("booking_section")
            ) {
                if (!isLoggedIn) {
                    // GUEST USER PROMPT
                    ElevatedWhiteCard(
                        modifier = Modifier.testTag("guest_login_prompt_card")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockPerson,
                                    contentDescription = "Login Required",
                                    tint = BalajiNavyDark,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Ghar Baithe Service Book Karein",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BalajiTextDark
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Please Login or Sign Up to book an appointment",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BalajiOrangeFlame,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Ek secure SMS OTP se login karein. Koi password yaad rakhne ki zaroorat nahi hai.",
                                fontSize = 12.sp,
                                color = BalajiTextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { showAuthDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("prompt_login_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = "Login", tint = BalajiCardWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Login / Sign Up Karein", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                            }
                        }
                    }
                } else {
                    // AUTHENTICATED USER: FULL BOOKING FORM
                    ElevatedWhiteCard(
                        modifier = Modifier.testTag("booking_form_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BalajiNavyDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HomeRepairService,
                                    contentDescription = "Booking",
                                    tint = BalajiTealPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ghar Baithe Service Book Karein",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BalajiTextDark
                                )
                                Text(
                                    text = "Pay After Service • 30 Din Cooling Guarantee",
                                    fontSize = 11.sp,
                                    color = BalajiTealDeep,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 1. Select Service (Dropdown from active services)
                        var serviceDropdownExpanded by remember { mutableStateOf(false) }
                        Text(
                            text = "Select Service *",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BalajiTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedServiceName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { serviceDropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { serviceDropdownExpanded = true }
                                    .testTag("booking_service_dropdown"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BalajiTextDark,
                                    unfocusedTextColor = BalajiTextDark,
                                    unfocusedBorderColor = BalajiBorder
                                )
                            )
                            DropdownMenu(
                                expanded = serviceDropdownExpanded,
                                onDismissRequest = { serviceDropdownExpanded = false },
                                modifier = Modifier.background(BalajiCardWhite)
                            ) {
                                activeServices.forEach { srv ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(srv.name, fontWeight = FontWeight.Bold, color = BalajiTextDark)
                                                Text("${srv.estimatedTime} • Pay After Service", fontSize = 11.sp, color = BalajiTextMuted)
                                            }
                                        },
                                        onClick = {
                                            selectedServiceName = srv.name
                                            serviceDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Units & Model/Type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Number of Units Counter
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Number of Units *",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = BalajiTextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .border(1.dp, BalajiBorder, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (unitsCount > 1) unitsCount-- },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Minus", tint = BalajiTextDark)
                                    }
                                    Text(
                                        text = "$unitsCount",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = BalajiTextDark
                                    )
                                    IconButton(
                                        onClick = { if (unitsCount < 10) unitsCount++ },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Plus", tint = BalajiTextDark)
                                    }
                                }
                            }

                            // Model / Type Dropdown
                            var modelDropdownExpanded by remember { mutableStateOf(false) }
                            val modelTypes = listOf(
                                "Split AC", "Window AC", "Inverter AC",
                                "Single Door Fridge", "Double Door Fridge", "Deep Freezer"
                            )
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = "Model / Type *",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = BalajiTextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box {
                                    OutlinedTextField(
                                        value = selectedModelType,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = {
                                            IconButton(onClick = { modelDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { modelDropdownExpanded = true }
                                            .testTag("booking_model_dropdown"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = BalajiTextDark,
                                            unfocusedTextColor = BalajiTextDark,
                                            unfocusedBorderColor = BalajiBorder
                                        )
                                    )
                                    DropdownMenu(
                                        expanded = modelDropdownExpanded,
                                        onDismissRequest = { modelDropdownExpanded = false },
                                        modifier = Modifier.background(BalajiCardWhite)
                                    ) {
                                        modelTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type, color = BalajiTextDark, fontWeight = FontWeight.Medium) },
                                                onClick = {
                                                    selectedModelType = type
                                                    modelDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. Customer Name & Mobile Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Customer Name *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BalajiTextDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = bookingName,
                                    onValueChange = { bookingName = it },
                                    placeholder = { Text("Name", color = BalajiTextMuted) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = BalajiTextDark,
                                        unfocusedTextColor = BalajiTextDark,
                                        unfocusedBorderColor = BalajiBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("booking_name_input")
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mobile Number *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BalajiTextDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = bookingPhone,
                                    onValueChange = { bookingPhone = it },
                                    placeholder = { Text("10 Digits", color = BalajiTextMuted) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = BalajiTextDark,
                                        unfocusedTextColor = BalajiTextDark,
                                        unfocusedBorderColor = BalajiBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("booking_phone_input")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Address & Area
                        Text("Address (House / Flat, Society, Landmark) *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BalajiTextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = bookingAddress,
                            onValueChange = { bookingAddress = it },
                            placeholder = { Text("Enter complete service address", color = BalajiTextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BalajiTextDark,
                                unfocusedTextColor = BalajiTextDark,
                                unfocusedBorderColor = BalajiBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("booking_address_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Area / Sector / City *", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BalajiTextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = bookingArea,
                            onValueChange = { bookingArea = it },
                            placeholder = { Text("e.g. Navrangpura, Satellite, Gandhinagar", color = BalajiTextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BalajiTextDark,
                                unfocusedTextColor = BalajiTextDark,
                                unfocusedBorderColor = BalajiBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("booking_area_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 5. Issue Notes
                        Text("Issue Notes (Kya problem aa rahi hai?)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BalajiTextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = issueNotes,
                            onValueChange = { issueNotes = it },
                            placeholder = { Text("e.g. Chilled hawa nahi aa rahi, Water leak ho raha hai, Gas khatam lag rahi hai", color = BalajiTextMuted) },
                            maxLines = 3,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BalajiTextDark,
                                unfocusedTextColor = BalajiTextDark,
                                unfocusedBorderColor = BalajiBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("booking_issue_notes_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 6. Promo Code with Apply Button
                        Text("Coupon Code", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BalajiTextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = {
                                    couponInput = it.uppercase()
                                    couponError = null
                                },
                                placeholder = { Text("Enter Coupon Code", color = BalajiTextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BalajiTextDark,
                                    unfocusedTextColor = BalajiTextDark,
                                    unfocusedBorderColor = BalajiBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("booking_coupon_input")
                            )

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val offer = repository.getOfferByCode(couponInput)
                                        if (offer != null) {
                                            appliedCoupon = offer
                                            couponError = null
                                            Toast.makeText(context, "🎉 ${offer.title} Applied!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            couponError = "Invalid or expired coupon"
                                            appliedCoupon = null
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                                modifier = Modifier.testTag("apply_coupon_button")
                            ) {
                                Text("Apply", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                            }
                        }

                        if (couponError != null) {
                            Text(
                                text = couponError ?: "",
                                color = BalajiEmergencyRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else if (appliedCoupon != null) {
                            Text(
                                text = "✅ Applied: ${appliedCoupon?.title} (Max ₹${appliedCoupon?.maxDiscount} off)",
                                color = BalajiGuaranteeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // NOTE: CRITICAL CONSTRAINT: NO DATE OR TIME PICKER FIELDS ALLOWED
                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (bookingName.isBlank() || bookingPhone.isBlank() || bookingAddress.isBlank()) {
                                    Toast.makeText(context, "Kripya Name, Phone aur Address enter karein", Toast.LENGTH_LONG).show()
                                } else {
                                    isBookingSubmitting = true
                                    coroutineScope.launch {
                                        val discount = appliedCoupon?.maxDiscount ?: 0
                                        val newBooking = repository.createBooking(
                                            customerName = bookingName.trim(),
                                            customerPhone = bookingPhone.trim(),
                                            address = bookingAddress.trim(),
                                            area = bookingArea.trim(),
                                            serviceName = selectedServiceName,
                                            units = unitsCount,
                                            modelType = selectedModelType,
                                            issueNotes = issueNotes.trim(),
                                            couponCode = appliedCoupon?.code ?: "",
                                            discountAmount = discount
                                        )

                                        // User stays on app! Booking is saved in local DB, Google Sheets and Firebase.
                                        isBookingSubmitting = false
                                        showBookingSuccessDialog = newBooking
                                        Toast.makeText(context, "🎉 Appointment Book Ho Gayi! ID: ${newBooking.id}", Toast.LENGTH_SHORT).show()

                                        // Reset fields
                                        issueNotes = ""
                                        appliedCoupon = null
                                        couponInput = ""
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_booking_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BalajiGuaranteeGreen),
                            enabled = !isBookingSubmitting
                        ) {
                            if (isBookingSubmitting) {
                                CircularProgressIndicator(color = BalajiCardWhite, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Confirm", tint = BalajiCardWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Appointment Confirm Karein (Pay After Service)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BalajiCardWhite
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 6. WORK PHOTOS GALLERY ("Hamara Kaam Dekhein")
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("gallery_section")
            ) {
                Text(
                    text = "Hamara Kaam Dekhein",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = BalajiCardWhite
                )
                Text(
                    text = "High-pressure jet pump wash & genuine AC/Fridge maintenance",
                    fontSize = 12.sp,
                    color = BalajiTealSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(galleryCategories) { cat ->
                        val isSelected = cat == selectedGalleryCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGalleryCategory = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BalajiTealPrimary,
                                selectedLabelColor = BalajiNavyDark,
                                containerColor = BalajiNavyElevated,
                                labelColor = BalajiTextLight
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gallery Cards
                filteredGallery.forEach { item ->
                    ElevatedWhiteCard(
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BalajiTextDark
                            )
                            Surface(
                                color = Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = item.tag,
                                    color = BalajiTealDeep,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Category: ${item.category}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BalajiOrangeFlame
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.description,
                            fontSize = 12.sp,
                            color = BalajiTextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 7. CUSTOMER REVIEWS & "Owner Ka Jawab"
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("reviews_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Customer Reviews",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = BalajiCardWhite
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐ 5.0 Stars (100% Genuine)", color = Color(0xFFFFB703), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { showAddReviewDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BalajiTealPrimary)
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = "Review", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Review Likhein", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                reviews.forEach { rev ->
                    ElevatedWhiteCard(
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rev.customerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = BalajiTextDark
                            )
                            Row {
                                repeat(5) {
                                    Text("⭐", fontSize = 11.sp)
                                }
                            }
                        }

                        Text(
                            text = "Service: ${rev.serviceUsed} • ${rev.dateText}",
                            fontSize = 11.sp,
                            color = BalajiTealDeep,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "\"${rev.comment}\"",
                            fontSize = 13.sp,
                            color = BalajiTextDark,
                            lineHeight = 17.sp
                        )

                        // NESTED "Owner Ka Jawab"
                        if (!rev.ownerReply.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "👨‍🔧 Response from Balaji Air Conditioners:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = BalajiNavyDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = rev.ownerReply,
                                        fontSize = 12.sp,
                                        color = BalajiTextDark,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 8. FOOTER
            // ==========================================
            Surface(
                color = BalajiNavySurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("footer_section")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BalajiLogo(
                        size = 80.dp,
                        showSubtext = false
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Balaji Air Conditioners",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = BalajiCardWhite
                    )

                    Text(
                        text = "Cool Comfort, Always • Certified AC & Fridge Services",
                        fontSize = 12.sp,
                        color = BalajiTealPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact rows
                    FooterInfoRow(
                        icon = Icons.Default.Phone,
                        title = "Helpline & WhatsApp",
                        value = "+91 9157896306",
                        onClick = { repository.launchDialer(context, BalajiRepository.BUSINESS_PHONE) }
                    )

                    FooterInfoRow(
                        icon = Icons.Default.Email,
                        title = "Email Support",
                        value = BalajiRepository.BUSINESS_EMAIL,
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:${BalajiRepository.BUSINESS_EMAIL}")
                            }
                            context.startActivity(intent)
                        }
                    )

                    FooterInfoRow(
                        icon = Icons.Default.LocationOn,
                        title = "Workshop Location",
                        value = "View on Google Maps",
                        onClick = { repository.launchMaps(context) }
                    )

                    FooterInfoRow(
                        icon = Icons.Default.AccessTime,
                        title = "Working Hours",
                        value = "8:00 AM - 9:00 PM (Everyday)",
                        onClick = null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "© Balaji Air Conditioners • Cool Comfort, Always",
                        fontSize = 11.sp,
                        color = BalajiTextSubtle
                    )
                }
            }
        }
    }

    // AUTH DIALOG
    if (showAuthDialog) {
        AuthDialog(
            repository = repository,
            onDismiss = { showAuthDialog = false },
            onAuthSuccess = { phone, name ->
                showAuthDialog = false
                Toast.makeText(context, "Welcome $name! Appointment form ready.", Toast.LENGTH_SHORT).show()
                coroutineScope.launch {
                    val targetY = (bookingFormYPosition - 80).coerceAtLeast(0)
                    scrollState.animateScrollTo(targetY)
                }
            }
        )
    }

    // ADMIN PIN DIALOG (PIN is strictly "1099")
    if (showAdminPinDialog) {
        AdminPinDialog(
            onDismiss = { showAdminPinDialog = false },
            onSuccess = {
                showAdminPinDialog = false
                onNavigateToAdmin()
            }
        )
    }

    // BOOKING SUCCESS DIALOG (Keeps user on the app with direct WhatsApp option)
    if (showBookingSuccessDialog != null) {
        val b = showBookingSuccessDialog!!
        AlertDialog(
            onDismissRequest = { showBookingSuccessDialog = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(BalajiGuaranteeGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = BalajiGuaranteeGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🎉 Appointment Book Ho Gayi!",
                        fontWeight = FontWeight.ExtraBold,
                        color = BalajiNavyDark,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = BalajiTealPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Booking ID: ${b.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BalajiNavyDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BalajiCardLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = BalajiTealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${b.serviceName} • ${b.units} Unit (${b.modelType})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BalajiNavyDark
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BalajiTealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${b.customerName} (${b.customerPhone})",
                                    fontSize = 12.sp,
                                    color = BalajiTextDark
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BalajiTealPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${b.address}, ${b.area}",
                                    fontSize = 12.sp,
                                    color = BalajiTextDark
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = BalajiGuaranteeGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pay After Service • Verified Technician • Balaji Air Conditioners",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BalajiGuaranteeGreen
                                )
                            }
                        }
                    }

                    // Status notification card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BalajiGuaranteeGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aapki appointment app me save ho chuki hai. Aap app par hi bane reh sakte hain ya direct WhatsApp par message bhej sakte hain.",
                                fontSize = 11.sp,
                                color = Color(0xFF1B5E20),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Direct WhatsApp Action Button
                    Button(
                        onClick = {
                            val whatsappMsg = repository.formatBookingWhatsAppText(b)
                            repository.launchWhatsApp(context, BalajiRepository.BUSINESS_PHONE, whatsappMsg)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("whatsapp_booking_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = BalajiCardWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp Par Details Bhejein (+91 9157896306)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BalajiCardWhite
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBookingSuccessDialog = null
                        onNavigateToMyBookings()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_my_bookings_dialog_btn")
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = BalajiCardWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("App Par Hi Rahe • View My Bookings", color = BalajiCardWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBookingSuccessDialog = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Theek Hai (App Par Bane Rahe)", color = BalajiTextMuted, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = BalajiCardWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ADD REVIEW DIALOG
    if (showAddReviewDialog) {
        var revName by remember { mutableStateOf(currentName ?: "") }
        var revComment by remember { mutableStateOf("") }
        var revService by remember { mutableStateOf(selectedServiceName.ifEmpty { "AC Jet Pump Service" }) }

        AlertDialog(
            onDismissRequest = { showAddReviewDialog = false },
            title = { Text("Apna Review Likhein", fontWeight = FontWeight.Bold, color = BalajiTextDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = revName,
                        onValueChange = { revName = it },
                        label = { Text("Aapka Naam") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BalajiTextDark, unfocusedTextColor = BalajiTextDark)
                    )
                    OutlinedTextField(
                        value = revComment,
                        onValueChange = { revComment = it },
                        label = { Text("Feedback / Experience") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = BalajiTextDark, unfocusedTextColor = BalajiTextDark)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (revName.isNotBlank() && revComment.isNotBlank()) {
                            coroutineScope.launch {
                                repository.addReview(
                                    ReviewItem(
                                        id = "rev_${System.currentTimeMillis()}",
                                        customerName = revName.trim(),
                                        rating = 5.0f,
                                        dateText = "Just now",
                                        comment = revComment.trim(),
                                        serviceUsed = revService,
                                        ownerReply = "Dhanyawad $revName ji! Balaji Air Conditioners par vishwas karne ke liye aabhar.",
                                        ownerReplyBy = "Balaji Air Conditioners"
                                    )
                                )
                                showAddReviewDialog = false
                                Toast.makeText(context, "Review post ho gaya!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                ) {
                    Text("Post Review", color = BalajiCardWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReviewDialog = false }) {
                    Text("Cancel", color = BalajiTextMuted)
                }
            },
            containerColor = BalajiCardWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun TrustBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BalajiNavyElevated,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = BalajiTealPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = BalajiTextLight,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun FooterInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = BalajiTealPrimary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 11.sp, color = BalajiTextSubtle)
            Text(text = value, fontSize = 13.sp, color = BalajiCardWhite, fontWeight = FontWeight.SemiBold)
        }
    }
}
