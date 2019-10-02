package com.synchz.weatherforecast.repository.network.models

data class Main(
    val humidity: Double,
    val pressure: Double,
    val temp: Double,
    val temp_max: Double,
    val temp_min: Double
)