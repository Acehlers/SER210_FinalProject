package com.example.lfrivalsggiteration1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lfrivalsggiteration1.data.PlayerHeroStat
import kotlin.math.roundToInt

private val Red = Color(0xFFD32F2F)
private val Green = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStatsScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val currentUser by vm.currentUser.collectAsState()
    val playerStats by vm.playerStats.collectAsState()

    // Pre-fill gamertag from saved profile
    var searchGamertag by remember(currentUser) {
        mutableStateOf(currentUser?.gamertag ?: "")
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("My Stats", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(12.dp))

            // ── Search bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchGamertag,
                    onValueChange = { searchGamertag = it },
                    placeholder = { Text("Enter your in-game name…") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Red,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                Button(
                    onClick = { vm.fetchPlayerStats(searchGamertag) },
                    enabled = !vm.isLoadingPlayer,
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    if (vm.isLoadingPlayer) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }

            // ── Hint text ─────────────────────────────────────────────────────
            Text(
                "Use your exact Marvel Rivals in-game name",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // ── Error ─────────────────────────────────────────────────────────
            if (vm.playerStatsError.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        vm.playerStatsError,
                        color = Red,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Results ───────────────────────────────────────────────────────
            playerStats?.let { stats ->
                // Player summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                stats.player.name.ifBlank { stats.name },
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Level ${stats.player.level}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = Red,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                stats.player.rank.rank,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Hero stats table header
                if (stats.player.heroStats.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("HERO",    Modifier.weight(2f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("WIN%",    Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("MATCHES", Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("K/D",     Modifier.weight(1f), fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(Modifier.height(4.dp))

                    val sortedHeroes = stats.player.heroStats.sortedByDescending { it.matches }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(sortedHeroes) { hero ->
                            PersonalHeroRow(hero)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }
                    }
                } else {
                    // Player found but no hero data
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No hero data found for this player yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            } ?: run {
                // Nothing loaded yet — show empty state
                if (!vm.isLoadingPlayer) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Enter your gamertag to load your stats",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalHeroRow(hero: PlayerHeroStat) {
    val winRate = if (hero.matches > 0) (hero.wins.toFloat() / hero.matches * 100).roundToInt() else 0
    val kd = if (hero.deaths > 0) String.format("%.2f", hero.kills / hero.deaths) else "∞"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                hero.heroName.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(hero.playTime, fontSize = 10.sp, color = Red)
        }
        Text(
            "$winRate%",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = when {
                winRate >= 55 -> Green
                winRate >= 50 -> MaterialTheme.colorScheme.onBackground
                else -> Red
            }
        )
        Text(
            "${hero.matches}",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            kd,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}