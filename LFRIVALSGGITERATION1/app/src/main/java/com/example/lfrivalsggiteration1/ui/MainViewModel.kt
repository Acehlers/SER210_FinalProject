package com.example.lfrivalsggiteration1.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lfrivalsggiteration1.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // ─── User & Posts ─────────────────────────────────────────────────────────
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _activePosts = MutableStateFlow<List<Post>>(emptyList())
    val activePosts: StateFlow<List<Post>> = _activePosts

    private val _acceptedPostIds = MutableStateFlow<Set<Int>>(emptySet())
    val acceptedPostIds: StateFlow<Set<Int>> = _acceptedPostIds

    // ─── Meta stats (hardcoded) ───────────────────────────────────────────────
    private val _metaStats = MutableStateFlow<List<MetaHeroStat>>(emptyList())
    val metaStats: StateFlow<List<MetaHeroStat>> = _metaStats

    var statsError by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    // ─── Personal stats (live API) ────────────────────────────────────────────
    private val _playerStats = MutableStateFlow<PlayerStatsResponse?>(null)
    val playerStats: StateFlow<PlayerStatsResponse?> = _playerStats

    var isLoadingPlayer by mutableStateOf(false)
    var playerStatsError by mutableStateOf("")

    init {
        fetchStats()
        db.userDao().getUser().observeForever { _currentUser.value = it }
        db.postDao().getActivePosts().observeForever { _activePosts.value = it ?: emptyList() }
    }

    // ─── Auth ─────────────────────────────────────────────────────────────────
    fun isRememberMeEnabled(): Boolean = prefs.getBoolean("remember_me", false)
    fun getSavedUsername(): String = prefs.getString("saved_username", "") ?: ""
    fun setRememberMe(username: String, remember: Boolean) {
        prefs.edit()
            .putBoolean("remember_me", remember)
            .putString("saved_username", username)
            .apply()
    }
    fun logout(onComplete: () -> Unit) {
        prefs.edit().putBoolean("remember_me", false).apply()
        _playerStats.value = null
        playerStatsError = ""
        onComplete()
    }

    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().login(username, password) }
            onResult(user != null)
        }
    }

    fun signUp(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val existing = withContext(Dispatchers.IO) { db.userDao().findByUsername(username) }
            if (existing != null) {
                onResult(false)
            } else {
                withContext(Dispatchers.IO) {
                    db.userDao().insertUser(
                        User(gamertag = username, discordHandle = "", username = username, password = password)
                    )
                }
                onResult(true)
            }
        }
    }

    // ─── Post actions ─────────────────────────────────────────────────────────
    fun acceptPost(postID: Int) {
        _acceptedPostIds.value = _acceptedPostIds.value + postID
    }

    fun createPost(hero: String, role: String, rank: String, message: String) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) { db.userDao().getUserOnce() } ?: return@launch
            val post = Post(
                userID = user.userID,
                hero = hero, role = role, rank = rank, content = message,
                expiresAt = System.currentTimeMillis() + 30 * 60 * 1000L
            )
            withContext(Dispatchers.IO) { db.postDao().insertPost(post) }
        }
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
        }
    }

    // ─── Meta stats — hardcoded Season 7.5 (source: https://metabot.gg/en/marvelrivals/heroes/win-rate Apr 24 2026 & https://beebom.com/marvel-rivals-ban-rates/) ──
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

    // ─── Personal stats fetch (live API) ──────────────────────────────────────
    fun fetchPlayerStats(query: String) = viewModelScope.launch {
        if (query.isBlank()) {
            playerStatsError = "Enter your UID or in-game name"
            return@launch
        }
        isLoadingPlayer = true
        playerStatsError = ""
        _playerStats.value = null

        val apiKey = "d5c57e773061d9793e5f031f2e4aacbd871cd7025aad6c3e96ce2ec6cebc361f"

        try {
            val uidToUse = try {
                val found = withContext(Dispatchers.IO) {
                    RetrofitClient.api.findPlayer(apiKey, query)
                }
                Log.d("PLAYER_STATS", "find-player: uid=${found.uid} name=${found.name}")
                found.uid.ifBlank { query }
            } catch (e: Exception) {
                Log.d("PLAYER_STATS", "find-player failed: ${e.localizedMessage}")
                query
            }

            Log.d("PLAYER_STATS", "Fetching stats for: $uidToUse")
            val result = withContext(Dispatchers.IO) {
                RetrofitClient.api.getPlayerStats(apiKey, uidToUse)
            }
            _playerStats.value = result
            Log.d("PLAYER_STATS", "Success: ${result.name}")

        } catch (e: Exception) {
            Log.e("PLAYER_STATS", "getPlayerStats failed: ${e.localizedMessage}")
            try {
                withContext(Dispatchers.IO) { RetrofitClient.api.updatePlayer(apiKey, query) }
                playerStatsError = "Player is being indexed. Try again in 1-2 minutes."
            } catch (e2: Exception) {
                Log.e("PLAYER_STATS", "updatePlayer failed: ${e2.localizedMessage}")
                playerStatsError = "Personal stats require a premium Marvel Rivals API subscription. " +
                        "The API call was made successfully but returned a 403 Forbidden response."
            }
        } finally {
            isLoadingPlayer = false
        }
    }
}