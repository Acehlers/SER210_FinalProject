package com.example.lfrivalsggiteration1.data

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

// Matches the response object shown in the docs
data class HeroStatResponse(
    @SerializedName("hero_name") val heroName: String = "Unknown",
    @SerializedName("matches") val matches: Int = 0,
    @SerializedName("wins") val wins: Int = 0,
    @SerializedName("k") val kills: Double = 0.0,
    @SerializedName("play_time") val playTime: String = "0h",
    @SerializedName("total_hero_damage") val totalDamage: Double = 0.0
)

interface MarvelRivalsApiService {
    @GET("heroes/hero/{heroName}/stats")
    suspend fun getSingleHeroStats(
        @Header("x-api-key") apiKey: String,
        @Path("heroName") heroName: String
    ): HeroStatResponse
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