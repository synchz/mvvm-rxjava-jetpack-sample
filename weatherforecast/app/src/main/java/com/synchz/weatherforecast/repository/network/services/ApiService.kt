package com.synchz.weatherforecast.repository.network.services

import com.synchz.weatherforecast.config.Constants
import com.synchz.weatherforecast.repository.network.models.CurrentWeather
import com.synchz.weatherforecast.repository.network.models.ForecastWeather
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 *
 *@author Prikshit
 *SERVICE CLASS TO FETCH DATA FROM API
 *
 * */
interface ApiService {
    @GET("weather")
    fun getWeatherData(@Query("q") countryName: String, @Query("APPID") apiKey:String = Constants.API_KEY, @Query("units") units:String="metric"):  Call<CurrentWeather>
    @GET("forecast")
    fun getForecastData(@Query("q") countryName: String, @Query("APPID") apiKey:String = Constants.API_KEY, @Query("units") units:String="metric"): Call<ForecastWeather>
}