package com.example.lfrivalsggiteration1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val currentUser by vm.currentUser.observeAsState()

    var gamertag by remember { mutableStateOf("") }
    var discord  by remember { mutableStateOf("") }
    var saved    by remember { mutableStateOf(false) }
    var gamertagError by remember { mutableStateOf(false) }

    // Pre-fill fields once the user loads from DB
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Your Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value         = gamertag,
                onValueChange = { gamertag = it; gamertagError = false; saved = false },
                label         = { Text("Gamertag") },
                modifier      = Modifier.fillMaxWidth(),
                isError       = gamertagError,
                supportingText = if (gamertagError) ({ Text("Gamertag is required") }) else null,
                singleLine    = true
            )

            OutlinedTextField(
                value         = discord,
                onValueChange = { discord = it; saved = false },
                label         = { Text("Discord Handle") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            AppButton(
                onClick = {
                    if (gamertag.isBlank()) { gamertagError = true; return@AppButton }
                    vm.saveProfile(gamertag, discord)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Profile") }

            if (saved) {
                Text("Profile saved!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}