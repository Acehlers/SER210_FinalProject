package com.example.lfrivalsggiteration1.data

data class Post(
    val postID: String = "",
    val uid: String = "",
    val userID: Int = 0,
    val username: String = "",
    val hero: String = "",
    val role: String = "",
    val rank: String = "",
    val content: String = "",
    val expiresAt: Long = 0L
)