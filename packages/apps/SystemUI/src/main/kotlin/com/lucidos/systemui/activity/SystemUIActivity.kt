package com.lucidos.systemui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucidos.systemui.QuickSettingsManager
import kotlin.math.roundToInt

class SystemUIActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize QuickSettingsManager with Application context
        QuickSettingsManager.initialize(applicationContext)

        setContent {
            SystemUIApp()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SystemUIApp() {
    // Shade expand state
    var isShadeExpanded by remember { mutableStateOf(false) }

    // Toggle states connected to QuickSettingsManager
    var wifiOn by remember { mutableStateOf(QuickSettingsManager.isWiFiEnabled()) }
    var bluetoothOn by remember { mutableStateOf(QuickSettingsManager.isBluetoothEnabled()) }
    var nfcOn by remember { mutableStateOf(QuickSettingsManager.isNfcEnabled()) }
    var airplaneModeOn by remember { mutableStateOf(false) }
    var darkModeOn by remember { mutableStateOf(false) }

    var brightness by remember { mutableFloatStateOf(50f) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF64B5F6),
            background = Color.Black,
            surface = Color(0xFF1E1E1E)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main Desktop Simulation Background Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 28.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LucidOS",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Swipe down from top for Quick Settings",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Real Android Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(Color.Black)
                    .clickable { isShadeExpanded = !isShadeExpanded }
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "10:00 AM",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (wifiOn) Text("📶 WiFi", color = Color.White, fontSize = 10.sp)
                    if (bluetoothOn) Text("🔹 BT", color = Color.White, fontSize = 10.sp)
                    if (nfcOn) Text("📲 NFC", color = Color.White, fontSize = 10.sp)
                    Text("🔋 100%", color = Color.White, fontSize = 10.sp)
                }
            }

            // Expandable Notification & Quick Settings Shade Overlay
            AnimatedVisibility(
                visible = isShadeExpanded,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { isShadeExpanded = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .background(
                                Color(0xFF161618),
                                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                            .clickable(enabled = false) {}
                            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        // Drag/Collapse Handle
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(5.dp)
                                .background(Color.Gray, shape = RoundedCornerShape(2.5.dp))
                                .align(Alignment.CenterHorizontally)
                                .clickable { isShadeExpanded = false }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scrollable Shade Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Quick Settings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            // Toggles Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuickSettingsTile(
                                    label = "Wi-Fi",
                                    isOn = wifiOn,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        wifiOn = !wifiOn
                                        QuickSettingsManager.toggleWiFi(wifiOn)
                                    }
                                )
                                QuickSettingsTile(
                                    label = "Bluetooth",
                                    isOn = bluetoothOn,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        bluetoothOn = !bluetoothOn
                                        QuickSettingsManager.toggleBluetooth(bluetoothOn)
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuickSettingsTile(
                                    label = "NFC",
                                    isOn = nfcOn,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        nfcOn = !nfcOn
                                        QuickSettingsManager.toggleNfc(nfcOn)
                                    }
                                )
                                QuickSettingsTile(
                                    label = "Airplane",
                                    isOn = airplaneModeOn,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        airplaneModeOn = !airplaneModeOn
                                        QuickSettingsManager.toggleAirplaneMode(airplaneModeOn)
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuickSettingsTile(
                                    label = "Dark Mode",
                                    isOn = darkModeOn,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        darkModeOn = !darkModeOn
                                        QuickSettingsManager.toggleDarkMode(darkModeOn)
                                    }
                                )
                                Box(modifier = Modifier.weight(1f)) // spacer
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Brightness Bar
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF262629), shape = RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Brightness", color = Color.White, fontSize = 14.sp)
                                    Text("${brightness.toInt()}%", color = Color.Gray, fontSize = 14.sp)
                                }
                                Slider(
                                    value = brightness,
                                    onValueChange = {
                                        brightness = it
                                        QuickSettingsManager.setScreenBrightness(it.toInt())
                                    },
                                    valueRange = 0f..100f
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Notifications Section
                            Text(
                                text = "Notifications",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            NotificationCard(
                                title = "System Update",
                                body = "LucidOS 1.0.0-alpha is ready to build."
                            )

                            NotificationCard(
                                title = "Connectivity Helper",
                                body = "WiFi and Bluetooth managers are running."
                            )
                        }
                    }
                }
            }

            // Real Android Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.Black)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Text("◀", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp, modifier = Modifier.clickable {
                    if (isShadeExpanded) isShadeExpanded = false
                })
                // Home Button
                Text("●", color = Color.White.copy(alpha = 0.7f), fontSize = 20.sp, modifier = Modifier.clickable {
                    isShadeExpanded = false
                })
                // Recents Button
                Text("■", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp, modifier = Modifier.clickable {})
            }
        }
    }
}

@Composable
fun QuickSettingsTile(
    label: String,
    isOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isOn) Color(0xFF64B5F6) else Color(0xFF262629))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = label,
                color = if (isOn) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isOn) "Active" else "Off",
                color = if (isOn) Color.Black.copy(alpha = 0.7f) else Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun NotificationCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF262629))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = body, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
