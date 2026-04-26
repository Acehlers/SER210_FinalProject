package com.example.lfrivalsggiteration1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lfrivalsggiteration1.data.HeroStatResponse

private val RivalsRed = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val stats by vm.heroStats.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Hero Stats", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { vm.fetchStats() }, enabled = !vm.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh stats")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)
        ) {
            // Filter Section
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterRow("PLATFORM:", listOf("PC", "Console"), vm.selectedPlatform) { vm.updatePlatform(it) }
                FilterRow("MODE:", listOf("QuickPlay", "Competitive"), vm.selectedMode) { vm.updateMode(it) }
            }

            // Table Header
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("HERO",    Modifier.weight(2f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Text("WINS",    Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                Text("MATCHES", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    vm.isLoading && stats.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = RivalsRed)
                    }
                    !vm.isLoading && stats.isEmpty() -> {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No stats loaded", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { vm.fetchStats() }, colors = ButtonDefaults.buttonColors(containerColor = RivalsRed)) {
                                Text("Retry")
                            }
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(stats) { hero ->
                                HeroStatItem(hero)
                                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // Thin progress bar when refreshing existing data
                if (vm.isLoading && stats.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                        color = RivalsRed
                    )
                }
            }
        }
    }
}

@Composable
fun FilterRow(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                Button(
                    onClick = { onSelect(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == option) RivalsRed else Color(0xFFE0E0E0),
                        contentColor = if (selected == option) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(option.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun HeroStatItem(hero: HeroStatResponse) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(2f)) {
            Text(hero.heroName.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
            Text("PLAYTIME: ${hero.playTime}", fontSize = 11.sp, color = RivalsRed)
        }
        Text("${hero.wins}",    Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Color.Black)
        Text("${hero.matches}", Modifier.weight(1f), color = Color.Gray)
    }
}