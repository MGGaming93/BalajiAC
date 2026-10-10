package com.example.ui.screens.admin

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.repository.BalajiRepository
import com.example.ui.components.ElevatedWhiteCard
import com.example.ui.theme.*

enum class AdminModule {
    DASHBOARD_GRID,
    MANAGE_SERVICES,
    USER_MANAGEMENT,
    BOOKINGS_REGISTER,
    DIGITAL_BILLING,
    SPECIAL_DISCOUNTS,
    MANAGE_GALLERY,
    MANAGE_REVIEWS,
    PUSH_NOTIFICATIONS,
    GOOGLE_SHEETS_SETUP
}

data class AdminGridTile(
    val module: AdminModule,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeText: String? = null,
    val iconTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: BalajiRepository,
    onBackToCustomerView: () -> Unit
) {
    val context = LocalContext.current
    var activeModule by remember { mutableStateOf(AdminModule.DASHBOARD_GRID) }

    // State counts
    val allServices by repository.allServices.collectAsState(initial = emptyList())
    val allBookings by repository.allBookings.collectAsState(initial = emptyList())
    val allUsers by repository.allUsers.collectAsState(initial = emptyList())
    val allOffers by repository.allOffers.collectAsState(initial = emptyList())
    val allReviews by repository.allReviews.collectAsState(initial = emptyList())
    val allGallery by repository.allGalleryItems.collectAsState(initial = emptyList())

    val pendingCount = remember(allBookings) { allBookings.count { it.status == "PENDING" } }

    BackHandler {
        if (activeModule != AdminModule.DASHBOARD_GRID) {
            activeModule = AdminModule.DASHBOARD_GRID
        } else {
            onBackToCustomerView()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (activeModule == AdminModule.DASHBOARD_GRID) "Admin Command Center" else activeModule.name.replace("_", " "),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BalajiTextLight
                        )
                        Text(
                            text = "Balaji Air Conditioners • PIN 1099 Auth",
                            fontSize = 11.sp,
                            color = BalajiTealPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeModule != AdminModule.DASHBOARD_GRID) {
                            activeModule = AdminModule.DASHBOARD_GRID
                        } else {
                            onBackToCustomerView()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BalajiTextLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onBackToCustomerView) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Customer View",
                            tint = BalajiTextLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BalajiNavyDark)
            )
        },
        containerColor = BalajiNavyDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeModule) {
                AdminModule.DASHBOARD_GRID -> {
                    AdminGridContent(
                        pendingBookingsCount = pendingCount,
                        totalBookings = allBookings.size,
                        totalUsers = allUsers.size,
                        totalServices = allServices.size,
                        onModuleSelected = { activeModule = it }
                    )
                }
                AdminModule.MANAGE_SERVICES -> {
                    ManageServicesView(repository = repository)
                }
                AdminModule.USER_MANAGEMENT -> {
                    UserManagementView(repository = repository)
                }
                AdminModule.BOOKINGS_REGISTER -> {
                    BookingsRegisterView(repository = repository)
                }
                AdminModule.DIGITAL_BILLING -> {
                    DigitalBillingView(repository = repository)
                }
                AdminModule.SPECIAL_DISCOUNTS -> {
                    SpecialDiscountsView(repository = repository)
                }
                AdminModule.MANAGE_GALLERY -> {
                    ManageGalleryView(repository = repository)
                }
                AdminModule.MANAGE_REVIEWS -> {
                    ManageReviewsView(repository = repository)
                }
                AdminModule.PUSH_NOTIFICATIONS -> {
                    PushNotificationsView(repository = repository)
                }
                AdminModule.GOOGLE_SHEETS_SETUP -> {
                    GoogleSheetsSetupView(repository = repository)
                }
            }
        }
    }
}

