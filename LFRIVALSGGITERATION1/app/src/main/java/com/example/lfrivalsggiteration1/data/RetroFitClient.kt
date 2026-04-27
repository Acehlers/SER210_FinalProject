package com.example.lfrivalsggiteration1.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

// ─── Meta stats (hardcoded, displayed on Stats tab) ──────────────────────────
data class MetaHeroStat(
    val heroName: String,
    val role: String,
    val winRate: Float,
    val pickRate: Float,
    val banRate: Float
)

// ─── Player personal stats (live API) ────────────────────────────────────────
data class PlayerRank(
    @SerializedName("rank")  val rank: String = "Unranked",
    @SerializedName("color") val color: String = "#FFFFFF"
)

data class PlayerIcon(
    @SerializedName("player_icon") val icon: String = ""
)

data class PlayerHeroStat(
    @SerializedName("hero_id")           val heroId: Int = 0,
    @SerializedName("hero_name")         val heroName: String = "Unknown",
    @SerializedName("matches")           val matches: Int = 0,
    @SerializedName("wins")              val wins: Int = 0,
    @SerializedName("k")                 val kills: Double = 0.0,
    @SerializedName("d")                 val deaths: Double = 0.0,
    @SerializedName("a")                 val assists: Double = 0.0,
    @SerializedName("play_time")         val playTime: String = "0h",
    @SerializedName("total_hero_damage") val totalDamage: Double = 0.0
)

data class PlayerInfo(
    @SerializedName("uid")        val uid: Long = 0,
    @SerializedName("level")      val level: String = "0",
    @SerializedName("name")       val name: String = "",
    @SerializedName("icon")       val icon: PlayerIcon = PlayerIcon(),
    @SerializedName("rank")       val rank: PlayerRank = PlayerRank(),
    @SerializedName("hero_stats") val heroStats: List<PlayerHeroStat> = emptyList()
)

data class PlayerStatsResponse(
    @SerializedName("uid")    val uid: Long = 0,
    @SerializedName("name")   val name: String = "",
    @SerializedName("player") val player: PlayerInfo = PlayerInfo()
)

data class FindPlayerResponse(
    @SerializedName("name") val name: String = "",
    @SerializedName("uid")  val uid: String = ""
)

// ─── API Service ──────────────────────────────────────────────────────────────
interface MarvelRivalsApiService {
    @GET("find-player/{username}")
    suspend fun findPlayer(
        @Header("x-api-key") apiKey: String,
        @Path(value = "username", encoded = false) username: String
    ): FindPlayerResponse

    @GET("player/{query}")
    suspend fun getPlayerStats(
        @Header("x-api-key") apiKey: String,
        @Path(value = "query", encoded = false) query: String
    ): PlayerStatsResponse

    @GET("player/{query}/update")
    suspend fun updatePlayer(
        @Header("x-api-key") apiKey: String,
        @Path(value = "query", encoded = false) query: String
    ): Any
}

object RetrofitClient {
    private const val BASE_URL = "https://marvelrivalsapi.com/api/v1/"

    val api: MarvelRivalsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarvelRivalsApiService::class.java)
    }
}