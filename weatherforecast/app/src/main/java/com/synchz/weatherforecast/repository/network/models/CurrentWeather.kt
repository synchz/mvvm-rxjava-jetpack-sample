package com.synchz.weatherforecast.repository.network.models

data class CurrentWeather(
    val coord: Coord,
    val dt: Long,
    val id: Int,
    val main: Main,
    val name: String,
    val weather: List<Weather>
)