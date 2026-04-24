package com.example.lfrivalsggiteration1.ui

import android.app.Application
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
    // ... (Keep your existing User/Post variables) ...

    private val _heroStats = MutableStateFlow<List<HeroStatResponse>>(emptyList())
    val heroStats: StateFlow<List<HeroStatResponse>> = _heroStats

    var selectedPlatform by mutableStateOf("PC")
    var selectedMode by mutableStateOf("Competitive")
    var isLoading by mutableStateOf(false)

    init { fetchStats() }

    fun fetchStats() = viewModelScope.launch {
        if (isLoading) return@launch
        isLoading = true

        // The API requires fetching individual heroes
        val heroesToFetch = listOf("hulk", "iron man", "spider-man", "magneto", "punisher", "storm")
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
    // ... (Keep your existing createPost/saveProfile functions) ...
}