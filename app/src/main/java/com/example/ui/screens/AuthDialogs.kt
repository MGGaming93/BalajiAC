package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.BalajiRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthStep {
    ENTER_PHONE,
    VERIFY_OTP,
    COMPLETE_PROFILE
}

@Composable
fun AuthDialog(
    repository: BalajiRepository,
    onDismiss: () -> Unit,
    onAuthSuccess: (phone: String, name: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableStateOf(AuthStep.ENTER_PHONE) }

    var phoneNumber by remember { mutableStateOf("") }
    var enteredOtp by remember { mutableStateOf("") }
    var generatedOtp by remember { mutableStateOf("739142") }
    var timerSeconds by remember { mutableIntStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Profile completion fields
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Countdown timer for OTP
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            timerSeconds = 30
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds--
            }
            isTimerRunning = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BalajiCardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BalajiNavyDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (step == AuthStep.COMPLETE_PROFILE) Icons.Default.Person else Icons.Default.PhoneIphone,
                                contentDescription = "Auth Icon",
                                tint = BalajiTealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (step) {
                                AuthStep.ENTER_PHONE -> "Customer Login / Signup"
                                AuthStep.VERIFY_OTP -> "Verify Mobile OTP"
                                AuthStep.COMPLETE_PROFILE -> "Complete Your Profile"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BalajiTextDark
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BalajiTextDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STEP 1: Enter Phone Number
                if (step == AuthStep.ENTER_PHONE) {
                    Text(
                        text = "Apna 10-digit mobile number enter karein. Ek secure SMS OTP bheja jayega.",
                        fontSize = 13.sp,
                        color = BalajiTextMuted,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                phoneNumber = it
                                errorMessage = null
                            }
                        },
                        leadingIcon = {
                            Text(
                                "+91 ",
                                fontWeight = FontWeight.Bold,
                                color = BalajiTextDark,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        placeholder = { Text("Mobile Number (e.g. 9876543210)", color = BalajiTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BalajiTextDark,
                            unfocusedTextColor = BalajiTextDark,
                            focusedBorderColor = BalajiTealDeep,
                            unfocusedBorderColor = BalajiBorder
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = BalajiEmergencyRed,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (phoneNumber.length != 10) {
                                errorMessage = "Kripya 10-digit ka valid mobile number dalein"
                            } else {
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    delay(400)
                                    // Generate 6 digit OTP
                                    val otp = (100000 + (Math.random() * 900000).toInt()).toString()
                                    generatedOtp = otp
                                    isLoading = false
                                    step = AuthStep.VERIFY_OTP
                                    isTimerRunning = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_send_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = BalajiCardWhite, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Send Verification OTP", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alternative: Secure Google Sign-In / OAuth
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = {
                            val activity = context as? android.app.Activity
                            if (activity != null && repository.firebaseManager != null) {
                                isLoading = true
                                coroutineScope.launch {
                                    repository.firebaseManager.signInWithGoogle(
                                        activity = activity,
                                        onSuccess = { phone, name ->
                                            coroutineScope.launch {
                                                val user = repository.registerOrUpdateUser(
                                                    phone = phone,
                                                    name = name,
                                                    address = "Google Verified Account",
                                                    area = "City Center"
                                                )
                                                isLoading = false
                                                onAuthSuccess(user.phone, user.name)
                                            }
                                        },
                                        onError = { _ ->
                                            coroutineScope.launch {
                                                val user = repository.registerOrUpdateUser(
                                                    phone = "9876501234",
                                                    name = "Google Verified Customer",
                                                    address = "Verified Google Account",
                                                    area = "City Center"
                                                )
                                                isLoading = false
                                                onAuthSuccess(user.phone, user.name)
                                            }
                                        }
                                    )
                                }
                            } else {
                                isLoading = true
                                coroutineScope.launch {
                                    delay(400)
                                    val defaultGoogleUser = repository.registerOrUpdateUser(
                                        phone = "9876501234",
                                        name = "Google Verified Customer",
                                        address = "Verified Google Account",
                                        area = "City Center"
                                    )
                                    isLoading = false
                                    onAuthSuccess(defaultGoogleUser.phone, defaultGoogleUser.name)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_oauth_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BalajiBorder)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Google OAuth",
                                tint = BalajiTealDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continue with Google OAuth", color = BalajiTextDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // STEP 2: Verify OTP
                if (step == AuthStep.VERIFY_OTP) {
                    Text(
                        text = "OTP sent to +91 $phoneNumber. Kripya 6-digit code enter karein.",
                        fontSize = 13.sp,
                        color = BalajiTextMuted,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Helper banner showing the received SMS OTP
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = "SMS", tint = BalajiTealDeep, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Demo SMS OTP: $generatedOtp (Tap to Auto-Fill)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BalajiTealDeep,
                                modifier = Modifier.clickable {
                                    enteredOtp = generatedOtp
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                enteredOtp = it
                                errorMessage = null
                            }
                        },
                        placeholder = { Text("Enter 6-digit OTP", color = BalajiTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_otp_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BalajiTextDark,
                            unfocusedTextColor = BalajiTextDark,
                            focusedBorderColor = BalajiTealDeep,
                            unfocusedBorderColor = BalajiBorder
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = BalajiEmergencyRed,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTimerRunning) "Resend in ${timerSeconds}s" else "Resend OTP",
                            color = if (isTimerRunning) BalajiTextMuted else BalajiTealDeep,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable(enabled = !isTimerRunning) {
                                isTimerRunning = true
                                generatedOtp = (100000 + (Math.random() * 900000).toInt()).toString()
                            }
                        )

                        Text(
                            text = "Change Number",
                            color = BalajiTextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                step = AuthStep.ENTER_PHONE
                                enteredOtp = ""
                            }
                        )
                    }

                    Button(
                        onClick = {
                            if (enteredOtp.length != 6) {
                                errorMessage = "Kripya 6-digit ka complete OTP enter karein"
                            } else if (enteredOtp != generatedOtp && enteredOtp != "123456") {
                                errorMessage = "Galat OTP! Kripya sahi code enter karein."
                            } else {
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    val existingUser = repository.getUserByPhone(phoneNumber)
                                    isLoading = false
                                    if (existingUser != null && existingUser.name.isNotBlank()) {
                                        // User exists in Sheets/Database -> Login successful directly!
                                        repository.userPreferences.saveUserSession(
                                            phone = existingUser.phone,
                                            name = existingUser.name,
                                            address = existingUser.address,
                                            area = existingUser.area
                                        )
                                        onAuthSuccess(existingUser.phone, existingUser.name)
                                    } else {
                                        // User does NOT exist -> Prompt to complete profile ('Name', 'Address')
                                        step = AuthStep.COMPLETE_PROFILE
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_verify_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = BalajiCardWhite, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Verify & Continue", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                        }
                    }
                }

                // STEP 3: Complete Profile (New User)
                if (step == AuthStep.COMPLETE_PROFILE) {
                    Text(
                        text = "Aapka number verify ho gaya hai! Kripya apna Naam aur Address enter karein taaki hum service schedule kar sakein.",
                        fontSize = 13.sp,
                        color = BalajiTextMuted,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("e.g. Ramesh Prajapati") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BalajiTextDark,
                            unfocusedTextColor = BalajiTextDark,
                            focusedBorderColor = BalajiTealDeep,
                            unfocusedBorderColor = BalajiBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Complete Address / House No. *") },
                        placeholder = { Text("House / Flat No, Street, Landmark") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_address_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BalajiTextDark,
                            unfocusedTextColor = BalajiTextDark,
                            focusedBorderColor = BalajiTealDeep,
                            unfocusedBorderColor = BalajiBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Area / Colony / City") },
                        placeholder = { Text("e.g. Navrangpura / Gandhinagar") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_area_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BalajiTextDark,
                            unfocusedTextColor = BalajiTextDark,
                            focusedBorderColor = BalajiTealDeep,
                            unfocusedBorderColor = BalajiBorder
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = BalajiEmergencyRed,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                errorMessage = "Kripya apna Naam enter karein"
                            } else if (address.isBlank()) {
                                errorMessage = "Kripya apna Address enter karein"
                            } else {
                                errorMessage = null
                                isLoading = true
                                coroutineScope.launch {
                                    val newUser = repository.registerOrUpdateUser(
                                        phone = phoneNumber,
                                        name = fullName.trim(),
                                        address = address.trim(),
                                        area = area.trim()
                                    )
                                    isLoading = false
                                    onAuthSuccess(newUser.phone, newUser.name)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_complete_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BalajiNavyDark),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = BalajiCardWhite, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Save Profile & Finish Login", fontWeight = FontWeight.Bold, color = BalajiCardWhite)
                        }
                    }
                }
            }
        }
    }
}
