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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.lfrivalsggiteration1.ui.theme.RivalsRed

@Composable
fun ProfileScreen(vm: MainViewModel, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    // observeAsState requires the 'androidx.compose.runtime.getValue' import
    val currentUser by vm.currentUser.observeAsState()

    var gamertag by remember { mutableStateOf("") }
    var discord  by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var saved    by remember { mutableStateOf(false) }
    var gamertagError by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        saved = false
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            if (gamertag.isEmpty()) gamertag = it.gamertag
            if (discord.isEmpty()) discord = it.discordHandle
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color.White).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFEEEEEE)).clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.AccountCircle, null, Modifier.size(64.dp), tint = Color.Gray)
            }
        }

        OutlinedTextField(
            value = gamertag,
            onValueChange = { gamertag = it; gamertagError = false; saved = false },
            label = { Text("Gamertag") },
            modifier = Modifier.fillMaxWidth(),
            isError = gamertagError
        )

        OutlinedTextField(
            value = discord,
            onValueChange = { discord = it; saved = false },
            label = { Text("Discord Handle") },
            modifier = Modifier.fillMaxWidth()
        )

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

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { vm.logout { onLogout() } },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RivalsRed)
        ) { Text("LOG OUT", fontWeight = FontWeight.Bold) }
    }
}