package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BalajiLogo
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BalajiNavyDark)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Centered Balaji Emblem Logo
            BalajiLogo(
                size = 190.dp,
                showSubtext = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tagline: "Cool Comfort, Always"
            Text(
                text = "Cool Comfort, Always",
                color = BalajiTealPrimary,
                fontSize = 22.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AC & Fridge Repair Specialists",
                color = BalajiTextLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Loading Indicator
            CircularProgressIndicator(
                color = BalajiTealPrimary,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("splash_loader")
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Founders acknowledgment
            Text(
                text = "Managed by:\nSanjay Ghanshyam Prajapati & Sandip Prajapati",
                color = BalajiTextSubtle,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
