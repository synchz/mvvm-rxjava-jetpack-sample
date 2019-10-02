package com.synchz.weatherforecast.ui.weatherForecast.services

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.content.edit
import com.synchz.weatherforecast.config.Constants
import com.synchz.weatherforecast.config.KeyConstants
import com.synchz.weatherforecast.repository.db.AppDatabase
import com.synchz.weatherforecast.repository.db.entities.WeatherEntity
import com.synchz.weatherforecast.repository.network.services.ApiService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class DataFetchService: JobIntentService() {

    companion object {
        val JOB_ID = 1

        fun enqueueWork(context: Context?, work: Intent) {
            context?.let {
                enqueueWork(context, DataFetchService::class.java, JOB_ID, work)
            }
        }
    }

    override fun onHandleWork(intent: Intent) {
        val cityName =  PreferenceManager.getDefaultSharedPreferences(application).getString(KeyConstants.CITY_NAME,"")?:""
        getCurrentForecast(cityName)
        getFutureForecast(cityName)
    }

    /**
     *
     *@author Prikshit
     *GET TODAY FORECAST FOR SELECTED CITY AND PERSIST IT
     *
     * */
    private fun getCurrentForecast(cityName : String){
        val retrofit =  Retrofit.Builder()
            .baseUrl(Constants.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        try {
            apiService.getWeatherData("$cityName,IN").execute().body()?.let { t ->
                AppDatabase.getAppDataBase(context = application)?.let {
                    it.getWeatherDAO().insertWeather(
                        WeatherEntity(
                            1,
                            t.dt,
                            t.main.temp,
                            t.main.temp_max,
                            t.main.temp_min,
                            t.weather[0].description,
                            t.weather[0].main,
                            t.weather[0].icon
                        )
                    )
                }
                PreferenceManager.getDefaultSharedPreferences(application).edit {
                    putLong(KeyConstants.LAST_FETCHED, Date().time)
                }
            }
        }catch (e:Exception){e.printStackTrace()}

    }

    /**
     *
     *@author Prikshit
     *GET FUTURE FORECAST FOIR CURRENT CITY AND PERSIST IT
     *
     * */
    private fun getFutureForecast(cityName : String){
        Log.e("PK","getFutureForecast")
        val retrofit =  Retrofit.Builder()
            .baseUrl(Constants.API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        try {
            apiService.getForecastData("$cityName,IN").execute().body()?.let { t ->
                Log.e("PK", "Response: " + t.list.size.toString())
                AppDatabase.getAppDataBase(context = application)?.let {
                    var c = 2
                    for (i in 8 until t.list.size step 7) {
                        var cw = t.list[i]
                        Log.e("PK", "Response: " + cw.name)
                        it.getWeatherDAO().insertWeather(
                            WeatherEntity(
                                c.toLong(),
                                cw.dt,
                                cw.main.temp,
                                cw.main.temp_max,
                                cw.main.temp_min,
                                cw.weather[0].description,
                                cw.weather[0].main,
                                cw.weather[0].icon
                            )
                        )
                        c += 1
                    }
                }
            }
        }catch (e:Exception){e.printStackTrace()}
    }

}