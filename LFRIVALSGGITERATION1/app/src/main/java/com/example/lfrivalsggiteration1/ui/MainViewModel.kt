package com.example.lfrivalsggiteration1.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lfrivalsggiteration1.data.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _activePosts = MutableStateFlow<List<Post>>(emptyList())
    val activePosts: StateFlow<List<Post>> = _activePosts

    private val _acceptedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val acceptedPostIds: StateFlow<Set<String>> = _acceptedPostIds

    private val _myPostHistory = MutableStateFlow<List<Post>>(emptyList())
    val myPostHistory: StateFlow<List<Post>> = _myPostHistory

    private val _acceptedMyPosts = MutableStateFlow<Set<String>>(emptySet())
    val acceptedMyPosts: StateFlow<Set<String>> = _acceptedMyPosts

    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences

    private val _inAppNotification = MutableStateFlow<String?>(null)
    val inAppNotification: StateFlow<String?> = _inAppNotification

    private val _metaStats = MutableStateFlow<List<MetaHeroStat>>(emptyList())
    val metaStats: StateFlow<List<MetaHeroStat>> = _metaStats

    var statsError       by mutableStateOf("")
    var isLoading        by mutableStateOf(false)
    var isLoadingPlayer  by mutableStateOf(false)
    var playerStatsError by mutableStateOf("")

    private val _playerStats = MutableStateFlow<PlayerStatsResponse?>(null)
    val playerStats: StateFlow<PlayerStatsResponse?> = _playerStats

    private var isFirstLoad = true

    init {
        fetchStats()
        listenToPosts()
        auth.currentUser?.let {
            viewModelScope.launch {
                val local = withContext(Dispatchers.IO) { db.userDao().getUserOnce() }
                _currentUser.value = local
                loadUserPreferences()
                listenToMyPostAcceptances()
            }
        }
    }

    private fun listenToPosts() {
        val now = System.currentTimeMillis()
        firestore.collection("posts")
            .whereGreaterThan("expiresAt", now)
            .orderBy("expiresAt")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firestore", "Posts listener failed", error)
                    return@addSnapshotListener
                }
                val posts = snapshots?.documents?.mapNotNull { doc ->
                    try {
                        Post(
                            postID    = doc.id,
                            uid       = doc.getString("uid") ?: "",
                            userID    = (doc.getLong("userID") ?: 0L).toInt(),
                            username  = doc.getString("username") ?: "",
                            discordHandle = doc.getString("discordHandle") ?: "",
                            hero      = doc.getString("hero") ?: "",
                            role      = doc.getString("role") ?: "",
                            rank      = doc.getString("rank") ?: "",
                            content   = doc.getString("content") ?: "",
                            expiresAt = doc.getLong("expiresAt") ?: 0L
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()

                if (!isFirstLoad && posts.size > _activePosts.value.size) {
                    val newPost = posts.firstOrNull { p ->
                        _activePosts.value.none { it.postID == p.postID }
                    }
                    if (newPost != null && _userPreferences.value.notificationsEnabled) {
                        _inAppNotification.value =
                            "New LFG: ${newPost.username} looking for group as ${newPost.hero}!"
                    }
                }
                isFirstLoad = false
                _activePosts.value = posts
            }
    }

    fun dismissNotification() { _inAppNotification.value = null }

    private fun listenToMyPostAcceptances() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("posts")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener
                val acceptedIds = snapshots?.documents
                    ?.filter { (it.getLong("acceptCount") ?: 0L) > 0L }
                    ?.map { it.id }
                    ?.toSet() ?: emptySet()
                _acceptedMyPosts.value = acceptedIds
            }
    }

    fun loadMyPostHistory() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("posts")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { snapshots ->
                _myPostHistory.value = snapshots.documents.mapNotNull { doc ->
                    try {
                        Post(
                            postID    = doc.id,
                            uid       = doc.getString("uid") ?: "",
                            userID    = (doc.getLong("userID") ?: 0L).toInt(),
                            discordHandle = doc.getString("discordHandle") ?: "",
                            username  = doc.getString("username") ?: "",
                            hero      = doc.getString("hero") ?: "",
                            role      = doc.getString("role") ?: "",
                            rank      = doc.getString("rank") ?: "",
                            content   = doc.getString("content") ?: "",
                            expiresAt = doc.getLong("expiresAt") ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
            }
    }

    fun deletePost(postID: String) {
        firestore.collection("posts").document(postID).delete()
            .addOnSuccessListener {
                _myPostHistory.value = _myPostHistory.value.filter { it.postID != postID }
                _activePosts.value   = _activePosts.value.filter   { it.postID != postID }
            }
    }

    private fun loadUserPreferences() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null) {
                    _userPreferences.value = UserPreferences(
                        darkMode             = doc.getBoolean("darkMode") ?: false,
                        notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true
                    )
                }
            }
    }

    fun updateDarkMode(enabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).update("darkMode", enabled)
        _userPreferences.value = _userPreferences.value.copy(darkMode = enabled)
    }

    fun updateNotifications(enabled: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).update("notificationsEnabled", enabled)
        _userPreferences.value = _userPreferences.value.copy(notificationsEnabled = enabled)
        if (enabled) Firebase.messaging.subscribeToTopic("new_posts")
        else Firebase.messaging.unsubscribeFromTopic("new_posts")
    }

    fun acceptPost(postID: String) {
        _acceptedPostIds.value = _acceptedPostIds.value + postID
        firestore.collection("posts").document(postID)
            .update("acceptCount", FieldValue.increment(1))
    }

    fun createPost(hero: String, role: String, rank: String, message: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val uid  = auth.currentUser?.uid ?: return@launch
            firestore.collection("posts").add(
                hashMapOf(
                    "uid"         to uid,
                    "userID"      to user.userID,
                    "username"    to user.gamertag,
                    "discordHandle" to user.discordHandle,
                    "hero"        to hero,
                    "role"        to role,
                    "rank"        to rank,
                    "content"     to message,
                    "acceptCount" to 0,
                    "expiresAt"   to (System.currentTimeMillis() + 30 * 60 * 1000L)
                )
            ).addOnFailureListener { Log.e("Firestore", "Failed to create post", it) }
        }
    }

    fun isRememberMeEnabled(): Boolean = prefs.getBoolean("remember_me", false)
    fun getSavedUsername(): String = prefs.getString("saved_username", "") ?: ""
    fun setRememberMe(username: String, remember: Boolean) {
        prefs.edit()
            .putBoolean("remember_me", remember)
            .putString("saved_username", username)
            .apply()
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        val email = "${username.trim().lowercase()}@lfrivals.app"
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                var local = withContext(Dispatchers.IO) { db.userDao().getUserOnce() }
                if (local == null || local.username != username) {
                    val uid = auth.currentUser?.uid ?: ""
                    val doc = firestore.collection("users").document(uid).get().await()
                    val gamertag = doc.getString("gamertag") ?: username
                    val discord  = doc.getString("discordHandle") ?: ""
                    withContext(Dispatchers.IO) {
                        db.userDao().insertUser(
                            User(gamertag = gamertag, discordHandle = discord,
                                username = username, password = "")
                        )
                    }
                    local = withContext(Dispatchers.IO) { db.userDao().getUserOnce() }
                }
                _currentUser.value = local
                loadUserPreferences()
                listenToMyPostAcceptances()
                onResult(true)
            } catch (e: Exception) {
                Log.e("Auth", "Login failed: ${e.localizedMessage}")
                onResult(false)
            }
        }
    }

    fun signUp(username: String, password: String, onResult: (Boolean) -> Unit) {
        val email = "${username.trim().lowercase()}@lfrivals.app"
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No UID")
                firestore.collection("users").document(uid).set(
                    mapOf(
                        "username"             to username,
                        "gamertag"             to username,
                        "discordHandle"        to "",
                        "darkMode"             to false,
                        "notificationsEnabled" to true
                    )
                ).await()
                Firebase.messaging.subscribeToTopic("new_posts").await()
                withContext(Dispatchers.IO) {
                    db.userDao().insertUser(
                        User(gamertag = username, discordHandle = "",
                            username = username, password = "")
                    )
                }
                _currentUser.value = withContext(Dispatchers.IO) { db.userDao().getUserOnce() }
                loadUserPreferences()
                listenToMyPostAcceptances()
                onResult(true)
            } catch (e: Exception) {
                Log.e("Auth", "Sign up failed: ${e.localizedMessage}")
                onResult(false)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        auth.signOut()
        prefs.edit().putBoolean("remember_me", false).apply()
        _playerStats.value     = null
        playerStatsError       = ""
        _currentUser.value     = null
        _myPostHistory.value   = emptyList()
        _userPreferences.value = UserPreferences()
        onComplete()
    }

    fun saveProfile(gamertag: String, discord: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val existing = db.userDao().getUserOnce()
                if (existing != null) {
                    db.userDao().updateUser(existing.copy(gamertag = gamertag, discordHandle = discord))
                } else {
                    db.userDao().insertUser(User(gamertag = gamertag, discordHandle = discord))
                }
            }
            auth.currentUser?.uid?.let { uid ->
                firestore.collection("users").document(uid).update(
                    mapOf("gamertag" to gamertag, "discordHandle" to discord)
                )
            }
            _currentUser.value = withContext(Dispatchers.IO) { db.userDao().getUserOnce() }
        }
    }

    fun fetchStats() = viewModelScope.launch {
        isLoading = true
        statsError = ""
        try {
            _metaStats.value = listOf(
                MetaHeroStat("Peni Parker",        "Vanguard",   59.6f, 1.0f,  14.2f),
                MetaHeroStat("Daredevil",          "Duelist",    57.7f, 2.0f,  7.3f),
                MetaHeroStat("Storm",              "Duelist",    56.9f, 0.7f,  8.9f),
                MetaHeroStat("Magik",              "Duelist",    55.8f, 1.6f,  9.7f),
                MetaHeroStat("Mantis",             "Strategist", 55.3f, 1.1f,  5.2f),
                MetaHeroStat("Hulk",               "Vanguard",   55.2f, 1.8f,  5.1f),
                MetaHeroStat("Ultron",             "Strategist", 55.2f, 0.7f,  11.2f),
                MetaHeroStat("Rocket Raccoon",     "Strategist", 55.1f, 2.9f,  8.4f),
                MetaHeroStat("Squirrel Girl",      "Duelist",    54.7f, 1.1f,  3.2f),
                MetaHeroStat("Captain America",    "Vanguard",   54.4f, 0.9f,  3.2f),
                MetaHeroStat("Angela",             "Vanguard",   54.2f, 1.2f,  4.1f),
                MetaHeroStat("Groot",              "Vanguard",   54.0f, 1.7f,  16.1f),
                MetaHeroStat("Loki",               "Strategist", 54.0f, 1.9f,  4.7f),
                MetaHeroStat("Adam Warlock",       "Strategist", 53.7f, 1.1f,  4.7f),
                MetaHeroStat("Rogue",              "Vanguard",   53.6f, 1.4f,  6.8f),
                MetaHeroStat("Elsa Bloodstone",    "Duelist",    53.1f, 4.5f,  19.4f),
                MetaHeroStat("Jeff The Land Shark","Strategist", 53.1f, 3.6f,  9.8f),
                MetaHeroStat("Invisible Woman",    "Strategist", 53.0f, 8.0f,  12.4f),
                MetaHeroStat("Star-Lord",          "Duelist",    53.0f, 1.5f,  2.1f),
                MetaHeroStat("Human Torch",        "Duelist",    52.9f, 0.4f,  1.1f),
                MetaHeroStat("Venom",              "Vanguard",   52.8f, 1.7f,  6.4f),
                MetaHeroStat("Iron Man",           "Duelist",    52.8f, 0.9f,  5.1f),
                MetaHeroStat("Gambit",             "Strategist", 52.6f, 2.5f,  28.1f),
                MetaHeroStat("Psylocke",           "Duelist",    52.5f, 1.5f,  5.9f),
                MetaHeroStat("Doctor Strange",     "Vanguard",   52.5f, 4.2f,  3.8f),
                MetaHeroStat("Hela",               "Duelist",    52.0f, 1.8f,  6.1f),
                MetaHeroStat("Iron Fist",          "Duelist",    51.9f, 0.9f,  3.5f),
                MetaHeroStat("Moon Knight",        "Duelist",    51.5f, 2.5f,  1.8f),
                MetaHeroStat("Thor",               "Vanguard",   51.5f, 2.0f,  4.8f),
                MetaHeroStat("Mister Fantastic",   "Duelist",    51.5f, 0.5f,  1.7f),
                MetaHeroStat("Deadpool",           "Duelist",    51.5f, 5.3f,  6.2f),
                MetaHeroStat("Phoenix",            "Duelist",    51.5f, 2.4f,  5.6f),
                MetaHeroStat("Spider-Man",         "Duelist",    51.2f, 2.8f,  7.2f),
                MetaHeroStat("Magneto",            "Vanguard",   51.2f, 4.3f,  5.3f),
                MetaHeroStat("Namor",              "Duelist",    51.0f, 2.4f,  2.4f),
                MetaHeroStat("Black Panther",      "Duelist",    50.8f, 1.1f,  4.8f),
                MetaHeroStat("Cloak & Dagger",     "Strategist", 50.7f, 5.0f,  15.6f),
                MetaHeroStat("Scarlet Witch",      "Duelist",    49.9f, 1.2f,  3.6f),
                MetaHeroStat("Blade",              "Duelist",    49.8f, 0.8f,  5.8f),
                MetaHeroStat("Hawkeye",            "Duelist",    49.1f, 0.8f,  2.9f),
                MetaHeroStat("Wolverine",          "Duelist",    48.8f, 1.0f,  7.1f),
                MetaHeroStat("The Thing",          "Vanguard",   48.5f, 1.4f,  2.9f),
                MetaHeroStat("Emma Frost",         "Vanguard",   47.6f, 2.1f,  2.3f),
                MetaHeroStat("Punisher",           "Duelist",    46.9f, 1.9f,  2.8f),
                MetaHeroStat("Luna Snow",          "Strategist", 45.9f, 2.5f,  9.8f)
            )
        } finally {
            isLoading = false
        }
    }

    fun fetchPlayerStats(query: String) = viewModelScope.launch {
        if (query.isBlank()) { playerStatsError = "Enter your UID or in-game name"; return@launch }
        isLoadingPlayer = true
        playerStatsError = ""
        _playerStats.value = null
        val apiKey = "d5c57e773061d9793e5f031f2e4aacbd871cd7025aad6c3e96ce2ec6cebc361f"
        try {
            val uidToUse = try {
                val found = withContext(Dispatchers.IO) { RetrofitClient.api.findPlayer(apiKey, query) }
                found.uid.ifBlank { query }
            } catch (e: Exception) { query }
            _playerStats.value = withContext(Dispatchers.IO) {
                RetrofitClient.api.getPlayerStats(apiKey, uidToUse)
            }
        } catch (e: Exception) {
            try {
                withContext(Dispatchers.IO) { RetrofitClient.api.updatePlayer(apiKey, query) }
                playerStatsError = "Player is being indexed. Try again in 1-2 minutes."
            } catch (e2: Exception) {
                playerStatsError = "Personal stats require a premium Marvel Rivals API subscription."
            }
        } finally {
            isLoadingPlayer = false
        }
    }
}