package com.example.lfrivalsggiteration1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.lfrivalsggiteration1.data.MetaHeroStat

private val RivalsRed = Color(0xFFD32F2F)
private val WinGreen  = Color(0xFF4CAF50)
private val BanOrange = Color(0xFFFF9800)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val stats by vm.metaStats.collectAsState()

    var sortBy by remember { mutableStateOf("WIN") }
    val sorted = remember(stats, sortBy) {
        when (sortBy) {
            "WIN"  -> stats.sortedByDescending { it.winRate }
            "PICK" -> stats.sortedByDescending { it.pickRate }
            "BAN"  -> stats.sortedByDescending { it.banRate }
            else   -> stats
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Hero Stats", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { vm.fetchStats() }, enabled = !vm.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Source banner
            Surface(
                color = RivalsRed.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Season 7.5 stats — source: metabot.gg (Apr 24, 2026) & beebom.com",
                    fontSize = 11.sp,
                    color = RivalsRed,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Sort tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("WIN" to "Win Rate", "PICK" to "Pick Rate", "BAN" to "Ban Rate").forEach { (key, label) ->
                    TextButton(
                        onClick = { sortBy = key },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (sortBy == key) RivalsRed
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = if (sortBy == key) FontWeight.Black else FontWeight.Normal
                            )
                            if (sortBy == key) {
                                Box(Modifier.height(2.dp).width(56.dp).background(RivalsRed))
                            }
                        }
                    }
                }
            }

            // Column headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("#",     Modifier.width(28.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("HERO",  Modifier.weight(2f),   fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("WIN%",  Modifier.weight(1f),   fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("PICK%", Modifier.weight(1f),   fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("BAN%",  Modifier.weight(1f),   fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Box(modifier = Modifier.fillMaxSize()) {
                if (stats.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No data", color = MaterialTheme.colorScheme.onBackground)
                        Button(
                            onClick = { vm.fetchStats() },
                            colors = ButtonDefaults.buttonColors(containerColor = RivalsRed)
                        ) { Text("Retry") }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(sorted.size) { index ->
                            MetaHeroRow(rank = index + 1, hero = sorted[index], sortBy = sortBy)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterRow(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                Button(
                    onClick = { onSelect(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected == option) RivalsRed else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selected == option) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
fun MetaHeroRow(rank: Int, hero: MetaHeroStat, sortBy: String) {
    val roleColor = when (hero.role) {
        "Vanguard"   -> Color(0xFF2196F3)
        "Duelist"    -> RivalsRed
        "Strategist" -> WinGreen
        else         -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$rank", modifier = Modifier.width(28.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Column(modifier = Modifier.weight(2f)) {
            Text(hero.heroName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
            Surface(color = roleColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                Text(
                    hero.role.uppercase(),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    fontSize = 9.sp, color = roleColor, fontWeight = FontWeight.Black
                )
            }
        }

        Text(
            "${"%.1f".format(hero.winRate)}%",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = if (sortBy == "WIN") FontWeight.Black else FontWeight.Normal,
            color = when {
                hero.winRate >= 55f -> WinGreen
                hero.winRate >= 50f -> MaterialTheme.colorScheme.onBackground
                else -> RivalsRed
            }
        )

        Text(
            "${"%.1f".format(hero.pickRate)}%",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = if (sortBy == "PICK") FontWeight.Black else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            "${"%.1f".format(hero.banRate)}%",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            fontWeight = if (sortBy == "BAN") FontWeight.Black else FontWeight.Normal,
            color = if (hero.banRate >= 15f) BanOrange else MaterialTheme.colorScheme.onBackground
        )
    }
}