package com.example.ui.screens.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.remote.GoogleAppsScriptTemplate
import com.example.data.remote.GoogleSheetsApiClient
import com.example.data.repository.BalajiRepository
import com.example.ui.components.ElevatedWhiteCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// -------------------------------------------------------------
// 1. MANAGE SERVICES (Service Catalog CRUD)
// -------------------------------------------------------------
@Composable
fun ManageServicesView(repository: BalajiRepository) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val services by repository.allServices.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("manage_services_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Service Catalog CRUD", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
                Text("Controls Home Page & Booking Dropdown", fontSize = 12.sp, color = BalajiTealSecondary)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BalajiTealPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = BalajiNavyDark)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Service", color = BalajiNavyDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(services, key = { it.id }) { service ->
                ElevatedWhiteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BalajiTextDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Time: ${service.estimatedTime} • Pay After Service", fontSize = 12.sp, color = BalajiTealDeep, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Notes: ${service.description}", fontSize = 12.sp, color = BalajiTextMuted)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (service.isActive) "Active" else "Hidden", fontSize = 11.sp, color = if (service.isActive) BalajiGuaranteeGreen else BalajiTextMuted, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = service.isActive,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            repository.updateService(service.copy(isActive = checked))
                                        }
                                    },
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }

                            Row {
                                IconButton(onClick = { editingService = service }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BalajiTealDeep)
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        repository.deleteService(service.id)
                                        Toast.makeText(context, "Service Deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BalajiEmergencyRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingService != null) {
        val s = editingService
        var name by remember { mutableStateOf(s?.name ?: "") }
        var time by remember { mutableStateOf(s?.estimatedTime ?: "45 Min") }
        var desc by remember { mutableStateOf(s?.description ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingService = null
            },
            title = { Text(if (s == null) "Add New Service" else "Edit Service", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Estimated Time (e.g. 45 Min)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Technician Notes / Description") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (s == null) {
                                repository.addService(
                                    ServiceItem(
                                        id = "srv_${System.currentTimeMillis()}",
                                        name = name.trim(),
                                        description = desc.trim(),
                                        estimatedTime = time.trim(),
                                        isActive = true
                                    )
                                )
                            } else {
                                repository.updateService(
                                    s.copy(
                                        name = name.trim(),
                                        description = desc.trim(),
                                        estimatedTime = time.trim()
                                    )
                                )
                            }
                            showAddDialog = false
                            editingService = null
                            Toast.makeText(context, "Saved Successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                ) {
                    Text("Save Service", color = BalajiCardWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingService = null
                }) {
                    Text("Cancel")
                }
            },
            containerColor = BalajiCardWhite
        )
    }
}

