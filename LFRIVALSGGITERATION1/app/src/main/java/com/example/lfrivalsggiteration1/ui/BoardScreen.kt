package com.example.lfrivalsggiteration1.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lfrivalsggiteration1.data.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(vm: MainViewModel, modifier: Modifier = Modifier) {
    val posts           by vm.activePosts.collectAsState()
    val acceptedIds     by vm.acceptedPostIds.collectAsState()
    val acceptedMyPosts by vm.acceptedMyPosts.collectAsState()
    val notification    by vm.inAppNotification.collectAsState()
    val currentUid      = Firebase.auth.currentUser?.uid ?: ""

    var showCreator by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPosts = remember(posts, searchQuery) {
        if (searchQuery.isBlank()) posts
        else posts.filter { post ->
            post.hero.contains(searchQuery, ignoreCase = true) ||
                    post.role.contains(searchQuery, ignoreCase = true) ||
                    post.rank.contains(searchQuery, ignoreCase = true) ||
                    post.content.contains(searchQuery, ignoreCase = true)
        }
    }

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
            ) { Icon(Icons.Default.Add, contentDescription = "Create Post") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── In-app notification banner ─────────────────────────────────
            AnimatedVisibility(
                visible = notification != null,
                enter = slideInVertically() + fadeIn(),
                exit  = slideOutVertically() + fadeOut()
            ) {
                notification?.let { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                        shape  = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(msg, color = Color.White, fontSize = 13.sp,
                                modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { vm.dismissNotification() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss",
                                    tint = Color.White)
                            }
                        }
                    }
                }
            }

            // ── Search bar ─────────────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by hero, role, rank…") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            // ── Post list ──────────────────────────────────────────────────
            if (filteredPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isBlank()) "No active posts. Tap + to create one!"
                        else "No posts match \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPosts, key = { it.postID }) { post ->
                        val wasAcceptedByOthers = post.postID in acceptedMyPosts &&
                                post.uid == currentUid
                        PostCard(
                            post                = post,
                            creatorName         = post.username.ifBlank { "Rival" },
                            isAccepted          = post.postID in acceptedIds,
                            discordHandle       = if (post.postID in acceptedIds) post.discordHandle else null,
                            wasAcceptedByOthers = wasAcceptedByOthers,
                            isOwnPost           = post.uid == currentUid,
                            onAccept            = { vm.acceptPost(post.postID) }
                        )
                    }
                }
            }
        }
    }

    if (showCreator) {
        PostCreatorSheet(
            onDismiss = { showCreator = false },
            onSubmit  = { hero, role, rank, msg ->
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
    wasAcceptedByOthers: Boolean,
    isOwnPost: Boolean = false,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (wasAcceptedByOthers)
                Color(0xFF1B5E20).copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.AccountCircle, null,
                    modifier = Modifier.padding(6.dp), tint = Color.White)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(creatorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold)
                    if (isOwnPost) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("YOU",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onTertiary,
                                fontWeight = FontWeight.Black)
                        }
                    }
                    if (wasAcceptedByOthers) {
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("ACCEPTED",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Hero: ${post.hero}", style = MaterialTheme.typography.bodySmall)
                Text("Rank: ${post.rank}", style = MaterialTheme.typography.bodySmall)
                Text("Role: ${post.role}", style = MaterialTheme.typography.bodySmall)

                if (post.content.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (isAccepted && discordHandle != null) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("Discord: $discordHandle",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            IconButton(
                onClick = onAccept,
                enabled = !isAccepted && !isOwnPost,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = when {
                        isAccepted  -> MaterialTheme.colorScheme.onPrimary
                        isOwnPost   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        else        -> MaterialTheme.colorScheme.onSurface
                    }
                )
            ) {
                when {
                    isAccepted -> Icon(Icons.Default.CheckCircle, "Accepted")
                    isOwnPost  -> Icon(Icons.Default.AccountCircle, "Your Post")
                    else       -> Icon(Icons.Default.Add, "Accept")
                }
            }
        }
    }
}

val HEROES = listOf("Black Panther", "Black Widow", "Captain America", "Doctor Strange",
    "Groot", "Hawkeye", "Hela", "Hulk", "Iron Fist", "Iron Man", "Luna Snow", "Magneto",
    "Mantis", "Moon Knight", "Namor", "Psylocke", "Punisher", "Scarlet Witch", "Spider-Man",
    "Star-Lord", "Storm", "Thor", "Venom", "Wolverine")
val ROLES  = listOf("Duelist", "Vanguard", "Strategist")
val RANKS  = listOf("Bronze", "Silver", "Gold", "Platinum", "Diamond", "Grandmaster",
    "Celestial", "Eternity", "One Above All")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreatorSheet(
    onDismiss: () -> Unit,
    onSubmit: (hero: String, role: String, rank: String, message: String) -> Unit
) {
    var hero    by remember { mutableStateOf("") }
    var role    by remember { mutableStateOf("") }
    var rank    by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var error   by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create LFG Post",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            LFGDropdown("Hero", hero, HEROES) { hero = it; error = false }
            LFGDropdown("Role", role, ROLES)  { role = it; error = false }
            LFGDropdown("Rank", rank, RANKS)  { rank = it; error = false }
            OutlinedTextField(
                value = message,
                onValueChange = { if (it.length <= 120) message = it },
                label = { Text("Message (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                supportingText = { Text("${message.length}/120") }
            )
            if (error) Text("Please select Hero, Role, and Rank",
                color = MaterialTheme.colorScheme.error)
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
fun LFGDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
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