package com.example.lfrivalsggiteration1.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage // Ensure implementation("io.coil-kt:coil-compose:2.6.0") is in build.gradle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val currentUser by vm.currentUser.observeAsState()

    var gamertag by remember { mutableStateOf("") }
    var discord  by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var saved    by remember { mutableStateOf(false) }
    var gamertagError by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri; saved = false }

    LaunchedEffect(currentUser) {
        if (currentUser != null && gamertag.isEmpty()) {
            gamertag = currentUser!!.gamertag
            discord  = currentUser!!.discordHandle
        }
    }

    Scaffold(
        modifier = modifier,
        topBar   = { TopAppBar(title = { Text("Profile") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Image Picker
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.AccountCircle, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("Tap to change picture", style = MaterialTheme.typography.labelSmall)

            Spacer(Modifier.height(8.dp))
            Text("Your Info", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = gamertag,
                onValueChange = { gamertag = it; gamertagError = false; saved = false },
                label = { Text("Gamertag") },
                modifier = Modifier.fillMaxWidth(),
                isError = gamertagError,
                supportingText = if (gamertagError) ({ Text("Gamertag is required") }) else null,
                singleLine = true
            )

            OutlinedTextField(
                value = discord,
                onValueChange = { discord = it; saved = false },
                label = { Text("Discord Handle") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    if (gamertag.isBlank()) { gamertagError = true; return@Button }
                    vm.saveProfile(gamertag, discord)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Profile") }

            if (saved) Text("Profile saved!", color = MaterialTheme.colorScheme.primary)
        }
    }
}

