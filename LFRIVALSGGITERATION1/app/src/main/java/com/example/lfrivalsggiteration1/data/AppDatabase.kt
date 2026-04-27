package com.example.lfrivalsggiteration1.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userID: Int = 0,
    val gamertag: String,
    val discordHandle: String,
    val username: String = "",
    val password: String = ""
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

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): LiveData<User?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUserOnce(): User?

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

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

@Database(entities = [User::class, Post::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lfrivals_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}