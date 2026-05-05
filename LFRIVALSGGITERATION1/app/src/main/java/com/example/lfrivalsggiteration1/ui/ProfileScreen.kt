package com.example.lfrivalsggiteration1.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import coil.compose.AsyncImage
import com.example.lfrivalsggiteration1.ui.theme.RivalsRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: MainViewModel, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    val currentUser     by vm.currentUser.collectAsState()
    val preferences     by vm.userPreferences.collectAsState()
    val myPostHistory   by vm.myPostHistory.collectAsState()
    val acceptedMyPosts by vm.acceptedMyPosts.collectAsState()

    var gamertag        by remember { mutableStateOf("") }
    var discord         by remember { mutableStateOf("") }
    var imageUri        by remember { mutableStateOf<Uri?>(null) }
    var saved           by remember { mutableStateOf(false) }
    var gamertagError   by remember { mutableStateOf(false) }
    var showHistory     by remember { mutableStateOf(false) }
    var showPreferences by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri; saved = false }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            if (gamertag.isEmpty()) gamertag = it.gamertag
            if (discord.isEmpty())  discord  = it.discordHandle
        }
    }

    LaunchedEffect(showHistory) {
        if (showHistory) vm.loadMyPostHistory()
    }

    // ── Preferences bottom sheet ───────────────────────────────────────────
    if (showPreferences) {
        ModalBottomSheet(onDismissRequest = { showPreferences = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Preferences", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode", fontSize = 16.sp)
                    Switch(
                        checked = preferences.darkMode,
                        onCheckedChange = { vm.updateDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RivalsRed,
                            checkedTrackColor = RivalsRed.copy(alpha = 0.5f)
                        )
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Post Notifications", fontSize = 16.sp)
                    Switch(
                        checked = preferences.notificationsEnabled,
                        onCheckedChange = { vm.updateNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RivalsRed,
                            checkedTrackColor = RivalsRed.copy(alpha = 0.5f)
                        )
                    )
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Text Size", fontSize = 16.sp)
                        Text(
                            when {
                                preferences.textSize <= 12f -> "Small"
                                preferences.textSize <= 14f -> "Medium"
                                preferences.textSize <= 17f -> "Large"
                                else                        -> "Extra Large"
                            },
                            fontSize = 14.sp,
                            color = RivalsRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = preferences.textSize,
                        onValueChange = { vm.updateTextSize(it) },
                        valueRange = 11f..20f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor       = RivalsRed,
                            activeTrackColor = RivalsRed
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("A", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("A", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Preview: Welcome to LFRivals!",
                        fontSize = TextUnit(preferences.textSize, TextUnitType.Sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // ── Main screen ────────────────────────────────────────────────────────
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showPreferences = true }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Preferences",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(model = imageUri, contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.AccountCircle, null,
                            Modifier.size(64.dp), tint = Color.Gray)
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = gamertag,
                    onValueChange = { gamertag = it; gamertagError = false; saved = false },
                    label = { Text("Gamertag") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = gamertagError
                )
            }
            item {
                OutlinedTextField(
                    value = discord,
                    onValueChange = { discord = it; saved = false },
                    label = { Text("Discord Handle") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(
                    onClick = {
                        if (gamertag.isBlank()) { gamertagError = true; return@Button }
                        vm.saveProfile(gamertag, discord)
                        saved = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RivalsRed)
                ) { Text("SAVE PROFILE", fontWeight = FontWeight.Bold) }
                if (saved) Text("Profile saved!", color = RivalsRed)
            }

            item {
                Button(
                    onClick = { showHistory = !showHistory },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        if (showHistory) "Hide Post History" else "View Post History",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showHistory) {
                if (myPostHistory.isEmpty()) {
                    item {
                        Text("No posts yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(myPostHistory) { post ->
                        val isExpired   = post.expiresAt < System.currentTimeMillis()
                        val wasAccepted = post.postID in acceptedMyPosts
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    wasAccepted -> Color(0xFF1B5E20).copy(alpha = 0.2f)
                                    isExpired   -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    else        -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(post.hero, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(post.role, fontSize = 13.sp)
                                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(post.rank, fontSize = 13.sp)
                                    }
                                    if (post.content.isNotBlank()) {
                                        Text(post.content, fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        when {
                                            wasAccepted -> "✓ Accepted"
                                            isExpired   -> "Expired"
                                            else        -> "Active"
                                        },
                                        fontSize = 11.sp,
                                        color = when {
                                            wasAccepted -> Color(0xFF4CAF50)
                                            isExpired   -> Color.Gray
                                            else        -> RivalsRed
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(onClick = { vm.deletePost(post.postID) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                                        tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                Button(
                    onClick = { vm.logout { onLogout() } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RivalsRed)
                ) { Text("LOG OUT", fontWeight = FontWeight.Bold) }
            }
        }
    }
}