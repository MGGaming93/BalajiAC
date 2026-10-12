package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.BalajiTealPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BalajiLogo(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    showSubtext: Boolean = true,
    enableAdminLongPress: Boolean = false,
    onAdminTrigger: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isPressing by remember { mutableStateOf(false) }
    var pressProgress by remember { mutableFloatStateOf(0f) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("balaji_main_logo")
            .then(
                if (enableAdminLongPress) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressing = true
                                pressProgress = 0f
                                val startTime = System.currentTimeMillis()
                                val job = coroutineScope.launch {
                                    while (isPressing) {
                                        val elapsed = System.currentTimeMillis() - startTime
                                        pressProgress = (elapsed / 5000f).coerceIn(0f, 1f)
                                        if (elapsed >= 5000) {
                                            isPressing = false
                                            pressProgress = 0f
                                            onAdminTrigger()
                                            break
                                        }
                                        delay(50)
                                    }
                                }
                                tryAwaitRelease()
                                isPressing = false
                                pressProgress = 0f
                                job.cancel()
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        // Outer subtle white badge container with clean circular clip
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(width = 1.5.dp, color = Color(0xFFE2E8F0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Balaji Air Conditioners Official Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.04f)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        }

        // If pressing for Admin PIN (5 sec hold), show animated cyan progress arc around logo
        if (pressProgress > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6f
                val radius = (this.size.minDimension - strokeWidth) / 2
                val center = Offset(this.size.width / 2, this.size.height / 2)

                drawArc(
                    color = Color(0xFF00B4D8),
                    startAngle = -90f,
                    sweepAngle = pressProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
            }
        }
    }
}
