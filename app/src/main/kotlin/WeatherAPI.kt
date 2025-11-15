package org.example.app

import org.example.app.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q")
        city: String,
        @Query("units")
        units: String = "metric",
        @Query("appid")
        apiKey: String = "cec3d920eed361281f4a6459e5cea9ea"
    ) : WeatherResponse
}