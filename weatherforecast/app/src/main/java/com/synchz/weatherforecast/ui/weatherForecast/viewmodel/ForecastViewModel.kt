package com.synchz.weatherforecast.ui.weatherForecast.viewmodel

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.synchz.weatherforecast.config.Constants
import com.synchz.weatherforecast.config.KeyConstants
import com.synchz.weatherforecast.repository.db.AppDatabase
import com.synchz.weatherforecast.repository.db.entities.WeatherEntity
import com.synchz.weatherforecast.repository.network.models.CurrentWeather
import com.synchz.weatherforecast.repository.network.models.ForecastWeather
import com.synchz.weatherforecast.repository.network.services.ApiService
import io.reactivex.Flowable
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class ForecastViewModel(application: Application, var compositeDisposable: CompositeDisposable) : AndroidViewModel(application) {

//  WEATHER FORECAST OBJECT
    var weatherData:Flowable<List<WeatherEntity>>? = null

    /**
     *
     *@author Prikshit
     *GETS WEATHER FORECAST FROM DB
     *
     * */
    fun getWeatherData(){
        AppDatabase.getAppDataBase(context = getApplication())?.let {
           weatherData = it.getWeatherDAO().getAllWeather()
        }
    }



}