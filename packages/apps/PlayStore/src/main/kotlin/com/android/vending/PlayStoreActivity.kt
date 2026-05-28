package com.android.vending

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PlayStoreActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayStoreApp()
        }
    }
}

data class AppItem(val name: String, val rating: String, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayStoreApp() {
    var searchQuery by remember { mutableStateOf("") }
    val featuredApps = listOf(
        AppItem("Lucid Games", "4.8 ★", "Games"),
        AppItem("SocialSpace", "4.4 ★", "Social"),
        AppItem("FlowMusic", "4.6 ★", "Entertainment"),
        AppItem("Tasky", "4.7 ★", "Productivity")
    )
    val topCharts = listOf(
        AppItem("WhatsApp Messenger", "4.3 ★", "Communication"),
        AppItem("Instagram", "4.2 ★", "Social"),
        AppItem("TikTok", "4.5 ★", "Entertainment"),
        AppItem("Spotify: Music and Podcasts", "4.4 ★", "Music")
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF0F9D58), // Google Play green
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onSurface = Color.White
        )
    ) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(Color(0xFF1E1E1E))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Google Play",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search for apps & games") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0F9D58),
                            unfocusedContainerColor = Color(0xFF2A2A2A),
                            focusedContainerColor = Color(0xFF2A2A2A)
                        )
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Section: Featured Apps
                Column {
                    Text(
                        text = "Featured Apps",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(featuredApps) { app ->
                            AppCard(app)
                        }
                    }
                }

                // Section: Top Charts
                Column {
                    Text(
                        text = "Top Charts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    topCharts.forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.Gray,
                                fontSize = 16.sp,
                                modifier = Modifier.width(32.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.name,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${app.category} • ${app.rating}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Button(
                                onClick = { /* Download action */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                            ) {
                                Text("Install", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppCard(app: AppItem) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFF2E2E2E), shape = RoundedCornerShape(16.dp))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = app.rating,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
