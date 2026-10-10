package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PromoOffer
import com.example.data.repository.BalajiRepository
import com.example.ui.components.ElevatedWhiteCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    repository: BalajiRepository,
    onBack: () -> Unit,
    onNavigateToMyBookings: () -> Unit,
    onLogout: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentPhone by repository.userPreferences.currentUserPhone.collectAsState()
    val currentName by repository.userPreferences.currentUserName.collectAsState()
    val currentAddress by repository.userPreferences.currentUserAddress.collectAsState()
    val currentArea by repository.userPreferences.currentUserArea.collectAsState()

    val activeOffers by repository.activeOffers.collectAsState(initial = emptyList())

    // Bottom Sheets & Dialogs
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showHelpSupportSheet by remember { mutableStateOf(false) }
    var showOffersSheet by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Edit Profile form fields
    var editName by remember { mutableStateOf(currentName ?: "") }
    var editAddress by remember { mutableStateOf(currentAddress ?: "") }
    var editArea by remember { mutableStateOf(currentArea ?: "") }
    var isSavingProfile by remember { mutableStateOf(false) }

    LaunchedEffect(currentName, currentAddress, currentArea) {
        editName = currentName ?: ""
        editAddress = currentAddress ?: ""
        editArea = currentArea ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = BalajiTextLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BalajiTextLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BalajiNavyDark
                )
            )
        },
        containerColor = BalajiNavyDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. USER INFORMATION CARD (Top Section)
            // ==========================================
            item {
                ElevatedWhiteCard(
                    modifier = Modifier.testTag("user_profile_info_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(BalajiNavyDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentName?.take(1)?.uppercase()).takeIf { !it.isNullOrBlank() } ?: "U",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                color = BalajiTealPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentName?.ifBlank { "Valued Customer" } ?: "Valued Customer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = BalajiTextDark
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = BalajiGuaranteeGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+91 ${currentPhone ?: "Not Registered"}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BalajiTealDeep
                                )
                            }

                            if (!currentAddress.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🏠 $currentAddress" + if (!currentArea.isNullOrBlank()) ", $currentArea" else "",
                                    fontSize = 12.sp,
                                    color = BalajiTextMuted,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Edit Profile / Manage Address Action Button
                    Button(
                        onClick = { showEditProfileSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("edit_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = BalajiCardWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Profile & Address",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BalajiCardWhite
                        )
                    }
                }
            }

            // ==========================================
            // 3. PROFILE MENU OPTIONS (Cards/Tiles)
            // ==========================================
            item {
                ElevatedWhiteCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ProfileMenuTile(
                            icon = Icons.Default.CalendarMonth,
                            iconTint = BalajiTealDeep,
                            title = "My Bookings",
                            subtitle = "View past & active service appointments",
                            onClick = onNavigateToMyBookings,
                            tag = "menu_my_bookings"
                        )

                        HorizontalDivider(color = BalajiBorder, modifier = Modifier.padding(vertical = 4.dp))

                        ProfileMenuTile(
                            icon = Icons.Default.CardGiftcard,
                            iconTint = BalajiOrangeFlame,
                            title = "Active Offers & Coupons",
                            subtitle = "Discounts on Jet Pump service & gas charging",
                            onClick = { showOffersSheet = true },
                            tag = "menu_active_offers"
                        )

                        HorizontalDivider(color = BalajiBorder, modifier = Modifier.padding(vertical = 4.dp))

                        ProfileMenuTile(
                            icon = Icons.Default.SupportAgent,
                            iconTint = BalajiGuaranteeGreen,
                            title = "Help & Support",
                            subtitle = "Call or WhatsApp Balaji Air Conditioners",
                            onClick = { showHelpSupportSheet = true },
                            tag = "menu_help_support"
                        )

                        HorizontalDivider(color = BalajiBorder, modifier = Modifier.padding(vertical = 4.dp))

                        ProfileMenuTile(
                            icon = Icons.Default.Policy,
                            iconTint = BalajiTextMuted,
                            title = "Terms & Conditions / Privacy",
                            subtitle = "30-Day Cooling Guarantee & Privacy Policy",
                            onClick = { showTermsDialog = true },
                            tag = "menu_terms_policy"
                        )
                    }
                }
            }

            // ==========================================
            // 4. LOGOUT BUTTON (Bottom Section)
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showLogoutConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("logout_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BalajiEmergencyRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = BalajiEmergencyRed
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = BalajiEmergencyRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = BalajiEmergencyRed
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Balaji Air Conditioners • Cool Comfort, Always",
                    color = BalajiTextSubtle,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // ==========================================
    // BOTTOM SHEET: EDIT PROFILE / MANAGE ADDRESS
    // ==========================================
    if (showEditProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditProfileSheet = false },
            containerColor = BalajiCardWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag("edit_profile_sheet")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile & Address",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BalajiTextDark
                    )
                    IconButton(onClick = { showEditProfileSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BalajiTextDark)
                    }
                }

                Text(
                    text = "Changes will sync immediately to your Google Sheets profile.",
                    fontSize = 12.sp,
                    color = BalajiTextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Customer Name *") },
                    placeholder = { Text("Enter your full name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BalajiTextDark,
                        unfocusedTextColor = BalajiTextDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editAddress,
                    onValueChange = { editAddress = it },
                    label = { Text("Complete Address (House / Flat / Street) *") },
                    placeholder = { Text("Enter street address") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BalajiTextDark,
                        unfocusedTextColor = BalajiTextDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editArea,
                    onValueChange = { editArea = it },
                    label = { Text("Area / Sector / Landmark") },
                    placeholder = { Text("e.g. Navrangpura, Satellite") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BalajiTextDark,
                        unfocusedTextColor = BalajiTextDark
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (editName.isBlank()) {
                            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        } else {
                            isSavingProfile = true
                            coroutineScope.launch {
                                val phone = currentPhone ?: "9157896306"
                                repository.registerOrUpdateUser(
                                    phone = phone,
                                    name = editName.trim(),
                                    address = editAddress.trim(),
                                    area = editArea.trim()
                                )
                                isSavingProfile = false
                                showEditProfileSheet = false
                                Toast.makeText(context, "Profile updated & synced to Google Sheets!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_profile_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                    enabled = !isSavingProfile
                ) {
                    if (isSavingProfile) {
                        CircularProgressIndicator(color = BalajiCardWhite, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Save & Sync to Google Sheets", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ==========================================
    // BOTTOM SHEET: HELP & SUPPORT (+91 9157896306)
    // ==========================================
    if (showHelpSupportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSupportSheet = false },
            containerColor = BalajiCardWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag("help_support_sheet")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Help & Support",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BalajiTextDark
                    )
                    IconButton(onClick = { showHelpSupportSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BalajiTextDark)
                    }
                }

                Text(
                    text = "Contact Balaji Air Conditioners directly for AC and Fridge repair, emergency gas leak, or service inquiries.",
                    fontSize = 13.sp,
                    color = BalajiTextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Button 1: "Call Us"
                Button(
                    onClick = {
                        repository.launchDialer(context, BalajiRepository.BUSINESS_PHONE)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("support_call_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call Us", tint = BalajiCardWhite)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Call Us (+91 9157896306)", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Button 2: "WhatsApp Us"
                Button(
                    onClick = {
                        val msg = "Namaste, mujhe Balaji Air Conditioners customer support se baat karni hai."
                        repository.launchWhatsApp(context, BalajiRepository.BUSINESS_PHONE, msg)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("support_whatsapp_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp Us", tint = BalajiCardWhite)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("WhatsApp Us (+91 9157896306)", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Working Hours: 8:00 AM - 9:00 PM (Monday to Sunday)",
                    fontSize = 11.sp,
                    color = BalajiTextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // ==========================================
    // BOTTOM SHEET: ACTIVE OFFERS
    // ==========================================
    if (showOffersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOffersSheet = false },
            containerColor = BalajiCardWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Offers & Coupons",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BalajiTextDark
                    )
                    IconButton(onClick = { showOffersSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = BalajiTextDark)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeOffers.isEmpty()) {
                    Text("No active coupons at this moment.", color = BalajiTextMuted)
                } else {
                    activeOffers.forEach { offer ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = offer.code,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = BalajiOrangeFlame
                                    )
                                    Text(
                                        text = offer.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BalajiTextDark
                                    )
                                    Text(
                                        text = "${offer.discountPercent}% Off (Max ₹${offer.maxDiscount}) • ${offer.description}",
                                        fontSize = 11.sp,
                                        color = BalajiTextMuted
                                    )
                                }

                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("PromoCode", offer.code))
                                        Toast.makeText(context, "Code ${offer.code} copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Copy", fontSize = 11.sp, color = BalajiCardWhite)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ==========================================
    // DIALOG: TERMS & CONDITIONS / PRIVACY
    // ==========================================
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text("Terms & 30-Day Cooling Guarantee", fontWeight = FontWeight.Bold, color = BalajiTextDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. 30-Day Cooling Guarantee:\nAny AC or Refrigerator service serviced with Jet Pump Deep Chemical Wash or Gas Refill carries a full 30-day performance warranty. In case of cooling issues, re-inspection is free.",
                        fontSize = 12.sp,
                        color = BalajiTextDark
                    )
                    Text(
                        text = "2. 100% Genuine Spare Parts:\nOnly OEM and high-grade copper spares and pure refrigerants (R32 / R410A / R22) are used.",
                        fontSize = 12.sp,
                        color = BalajiTextDark
                    )
                    Text(
                        text = "3. Privacy Policy:\nYour contact number and location are stored securely in Google Sheets for service fulfillment only and never shared with third parties.",
                        fontSize = 12.sp,
                        color = BalajiTextDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                ) {
                    Text("Understood", color = BalajiCardWhite)
                }
            },
            containerColor = BalajiCardWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ==========================================
    // DIALOG: LOGOUT CONFIRMATION
    // ==========================================
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text("Confirm Logout", fontWeight = FontWeight.Bold, color = BalajiTextDark)
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of Balaji Air Conditioners?",
                    color = BalajiTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        repository.userPreferences.clearSession()
                        try {
                            repository.firebaseManager?.auth?.signOut()
                        } catch (_: Exception) {}
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiEmergencyRed)
                ) {
                    Text("Logout", color = BalajiCardWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = BalajiTextDark)
                }
            },
            containerColor = BalajiCardWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ProfileMenuTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = BalajiTextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = BalajiTextMuted
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = BalajiTextSubtle,
            modifier = Modifier.size(14.dp)
        )
    }
}