@Composable
fun AdminGridContent(
    pendingBookingsCount: Int,
    totalBookings: Int,
    totalUsers: Int,
    totalServices: Int,
    onModuleSelected: (AdminModule) -> Unit
) {
    val tiles = listOf(
        AdminGridTile(
            module = AdminModule.MANAGE_SERVICES,
            title = "Manage Services",
            subtitle = "Service Catalog CRUD",
            icon = Icons.Default.Handyman,
            badgeText = "$totalServices items",
            iconTint = Color(0xFF0284C7)
        ),
        AdminGridTile(
            module = AdminModule.USER_MANAGEMENT,
            title = "Customers",
            subtitle = "New Signups & List",
            icon = Icons.Default.People,
            badgeText = "$totalUsers users",
            iconTint = Color(0xFF8B5CF6)
        ),
        AdminGridTile(
            module = AdminModule.BOOKINGS_REGISTER,
            title = "Service Register",
            subtitle = "Pending & Done Jobs",
            icon = Icons.Default.ShoppingCart,
            badgeText = if (pendingBookingsCount > 0) "$pendingBookingsCount pending" else "$totalBookings total",
            iconTint = if (pendingBookingsCount > 0) Color(0xFFEF4444) else Color(0xFF10B981)
        ),
        AdminGridTile(
            module = AdminModule.DIGITAL_BILLING,
            title = "Digital Billing",
            subtitle = "Create & WhatsApp Bill",
            icon = Icons.Default.ReceiptLong,
            badgeText = "Invoicing",
            iconTint = Color(0xFF10B981)
        ),
        AdminGridTile(
            module = AdminModule.SPECIAL_DISCOUNTS,
            title = "Special Discounts",
            subtitle = "Coupons & Offers",
            icon = Icons.Default.Percent,
            badgeText = "Promo Codes",
            iconTint = Color(0xFFF59E0B)
        ),
        AdminGridTile(
            module = AdminModule.MANAGE_GALLERY,
            title = "Work Gallery",
            subtitle = "Photos & Categories",
            icon = Icons.Default.PhotoLibrary,
            badgeText = "Photos",
            iconTint = Color(0xFF06B6D4)
        ),
        AdminGridTile(
            module = AdminModule.MANAGE_REVIEWS,
            title = "Customer Reviews",
            subtitle = "Balaji AC Replies",
            icon = Icons.Default.Star,
            badgeText = "5.0 Stars",
            iconTint = Color(0xFFFBBF24)
        ),
        AdminGridTile(
            module = AdminModule.PUSH_NOTIFICATIONS,
            title = "Broadcast Push",
            subtitle = "Customer Alerts",
            icon = Icons.Default.Notifications,
            badgeText = "Push",
            iconTint = Color(0xFFEC4899)
        ),
        AdminGridTile(
            module = AdminModule.GOOGLE_SHEETS_SETUP,
            title = "Google Sheets API",
            subtitle = "Apps Script Integration",
            icon = Icons.Default.TableChart,
            badgeText = "CRUD Sync",
            iconTint = Color(0xFF14B8A6)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_dashboard_grid")
    ) {
        // Top Stats Banner
        ElevatedWhiteCard(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Balaji Air Conditioners",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = BalajiTextDark
                    )
                    Text(
                        text = "Admin & Operations Portal",
                        fontSize = 12.sp,
                        color = BalajiTealDeep
                    )
                }

                Surface(
                    color = if (pendingBookingsCount > 0) Color(0xFFFEE2E2) else Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (pendingBookingsCount > 0) "$pendingBookingsCount Action Needed" else "All Done ✅",
                        color = if (pendingBookingsCount > 0) Color(0xFFDC2626) else Color(0xFF059669),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // ICON GRID LAYOUT (Responsive 2 Columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tiles) { tile ->
                ElevatedWhiteCard(
                    modifier = Modifier.testTag("admin_tile_${tile.module.name}"),
                    onClick = { onModuleSelected(tile.module) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(tile.iconTint.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = tile.title,
                                    tint = tile.iconTint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (tile.badgeText != null) {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = tile.badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BalajiTextDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = tile.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BalajiTextDark,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tile.subtitle,
                            fontSize = 11.sp,
                            color = BalajiTextMuted
                        )
                    }
                }
            }
        }
    }
}