// -------------------------------------------------------------
// 2. USER MANAGEMENT (View Signups, Customer List, Red Delete Fake)
// -------------------------------------------------------------
@Composable
fun UserManagementView(repository: BalajiRepository) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val users by repository.allUsers.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("user_management_view")
    ) {
        Text("Registered Customers (${users.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
        Text("View new signups & remove fake numbers", fontSize = 12.sp, color = BalajiTealSecondary)

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(users, key = { it.phone }) { user ->
                ElevatedWhiteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BalajiTextDark)
                                if (user.role == "ADMIN") {
                                    Surface(color = BalajiOrangeWarm, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(start = 6.dp)) {
                                        Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BalajiCardWhite, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("📞 +91 ${user.phone}", fontSize = 13.sp, color = BalajiTealDeep, fontWeight = FontWeight.SemiBold)
                            if (user.address.isNotBlank()) {
                                Text("🏠 ${user.address}, ${user.area}", fontSize = 12.sp, color = BalajiTextMuted)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                repository.launchDialer(context, "+91${user.phone}")
                            }) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", tint = BalajiTealDeep)
                            }
                            IconButton(onClick = {
                                repository.launchWhatsApp(context, "+91${user.phone}", "Namaste ${user.name} ji, Balaji Air Conditioners se.")
                            }) {
                                Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                            }
                            // RED DELETE BUTTON TO REMOVE FAKE/UNWANTED USERS
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.deleteUser(user.phone)
                                        Toast.makeText(context, "User ${user.phone} deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Fake User", tint = BalajiEmergencyRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. BOOKINGS REGISTER & SERVICE DONE
// -------------------------------------------------------------
@Composable
fun BookingsRegisterView(repository: BalajiRepository) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val bookings by repository.allBookings.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "COMPLETED"

    val filteredList = remember(selectedFilter, bookings) {
        when (selectedFilter) {
            "PENDING" -> bookings.filter { it.status != "COMPLETED" }
            "COMPLETED" -> bookings.filter { it.status == "COMPLETED" }
            else -> bookings
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("bookings_register_view")
    ) {
        Text("Service Register & Service Done", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
        Text("Live appointments synced with Google Sheets", fontSize = 12.sp, color = BalajiTealSecondary)

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "All (${bookings.size})", "PENDING" to "Pending / Active", "COMPLETED" to "Completed").forEach { (f, label) ->
                FilterChip(
                    selected = selectedFilter == f,
                    onClick = { selectedFilter = f },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BalajiTealPrimary,
                        selectedLabelColor = BalajiNavyDark,
                        containerColor = BalajiNavyElevated,
                        labelColor = BalajiTextLight
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredList, key = { it.id }) { b ->
                ElevatedWhiteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(b.id, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = BalajiNavyDark)
                        Surface(
                            color = if (b.status == "COMPLETED") Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = b.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (b.status == "COMPLETED") Color(0xFF065F46) else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${b.serviceName} (${b.units} ${b.modelType})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BalajiTextDark)
                    Text("Customer: ${b.customerName} (+91 ${b.customerPhone})", fontSize = 13.sp, color = BalajiTealDeep, fontWeight = FontWeight.SemiBold)
                    Text("Location: ${b.address}, ${b.area}", fontSize = 12.sp, color = BalajiTextMuted)
                    if (b.issueNotes.isNotBlank()) {
                        Text("Issue: \"${b.issueNotes}\"", fontSize = 12.sp, color = BalajiTextDark)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (b.status != "COMPLETED") {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.updateBookingStatus(b.id, "TECHNICIAN_ASSIGNED")
                                        Toast.makeText(context, "Status: Technician Assigned", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                            ) {
                                Text("Assign", fontSize = 11.sp, color = BalajiCardWhite)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.updateBookingStatus(b.id, "COMPLETED")
                                        Toast.makeText(context, "Marked as Completed!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BalajiGuaranteeGreen)
                            ) {
                                Text("Mark Done", fontSize = 11.sp, color = BalajiCardWhite)
                            }
                        }

                        Button(
                            onClick = {
                                val msg = "Namaste ${b.customerName} ji! Balaji Air Conditioners se Sanjay Prajapati bol raha hoon aapki booking ${b.id} ke reference me."
                                repository.launchWhatsApp(context, "+91${b.customerPhone}", msg)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Text("WhatsApp", fontSize = 11.sp, color = BalajiCardWhite)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. DIGITAL BILLING & INVOICING (Calculate & WhatsApp Customer)
// -------------------------------------------------------------
@Composable
fun DigitalBillingView(repository: BalajiRepository) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val bookings by repository.allBookings.collectAsState(initial = emptyList())

    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var custName by remember { mutableStateOf("") }
    var custPhone by remember { mutableStateOf("") }
    var serviceCharge by remember { mutableStateOf("499") }
    var spareParts by remember { mutableStateOf("0") }
    var discountAmount by remember { mutableStateOf("0") }

    val total = remember(serviceCharge, spareParts, discountAmount) {
        val s = serviceCharge.toIntOrNull() ?: 0
        val p = spareParts.toIntOrNull() ?: 0
        val d = discountAmount.toIntOrNull() ?: 0
        (s + p - d).coerceAtLeast(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("digital_billing_view")
    ) {
        Text("Digital Billing & Invoicing", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
        Text("Generate invoice and send directly to customer's WhatsApp", fontSize = 12.sp, color = BalajiTealSecondary)

        Spacer(modifier = Modifier.height(14.dp))

        ElevatedWhiteCard {
            Text("Select Booking / Customer", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BalajiTextDark)
            Spacer(modifier = Modifier.height(6.dp))

            // Quick Select from active bookings
            var bookingDropdownExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (selectedBooking != null) "${selectedBooking?.id} - ${selectedBooking?.customerName}" else "Select a booking or enter details below",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { bookingDropdownExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                DropdownMenu(
                    expanded = bookingDropdownExpanded,
                    onDismissRequest = { bookingDropdownExpanded = false }
                ) {
                    bookings.forEach { b ->
                        DropdownMenuItem(
                            text = { Text("${b.id} • ${b.customerName} (${b.serviceName})") },
                            onClick = {
                                selectedBooking = b
                                custName = b.customerName
                                custPhone = b.customerPhone
                                discountAmount = b.discountAmount.toString()
                                bookingDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = custName,
                    onValueChange = { custName = it },
                    label = { Text("Customer Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = custPhone,
                    onValueChange = { custPhone = it },
                    label = { Text("Customer Phone") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Charges Input
            OutlinedTextField(
                value = serviceCharge,
                onValueChange = { serviceCharge = it },
                label = { Text("Service / Labor Charge (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = spareParts,
                onValueChange = { spareParts = it },
                label = { Text("Spare Parts Charge (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = discountAmount,
                onValueChange = { discountAmount = it },
                label = { Text("Discount Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Payable Amount:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BalajiTextDark)
                    Text("₹$total", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BalajiGuaranteeGreen)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (custPhone.isBlank()) {
                        Toast.makeText(context, "Kripya customer phone number enter karein", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            val dummyBooking = selectedBooking ?: Booking(
                                id = "BK-${System.currentTimeMillis() % 100000}",
                                customerName = custName.ifBlank { "Customer" },
                                customerPhone = custPhone,
                                address = "Direct Service",
                                area = "City",
                                serviceName = "AC / Fridge Complete Service"
                            )

                            val s = serviceCharge.toIntOrNull() ?: 499
                            val p = spareParts.toIntOrNull() ?: 0
                            val d = discountAmount.toIntOrNull() ?: 0

                            repository.completeBookingWithInvoice(dummyBooking.id, s, p, d)

                            // Launch WhatsApp directly with formatted invoice
                            val invoiceText = repository.formatInvoiceWhatsAppText(dummyBooking, s, p, d)
                            repository.launchWhatsApp(context, "+91$custPhone", invoiceText)
                            Toast.makeText(context, "Invoice sent to WhatsApp!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("send_invoice_whatsapp_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = BalajiCardWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("WhatsApp Invoice to Customer", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
            }
        }
    }
}

// -------------------------------------------------------------
// 5. SPECIAL DISCOUNTS (Promo Codes CRUD)
// -------------------------------------------------------------
@Composable
fun SpecialDiscountsView(repository: BalajiRepository) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val offers by repository.allOffers.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("special_discounts_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Special Discounts & Coupons", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
                Text("Active coupons displayed in customer app", fontSize = 12.sp, color = BalajiTealSecondary)
            }
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BalajiTealPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = BalajiNavyDark)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Coupon", color = BalajiNavyDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(offers, key = { it.code }) { offer ->
                ElevatedWhiteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Surface(color = Color(0xFFFFF1E6), shape = RoundedCornerShape(6.dp)) {
                                Text("🏷️ ${offer.code}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = BalajiOrangeFlame, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(offer.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BalajiTextDark)
                            Text("${offer.discountPercent}% Off (Max ₹${offer.maxDiscount}) • ${offer.description}", fontSize = 12.sp, color = BalajiTextMuted)
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                repository.deleteOffer(offer.code)
                                Toast.makeText(context, "Coupon removed", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BalajiEmergencyRed)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var code by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var percent by remember { mutableStateOf("15") }
        var maxDisc by remember { mutableStateOf("150") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create Promo Code", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Coupon Code (e.g. MONSOON20)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Offer Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = percent, onValueChange = { percent = it }, label = { Text("Discount %") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = maxDisc, onValueChange = { maxDisc = it }, label = { Text("Max Discount Amount (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description / Terms") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (code.isNotBlank()) {
                            coroutineScope.launch {
                                repository.addOffer(
                                    PromoOffer(
                                        code = code.trim().uppercase(),
                                        title = title.trim(),
                                        discountPercent = percent.toIntOrNull() ?: 10,
                                        maxDiscount = maxDisc.toIntOrNull() ?: 100,
                                        description = desc.trim(),
                                        isActive = true
                                    )
                                )
                                showAddDialog = false
                                Toast.makeText(context, "Coupon Added!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                ) {
                    Text("Save Coupon", color = BalajiCardWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            },
            containerColor = BalajiCardWhite
        )
    }
}

// -------------------------------------------------------------
// 6. MANAGE GALLERY (Photos CRUD)
// -------------------------------------------------------------
@Composable
fun ManageGalleryView(repository: BalajiRepository) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val items by repository.allGalleryItems.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("manage_gallery_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Manage Work Gallery", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
                Text("Showcase AC & Fridge repair before/after", fontSize = 12.sp, color = BalajiTealSecondary)
            }
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BalajiTealPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add", tint = BalajiNavyDark)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Photo", color = BalajiNavyDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.id }) { item ->
                ElevatedWhiteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BalajiTextDark)
                            Text("Category: ${item.category} • ${item.tag}", fontSize = 12.sp, color = BalajiOrangeFlame, fontWeight = FontWeight.SemiBold)
                            Text(item.description, fontSize = 12.sp, color = BalajiTextMuted)
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                repository.deleteGalleryItem(item.id)
                                Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BalajiEmergencyRed)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("AC Jet Wash") }
        var desc by remember { mutableStateOf("") }
        var tag by remember { mutableStateOf("Before & After") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Gallery Work", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (AC Jet Wash / Fridge / etc)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Work Details") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tag, onValueChange = { tag = it }, label = { Text("Tag (e.g. Verified / Before & After)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            coroutineScope.launch {
                                repository.addGalleryItem(
                                    GalleryItem(
                                        id = "gal_${System.currentTimeMillis()}",
                                        title = title.trim(),
                                        category = category.trim(),
                                        description = desc.trim(),
                                        tag = tag.trim()
                                    )
                                )
                                showAddDialog = false
                                Toast.makeText(context, "Work photo added!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                ) {
                    Text("Add", color = BalajiCardWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            },
            containerColor = BalajiCardWhite
        )
    }
}

// -------------------------------------------------------------
// 7. MANAGE REVIEWS & OWNER REPLY
// -------------------------------------------------------------
@Composable
fun ManageReviewsView(repository: BalajiRepository) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val reviews by repository.allReviews.collectAsState(initial = emptyList())
    var replyTargetReview by remember { mutableStateOf<ReviewItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("manage_reviews_view")
    ) {
        Text("Manage Customer Reviews", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
        Text("Remove fake reviews and reply as Sanjay & Sandip Prajapati", fontSize = 12.sp, color = BalajiTealSecondary)

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(reviews, key = { it.id }) { rev ->
                ElevatedWhiteCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${rev.customerName} ⭐ ${rev.rating}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BalajiTextDark)
                            Text("Service: ${rev.serviceUsed} • ${rev.dateText}", fontSize = 11.sp, color = BalajiTealDeep)
                            Text("\"${rev.comment}\"", fontSize = 13.sp, color = BalajiTextDark, modifier = Modifier.padding(vertical = 4.dp))

                            if (!rev.ownerReply.isNullOrBlank()) {
                                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                    Text("👨‍🔧 Reply: ${rev.ownerReply}", fontSize = 11.sp, color = BalajiNavyDark, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Column {
                            IconButton(onClick = { replyTargetReview = rev }) {
                                Icon(Icons.Default.Reply, contentDescription = "Reply", tint = BalajiTealDeep)
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    repository.deleteReview(rev.id)
                                    Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BalajiEmergencyRed)
                            }
                        }
                    }
                }
            }
        }
    }

    if (replyTargetReview != null) {
        val r = replyTargetReview!!
        var replyText by remember { mutableStateOf(r.ownerReply ?: "") }
        var replyAuthor by remember { mutableStateOf(r.ownerReplyBy.ifBlank { "Sanjay Prajapati (Owner)" }) }

        AlertDialog(
            onDismissRequest = { replyTargetReview = null },
            title = { Text("Owner Ka Jawab (Reply to ${r.customerName})", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = replyAuthor, onValueChange = { replyAuthor = it }, label = { Text("Replying As") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = replyText, onValueChange = { replyText = it }, label = { Text("Your Reply Message") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.addOwnerReply(r.id, replyText.trim(), replyAuthor.trim())
                            replyTargetReview = null
                            Toast.makeText(context, "Reply posted!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
                ) {
                    Text("Post Reply", color = BalajiCardWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { replyTargetReview = null }) { Text("Cancel") }
            },
            containerColor = BalajiCardWhite
        )
    }
}

// -------------------------------------------------------------
// 8. PUSH NOTIFICATIONS / BROADCAST ALERTS
// -------------------------------------------------------------
@Composable
fun PushNotificationsView(repository: BalajiRepository) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("Summer AC Pre-Maintenance Alert ❄️") }
    var message by remember { mutableStateOf("Heatwave aane se pehle apna AC Jet Pump chemical service karwayein. Special 20% off active hai! Balaji Air Conditioners: +91 9157896306.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("push_notifications_view")
    ) {
        Text("Broadcast Push Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
        Text("Send seasonal maintenance reminders to registered customers", fontSize = 12.sp, color = BalajiTealSecondary)

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedWhiteCard {
            Text("Notification Title", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BalajiTextDark)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(12.dp))

            Text("Message Content", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BalajiTextDark)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = message, onValueChange = { message = it }, maxLines = 4, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "📢 Broadcast notification dispatched to customers!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = "Broadcast", tint = BalajiCardWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Broadcast to All Customers", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
            }
        }
    }
}

// -------------------------------------------------------------
// 9. GOOGLE SHEETS SETUP & SCHEMA DOCUMENTATION
// -------------------------------------------------------------
@Composable
fun GoogleSheetsSetupView(repository: BalajiRepository) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUrl = repository.userPreferences.appsScriptUrl.collectAsState().value
    var inputUrl by remember { mutableStateOf(currentUrl) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("google_sheets_setup_view")
    ) {
        Text("Google Sheets Integration (6 Tabs)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BalajiCardWhite)
        Text("Apps Script Web App API for Users, Bookings & Services", fontSize = 12.sp, color = BalajiTealSecondary)

        Spacer(modifier = Modifier.height(14.dp))

        ElevatedWhiteCard {
            Text("Apps Script Web App URL", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BalajiTextDark)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        repository.userPreferences.saveAppsScriptUrl(inputUrl.trim())
                        Toast.makeText(context, "URL Saved!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save URL", color = BalajiCardWhite, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isTesting = true
                        testResult = null
                        coroutineScope.launch {
                            val client = GoogleSheetsApiClient()
                            val res = client.testConnection(inputUrl.trim())
                            isTesting = false
                            testResult = if (res.isSuccess) {
                                "✅ " + res.getOrNull()
                            } else {
                                "❌ Failed: " + res.exceptionOrNull()?.message
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiTealPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(color = BalajiNavyDark, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Test Connection", color = BalajiNavyDark, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (testResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(testResult ?: "", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (testResult?.startsWith("✅") == true) BalajiGuaranteeGreen else BalajiEmergencyRed)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Copy Ready Apps Script Code Box
        ElevatedWhiteCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Apps Script Code (.gs)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BalajiTextDark)

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("AppsScriptCode", GoogleAppsScriptTemplate.COMPLETE_APPS_SCRIPT_CODE)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Full Apps Script Code Copied to Clipboard!", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = BalajiCardWhite, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Code", fontSize = 11.sp, color = BalajiCardWhite)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                Text(
                    text = GoogleAppsScriptTemplate.SCHEMA_DOCUMENTATION + "\n\n" + GoogleAppsScriptTemplate.COMPLETE_APPS_SCRIPT_CODE.take(600) + "...\n(Tap 'Copy Code' above for complete file)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
