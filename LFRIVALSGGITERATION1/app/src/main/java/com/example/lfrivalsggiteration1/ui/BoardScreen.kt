package com.example.lfrivalsggiteration1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lfrivalsggiteration1.data.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val posts by vm.activePosts.observeAsState(emptyList())
    val acceptedIds by vm.acceptedPostIds.collectAsState()
    val currentUser by vm.currentUser.observeAsState()

    var showCreator by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("LFG Board", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreator = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { padding ->
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No active posts. Tap + to create one!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.postID }) { post ->
                    // In a real app, you'd fetch the creator's gamertag via a SQL Join.
                    // For now, we use the current user's tag if they are the creator,
                    // otherwise a placeholder.
                    val displayName = if (post.userID == currentUser?.userID) {
                        currentUser?.gamertag ?: "You"
                    } else {
                        "Rival#${post.userID}"
                    }

                    PostCard(
                        post = post,
                        creatorName = displayName,
                        isAccepted = post.postID in acceptedIds,
                        discordHandle = if (post.postID in acceptedIds) currentUser?.discordHandle else null,
                        onAccept = { vm.acceptPost(post.postID) }
                    )
                }
            }
        }
    }

    if (showCreator) {
        PostCreatorSheet(
            onDismiss = { showCreator = false },
            onSubmit = { hero, role, rank, msg ->
                vm.createPost(hero, role, rank, msg)
                showCreator = false
            }
        )
    }
}

@Composable
fun PostCard(
    post: Post,
    creatorName: String,
    isAccepted: Boolean,
    discordHandle: String?,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Profile Icon Circle
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(16.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = creatorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(4.dp))

                Text(text = "Hero: ${post.hero}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Rank: ${post.rank}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Role: ${post.role}", style = MaterialTheme.typography.bodySmall)

                if (post.content.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isAccepted && discordHandle != null) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Discord: $discordHandle",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Action Button (Accept)
            IconButton(
                onClick = onAccept,
                enabled = !isAccepted,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isAccepted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) {
                if (isAccepted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Accepted")
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Accept")
                }
            }
        }
    }
}

// --- Logic Constants ---
val HEROES = listOf("Black Panther", "Black Widow", "Captain America", "Doctor Strange", "Groot", "Hawkeye", "Hela", "Hulk", "Iron Fist", "Iron Man", "Luna Snow", "Magneto", "Mantis", "Moon Knight", "Namor", "Psylocke", "Punisher", "Scarlet Witch", "Spider-Man", "Star-Lord", "Storm", "Thor", "Venom", "Wolverine")
val ROLES = listOf("Duelist", "Vanguard", "Strategist")
val RANKS = listOf("Bronze", "Silver", "Gold", "Platinum", "Diamond", "Grandmaster", "Celestial")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreatorSheet(
    onDismiss: () -> Unit,
    onSubmit: (hero: String, role: String, rank: String, message: String) -> Unit
) {
    var hero by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var rank by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create LFG Post", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            LFGDropdown("Hero", hero, HEROES) { hero = it; error = false }
            LFGDropdown("Role", role, ROLES) { role = it; error = false }
            LFGDropdown("Rank", rank, RANKS) { rank = it; error = false }

            OutlinedTextField(
                value = message,
                onValueChange = { if (it.length <= 120) message = it },
                label = { Text("Message (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                supportingText = { Text("${message.length}/120") }
            )

            if (error) Text("Please select Hero, Role, and Rank", color = MaterialTheme.colorScheme.error)

            AppButton(
                onClick = {
                    if (hero.isBlank() || role.isBlank() || rank.isBlank()) {
                        error = true; return@AppButton
                    }
                    onSubmit(hero, role, rank, message)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Post") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LFGDropdown(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
