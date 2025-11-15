package org.example.app.models

import java.awt.TrayIcon


data class WeatherResponse(

    val name: String,
    val main: Main,
    val weather: List<Weather>
)
data class Main(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)
data class Weather(
    val description: String,
    val icon: String
)