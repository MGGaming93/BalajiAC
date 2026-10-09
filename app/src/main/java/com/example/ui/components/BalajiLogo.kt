package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
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
        // Outer Dual-Colored Circular Ring (Blue Left, Orange Right)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.toPx() * 0.038f
            val radius = (size.toPx() - strokeWidth) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            // Draw Blue Left Arc (from 90° to 270°)
            drawArc(
                color = Color(0xFF005691),
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )

            // Draw Vibrant Orange Right Arc (from 270° to 450°)
            drawArc(
                color = Color(0xFFFF7A00),
                startAngle = 270f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )

            // If pressing for Admin PIN, show smooth progress ring around the logo!
            if (pressProgress > 0f) {
                drawArc(
                    color = Color(0xFF00B4D8),
                    startAngle = -90f,
                    sweepAngle = pressProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius - 6, center.y - radius - 6),
                    size = Size((radius + 6) * 2, (radius + 6) * 2),
                    style = Stroke(width = 6f)
                )
            }
        }

        // Inner White Badge Surface
        Box(
            modifier = Modifier
                .size(size * 0.90f)
                .clip(CircleShape)
                .background(Color.White)
                .padding(size * 0.04f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Top Sacred Tilak with flame
                Box(
                    modifier = Modifier.size(size * 0.16f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = this.size.width
                        val h = this.size.height

                        // Flames on sides
                        val flameLeft = Path().apply {
                            moveTo(w * 0.2f, h * 0.9f)
                            cubicTo(w * 0.05f, h * 0.6f, w * 0.1f, h * 0.2f, w * 0.25f, 0f)
                            cubicTo(w * 0.35f, h * 0.3f, w * 0.3f, h * 0.7f, w * 0.38f, h * 0.9f)
                            close()
                        }
                        drawPath(flameLeft, color = Color(0xFFFF9F1C))

                        val flameRight = Path().apply {
                            moveTo(w * 0.8f, h * 0.9f)
                            cubicTo(w * 0.95f, h * 0.6f, w * 0.9f, h * 0.2f, w * 0.75f, 0f)
                            cubicTo(w * 0.65f, h * 0.3f, w * 0.7f, h * 0.7f, w * 0.62f, h * 0.9f)
                            close()
                        }
                        drawPath(flameRight, color = Color(0xFFFF9F1C))

                        // Sacred U-Tilak
                        val uPath = Path().apply {
                            moveTo(w * 0.32f, h * 0.15f)
                            lineTo(w * 0.38f, h * 0.7f)
                            cubicTo(w * 0.42f, h * 0.95f, w * 0.58f, h * 0.95f, w * 0.62f, h * 0.7f)
                            lineTo(w * 0.68f, h * 0.15f)
                            lineTo(w * 0.58f, h * 0.15f)
                            lineTo(w * 0.54f, h * 0.65f)
                            cubicTo(w * 0.52f, h * 0.75f, w * 0.48f, h * 0.75f, w * 0.46f, h * 0.65f)
                            lineTo(w * 0.42f, h * 0.15f)
                            close()
                        }
                        drawPath(uPath, color = Color(0xFF1E293B))

                        // Red Tilak inside
                        val redTilak = Path().apply {
                            moveTo(w * 0.5f, 0f)
                            cubicTo(w * 0.44f, h * 0.3f, w * 0.44f, h * 0.65f, w * 0.5f, h * 0.78f)
                            cubicTo(w * 0.56f, h * 0.65f, w * 0.56f, h * 0.3f, w * 0.5f, 0f)
                            close()
                        }
                        drawPath(redTilak, color = Color(0xFFDC2626))
                    }
                }

                // 2. Bold Brand Name: BALAJI
                Text(
                    text = "BALAJI",
                    fontSize = (size.value * 0.13f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F3057),
                    letterSpacing = 1.sp,
                    lineHeight = (size.value * 0.13f).sp
                )

                // 3. Sub-text: AIR CONDITIONERS
                Text(
                    text = "AIR CONDITIONERS",
                    fontSize = (size.value * 0.052f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F3057),
                    letterSpacing = 0.5.sp
                )

                // 4. Divider Tag: AC & FRIDGE SERVICE
                Text(
                    text = "— AC & FRIDGE SERVICE —",
                    fontSize = (size.value * 0.038f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(size * 0.015f))

                // 5. Central Graphic: AC Unit + Refrigerator
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(size * 0.22f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Split AC Unit on Left
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(size * 0.18f),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = this.size.width
                            val h = this.size.height

                            // AC Body
                            drawRoundRect(
                                color = Color(0xFFF1F5F9),
                                topLeft = Offset(0f, h * 0.1f),
                                size = Size(w * 0.95f, h * 0.55f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                                style = androidx.compose.ui.graphics.drawscope.Fill
                            )
                            drawRoundRect(
                                color = Color(0xFF0F3057),
                                topLeft = Offset(0f, h * 0.1f),
                                size = Size(w * 0.95f, h * 0.55f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                                style = Stroke(width = 2f)
                            )
                            // Vent line
                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = Offset(w * 0.05f, h * 0.52f),
                                end = Offset(w * 0.90f, h * 0.52f),
                                strokeWidth = 1.5f
                            )
                            // Display LED
                            drawLine(
                                color = Color(0xFF00B4D8),
                                start = Offset(w * 0.72f, h * 0.32f),
                                end = Offset(w * 0.88f, h * 0.32f),
                                strokeWidth = 3f
                            )
                            // Cooling Wind Streams
                            val path1 = Path().apply {
                                moveTo(w * 0.15f, h * 0.68f)
                                quadraticTo(w * 0.28f, h * 0.95f, w * 0.42f, h * 0.98f)
                            }
                            drawPath(path1, color = Color(0xFF00B4D8), style = Stroke(width = 2.5f))

                            val path2 = Path().apply {
                                moveTo(w * 0.40f, h * 0.68f)
                                quadraticTo(w * 0.53f, h * 0.95f, w * 0.68f, h * 0.98f)
                            }
                            drawPath(path2, color = Color(0xFF38BDF8), style = Stroke(width = 2.5f))
                        }
                    }

                    Spacer(modifier = Modifier.width(size * 0.02f))

                    // Refrigerator on Right
                    Box(
                        modifier = Modifier
                            .weight(0.7f)
                            .height(size * 0.22f),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = this.size.width
                            val h = this.size.height

                            // Refrigerator Body
                            drawRoundRect(
                                color = Color(0xFFE2E8F0),
                                topLeft = Offset(w * 0.1f, 0f),
                                size = Size(w * 0.8f, h * 0.95f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                                style = androidx.compose.ui.graphics.drawscope.Fill
                            )
                            drawRoundRect(
                                color = Color(0xFF0F3057),
                                topLeft = Offset(w * 0.1f, 0f),
                                size = Size(w * 0.8f, h * 0.95f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                                style = Stroke(width = 2f)
                            )
                            // Freezer separation line
                            drawLine(
                                color = Color(0xFF0F3057),
                                start = Offset(w * 0.1f, h * 0.38f),
                                end = Offset(w * 0.9f, h * 0.38f),
                                strokeWidth = 2f
                            )
                            // Upper door handle
                            drawLine(
                                color = Color(0xFF0F3057),
                                start = Offset(w * 0.22f, h * 0.15f),
                                end = Offset(w * 0.22f, h * 0.3f),
                                strokeWidth = 2.5f
                            )
                            // Lower door handle
                            drawLine(
                                color = Color(0xFF0F3057),
                                start = Offset(w * 0.22f, h * 0.45f),
                                end = Offset(w * 0.22f, h * 0.65f),
                                strokeWidth = 2.5f
                            )
                        }
                    }
                }

                if (showSubtext) {
                    Spacer(modifier = Modifier.height(size * 0.015f))
                    // Bottom Slogan: Cool Comfort, Always
                    Text(
                        text = "Cool Comfort, Always",
                        fontSize = (size.value * 0.042f).sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF005691),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
