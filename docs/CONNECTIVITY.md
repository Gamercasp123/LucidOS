# LucidOS Connectivity & Sensors Guide

## Connectivity Services

### ConnectivityService (`packages/services/ConnectivityService/`)

Low-level connectivity management for WiFi, Bluetooth, and NFC.

#### WiFiManager
```kotlin
val wifiManager = WiFiManager(context)
wifiManager.isWiFiEnabled()        // Check status
wifiManager.setWiFiEnabled(true)   // Toggle WiFi
```

**Features:**
- Toggle WiFi on/off
- Check WiFi status
- Android API 23+

#### BluetoothManager
```kotlin
val btManager = BluetoothManager(context)
btManager.isBluetoothEnabled()     // Check status
btManager.setBluetoothEnabled(true) // Toggle Bluetooth
```

**Features:**
- Toggle Bluetooth on/off
- Check Bluetooth status
- Enable/disable operations

### NetworkService (`packages/services/NetworkService/`)

High-level network monitoring and management.

#### NetworkMonitor
Monitor overall network connectivity:
```kotlin
val monitor = NetworkMonitor(context)
val status = monitor.getNetworkStatus()
// NetworkStatus(isConnected, type, isMetered)
```

**Supported Network Types:**
- WiFi
- Cellular
- Ethernet

**Features:**
- Connection status tracking
- Network type detection
- Metered connection detection
- Real-time network info logging

#### WiFiNetworkManager
Advanced WiFi scanning and connection:
```kotlin
val wifiNet = WiFiNetworkManager(context)
wifiNet.startWifiScan()                 // Scan for networks
val networks = wifiNet.getAvailableNetworks() // Get list
val signal = wifiNet.getWiFiSignalStrength()  // Signal level 0-5
```

**Features:**
- WiFi network scanning
- Available networks list
- Signal strength monitoring
- Real-time WiFi info

### SensorService (`packages/services/SensorService/`)

Device sensor management and data collection.

#### DeviceSensorManager
```kotlin
val sensorMgr = DeviceSensorManager(context)
sensorMgr.startListening()

// Get sensor data
val (x, y, z) = sensorMgr.getAccelerometerData()
val proximity = sensorMgr.getProximity()
val light = sensorMgr.getLight()
```

**Supported Sensors:**
- Accelerometer (X, Y, Z axes)
- Proximity sensor
- Light sensor

**Features:**
- Real-time sensor monitoring
- Proximity detection
- Ambient light detection
- Sensor event listening

## Quick Settings Integration

The SystemUI `QuickSettingsManager` integrates all connectivity features:

```kotlin
// WiFi
QuickSettingsManager.toggleWiFi(true)      // Turn on
val enabled = QuickSettingsManager.isWiFiEnabled()

// Bluetooth
QuickSettingsManager.toggleBluetooth(true)
val enabled = QuickSettingsManager.isBluetoothEnabled()

// NFC
QuickSettingsManager.toggleNfc(true)
val enabled = QuickSettingsManager.isNfcEnabled()

// Airplane Mode
QuickSettingsManager.toggleAirplaneMode(true)

// Dark Mode
QuickSettingsManager.toggleDarkMode(true)

// Brightness
QuickSettingsManager.setScreenBrightness(80) // 0-100%
```

## System Integration Points

### SystemUI Status Bar
- WiFi signal strength icon
- Bluetooth connection indicator
- Network type icon
- Signal bars for cellular/WiFi

### Quick Settings Tiles
- WiFi quick toggle
- Bluetooth quick toggle
- NFC quick toggle
- Airplane mode toggle
- Dark mode toggle
- Brightness slider

### Launcher
- Network connectivity status
- Device orientation from accelerometer
- Proximity detection for adaptive UI

### Performance Service
- Network latency monitoring
- Sensor-based power profiling
- Adaptive refresh rate based on light sensor

## Architecture

```
┌─────────────────────────────────────────┐
│         SystemUI Quick Settings         │
│  (WiFi, BT, NFC, Airplane, Dark Mode)  │
└────────────────┬────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
   ┌────▼──────┐   ┌──────▼────┐
   │Connectivity│   │ Network   │
   │  Service   │   │ Service   │
   └────┬──────┘   └──────┬────┘
        │                 │
   ┌────▼──────────┬──────▼────┐
   │ WiFiManager   │ WiFiNet   │
   │ Bluetooth     │ Monitor   │
   │ NFC          │           │
   └───────────────┴───────────┘
```

## Permissions Required

### ConnectivityService
```xml
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

### NetworkService
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
```

### SensorService
```xml
<uses-permission android:name="android.permission.BODY_SENSORS" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Usage Examples

### Check Network Status
```kotlin
val monitor = NetworkMonitor(context)
if (monitor.isConnected()) {
    val status = monitor.getNetworkStatus()
    Log.d("Network", "Connected via ${status.type}")
}
```

### Scan WiFi Networks
```kotlin
val wifiNet = WiFiNetworkManager(context)
wifiNet.startWifiScan()
val networks = wifiNet.getAvailableNetworks()
Log.d("WiFi", "Found ${networks.size} networks")
```

### Monitor Device Orientation
```kotlin
val sensorMgr = DeviceSensorManager(context)
sensorMgr.startListening()
val (x, y, z) = sensorMgr.getAccelerometerData()
Log.d("Accel", "X=$x, Y=$y, Z=$z")
```

### Smart Features Using Sensors

1. **Proximity-based Actions**
   ```kotlin
   if (sensorMgr.isProximityNear()) {
       // Turn off screen during call
   }
   ```

2. **Adaptive Brightness**
   ```kotlin
   val light = sensorMgr.getLight()
   QuickSettingsManager.setScreenBrightness((light / 10).toInt())
   ```

3. **Orientation Detection**
   ```kotlin
   val (x, y, z) = sensorMgr.getAccelerometerData()
   val magnitude = sqrt(x*x + y*y + z*z)
   // Determine device orientation
   ```

## Performance Considerations

- **ConnectivityService**: Minimal CPU impact
- **NetworkService**: ~1% CPU for active monitoring
- **SensorService**: ~2% CPU with UI update rate
- **Battery Impact**: < 0.2% per hour (passive monitoring)

## Future Enhancements

- GPS/Location services
- Cellular network management
- VPN support
- Hotspot management
- 5G detection and handling
- Advanced sensor fusion for better orientation
- ML-based network prediction
