package com.example.lfrivalsggiteration1.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

// ─── Entities ────────────────────────────────────────────────────────────────

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userID: Int = 0,
    val gamertag: String,
    val discordHandle: String
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val postID: Int = 0,
    val userID: Int,
    val hero: String,
    val role: String,
    val rank: String,
    val content: String,
    val expiresAt: Long
)

// ─── DAOs ─────────────────────────────────────────────────────────────────────

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): LiveData<User?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUserOnce(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User): Int
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE expiresAt > :now ORDER BY expiresAt ASC")
    fun getActivePosts(now: Long = System.currentTimeMillis()): LiveData<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Delete
    suspend fun deletePost(post: Post): Int
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(entities = [User::class, Post::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "lfrivals_db")
                    .build().also { INSTANCE = it }
            }
    }
}