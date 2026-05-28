package com.lucidos.settings

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lucidos.connectivity.BluetoothManager
import com.lucidos.connectivity.NfcManager
import com.lucidos.connectivity.WiFiManager

class SettingsActivity : ComponentActivity() {

    private val TAG = "SettingsActivity"
    private lateinit var wifiManager: WiFiManager
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var nfcManager: NfcManager
    private var uiModeManager: UiModeManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize managers
        wifiManager = WiFiManager(this)
        bluetoothManager = BluetoothManager(this)
        nfcManager = NfcManager(this)
        uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager

        // Check write settings permission for brightness control
        checkSettingsPermission()

        setContent {
            LucidSettingsApp()
        }
    }

    private fun checkSettingsPermission() {
        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LucidSettingsApp() {
        var wifiEnabled by remember { mutableStateOf(wifiManager.isWiFiEnabled()) }
        var bluetoothEnabled by remember { mutableStateOf(bluetoothManager.isBluetoothEnabled()) }
        var nfcEnabled by remember { mutableStateOf(nfcManager.isNfcEnabled()) }

        // Fetch current brightness (0 to 255 scaled to 0 to 100)
        var brightnessVal by remember {
            val current = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                128
            }
            mutableFloatStateOf((current * 100f) / 255f)
        }

        var airplaneModeEnabled by remember {
            val current = try {
                Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
            } catch (e: Exception) {
                false
            }
            mutableStateOf(current)
        }

        var darkModeEnabled by remember {
            val current = uiModeManager?.nightMode == UiModeManager.MODE_NIGHT_YES
            mutableStateOf(current)
        }

        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFF64B5F6),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E),
                onSurface = Color(0xFFEEEEEE),
                onBackground = Color(0xFFFFFFFF)
            )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "System Settings",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1F1F1F)
                        )
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category: Connectivity
                    Text(
                        text = "Network & Connectivity",
                        color = Color(0xFF64B5F6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    SettingsToggleCard(
                        title = "Wi-Fi",
                        description = if (wifiEnabled) "Connected" else "Disabled",
                        checked = wifiEnabled,
                        onCheckedChange = {
                            wifiEnabled = it
                            wifiManager.setWiFiEnabled(it)
                        }
                    )

                    SettingsToggleCard(
                        title = "Bluetooth",
                        description = if (bluetoothEnabled) "On" else "Off",
                        checked = bluetoothEnabled,
                        onCheckedChange = {
                            bluetoothEnabled = it
                            bluetoothManager.setBluetoothEnabled(it)
                        }
                    )

                    SettingsToggleCard(
                        title = "NFC",
                        description = if (nfcEnabled) "Enabled" else "Disabled",
                        checked = nfcEnabled,
                        onCheckedChange = {
                            nfcEnabled = it
                            nfcManager.setNfcEnabled(it)
                        }
                    )

                    SettingsToggleCard(
                        title = "Airplane Mode",
                        description = if (airplaneModeEnabled) "Active" else "Disabled",
                        checked = airplaneModeEnabled,
                        onCheckedChange = {
                            airplaneModeEnabled = it
                            try {
                                Settings.Global.putInt(
                                    contentResolver,
                                    Settings.Global.AIRPLANE_MODE_ON,
                                    if (it) 1 else 0
                                )
                                // Broadcast intent to update system
                                sendBroadcast(Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                                    putExtra("state", it)
                                })
                            } catch (e: Exception) {
                                Log.e(TAG, "Error toggling airplane mode", e)
                            }
                        }
                    )

                    // Category: Device & Display
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Device & Display",
                        color = Color(0xFF64B5F6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Brightness slider
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Screen Brightness",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${brightnessVal.toInt()}%",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = brightnessVal,
                                onValueChange = {
                                    brightnessVal = it
                                    if (Settings.System.canWrite(this@SettingsActivity)) {
                                        try {
                                            val value = ((it / 100f) * 255f).toInt()
                                            Settings.System.putInt(
                                                contentResolver,
                                                Settings.System.SCREEN_BRIGHTNESS,
                                                value
                                            )
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Failed to write brightness settings", e)
                                        }
                                    }
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF64B5F6),
                                    activeTrackColor = Color(0xFF64B5F6)
                                )
                            )
                        }
                    }

                    SettingsToggleCard(
                        title = "Dark Mode",
                        description = if (darkModeEnabled) "Dark mode active" else "Light mode active",
                        checked = darkModeEnabled,
                        onCheckedChange = {
                            darkModeEnabled = it
                            uiModeManager?.nightMode = if (it) {
                                UiModeManager.MODE_NIGHT_YES
                            } else {
                                UiModeManager.MODE_NIGHT_NO
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun SettingsToggleCard(
        title: String,
        description: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF64B5F6),
                        checkedTrackColor = Color(0xFF1A5F7A)
                    )
                )
            }
        }
    }
}
