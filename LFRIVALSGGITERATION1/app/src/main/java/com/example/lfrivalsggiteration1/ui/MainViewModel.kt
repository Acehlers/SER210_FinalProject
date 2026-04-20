package com.example.lfrivalsggiteration1.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.lfrivalsggiteration1.data.AppDatabase
import com.example.lfrivalsggiteration1.data.Post
import com.example.lfrivalsggiteration1.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.Companion.getDatabase(application)

    // ── Profile ───────────────────────────────────────────────────────────────
    val currentUser: LiveData<User?> = db.userDao().getUser()

    fun saveProfile(gamertag: String, discord: String) = viewModelScope.launch {
        val existing = db.userDao().getUserOnce()
        if (existing == null)
            db.userDao().insertUser(
                User(
                    gamertag = gamertag.trim(),
                    discordHandle = discord.trim()
                )
            )
        else
            db.userDao().updateUser(existing.copy(gamertag = gamertag.trim(), discordHandle = discord.trim()))
    }

    // ── Board ─────────────────────────────────────────────────────────────────
    val activePosts: LiveData<List<Post>> = db.postDao().getActivePosts()

    fun createPost(hero: String, role: String, rank: String, message: String) = viewModelScope.launch {
        val userId = db.userDao().getUserOnce()?.userID ?: 1
        db.postDao().insertPost(
            Post(
                userID = userId,
                hero = hero, role = role, rank = rank,
                content = message,
                // Actual
                expiresAt = System.currentTimeMillis() + 30 * 60 * 1000L
                // Demo
                //expiresAt = System.currentTimeMillis() + 10 * 1000L
            )
        )
    }

    // ── Accept (reveals Discord handle) ──────────────────────────────────────
    private val _acceptedPostIds = MutableStateFlow<Set<Int>>(emptySet())
    val acceptedPostIds: StateFlow<Set<Int>> = _acceptedPostIds

    fun acceptPost(postId: Int) {
        _acceptedPostIds.value = _acceptedPostIds.value + postId
    }
}