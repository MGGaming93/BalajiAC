package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.repository.BalajiRepository
import com.example.ui.components.ElevatedWhiteCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    repository: BalajiRepository,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val currentPhone = repository.userPreferences.currentUserPhone.collectAsState().value ?: ""
    val customerName = repository.userPreferences.currentUserName.collectAsState().value ?: "Customer"

    val bookings by repository.getBookingsForCustomer(currentPhone).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Bookings (Mera History)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BalajiTextLight
                        )
                        Text(
                            text = "+91 $currentPhone • $customerName",
                            fontSize = 12.sp,
                            color = BalajiTealSecondary
                        )
                    }
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
        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ElevatedWhiteCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No Bookings",
                            tint = BalajiTealDeep,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Abhi Koi Booking Nahi Hai",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BalajiTextDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aapne abhi tak koi AC ya Fridge service appointment book nahi ki hai.",
                            fontSize = 13.sp,
                            color = BalajiTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                        ) {
                            Text("Service Book Karein", color = BalajiCardWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(bookings, key = { it.id }) { booking ->
                    BookingItemCard(booking = booking, repository = repository)
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(
    booking: Booking,
    repository: BalajiRepository
) {
    val context = LocalContext.current

    val (statusColor, statusBg, statusLabel) = when (booking.status) {
        "COMPLETED" -> Triple(Color(0xFF065F46), Color(0xFFD1FAE5), "Service Completed ✅")
        "TECHNICIAN_ASSIGNED" -> Triple(Color(0xFF1E40AF), Color(0xFFDBEAFE), "Technician Assigned 👨‍🔧")
        "IN_PROGRESS" -> Triple(Color(0xFF9A3412), Color(0xFFFFEDD5), "Service In Progress ⏳")
        else -> Triple(Color(0xFFB45309), Color(0xFFFEF3C7), "Request Confirmed (Pending) 🕒")
    }

    ElevatedWhiteCard(
        modifier = Modifier.testTag("booking_item_${booking.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = booking.id,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = BalajiNavyDark
            )

            Surface(
                color = statusBg,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = booking.serviceName,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = BalajiTextDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Devices, contentDescription = "Type", tint = BalajiTealDeep, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${booking.units} Unit(s) • ${booking.modelType}",
                fontSize = 13.sp,
                color = BalajiTextDark,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.LocationOn, contentDescription = "Address", tint = BalajiEmergencyRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${booking.address}, ${booking.area}",
                fontSize = 12.sp,
                color = BalajiTextMuted
            )
        }

        if (booking.issueNotes.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Notes: \"${booking.issueNotes}\"",
                    fontSize = 12.sp,
                    color = BalajiTextDark,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        if (booking.couponCode.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "🏷️ Coupon Applied: ${booking.couponCode} (Saved ₹${booking.discountAmount})",
                fontSize = 12.sp,
                color = BalajiGuaranteeGreen,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (booking.billAmount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BalajiBorder)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Paid Bill:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BalajiTextDark
                )
                Text(
                    text = "₹${booking.billAmount}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = BalajiGuaranteeGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick WhatsApp / Call assistance button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    repository.launchDialer(context, BalajiRepository.BUSINESS_PHONE)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = BalajiNavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Support", fontSize = 12.sp, color = BalajiNavyDark, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val msg = "Namaste Sanjay bhai, meri booking ${booking.id} (${booking.serviceName}) ke baare me update chahiye."
                    repository.launchWhatsApp(context, BalajiRepository.BUSINESS_PHONE, msg)
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = BalajiCardWhite, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp", fontSize = 12.sp, color = BalajiCardWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}
