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

    // ─── User & Posts as StateFlow ────────────────────────────────────────────
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _activePosts = MutableStateFlow<List<Post>>(emptyList())
    val activePosts: StateFlow<List<Post>> = _activePosts

    // ─── Accepted posts ───────────────────────────────────────────────────────
    private val _acceptedPostIds = MutableStateFlow<Set<Int>>(emptySet())
    val acceptedPostIds: StateFlow<Set<Int>> = _acceptedPostIds

    // ─── Stats ────────────────────────────────────────────────────────────────
    private val _heroStats = MutableStateFlow<List<HeroStatResponse>>(emptyList())
    val heroStats: StateFlow<List<HeroStatResponse>> = _heroStats

    var selectedPlatform by mutableStateOf("PC")
        private set
    fun updatePlatform(value: String) { selectedPlatform = value; fetchStats() }

    var selectedMode by mutableStateOf("Competitive")
        private set
    fun updateMode(value: String) { selectedMode = value; fetchStats() }

    var isLoading by mutableStateOf(false)

    init {
        fetchStats()
        // Observe Room LiveData and push into StateFlow
        viewModelScope.launch {
            db.userDao().getUser().observeForever { _currentUser.value = it }
        }
        viewModelScope.launch {
            db.postDao().getActivePosts().observeForever { _activePosts.value = it ?: emptyList() }
        }
    }

    // ─── Auth helpers ─────────────────────────────────────────────────────────
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
        onComplete()
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
                hero = hero,
                role = role,
                rank = rank,
                content = message,
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

    // ─── Stats fetching ───────────────────────────────────────────────────────
    fun fetchStats() = viewModelScope.launch {
        if (isLoading) return@launch
        isLoading = true
        val heroesToFetch = listOf("hulk", "iron man", "spider-man", "magneto", "punisher", "storm", "thor", "hela", "scarlet witch", "doctor strange")
        val fetchedList = mutableListOf<HeroStatResponse>()
        try {
            val apiKey = "d5c57e773061d9793e5f031f2e4aacbd871cd7025aad6c3e96ce2ec6cebc361f"
            heroesToFetch.forEach { name ->
                val result = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getSingleHeroStats(apiKey, name)
                }
                fetchedList.add(result)
            }
            _heroStats.value = fetchedList
            Log.d("STATS_DEBUG", "SUCCESS: Loaded ${fetchedList.size} heroes.")
        } catch (e: Exception) {
            Log.e("STATS_DEBUG", "API Error: ${e.localizedMessage}")
        } finally {
            isLoading = false
        }
    }
}