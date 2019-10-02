package com.synchz.weatherforecast.ui.weatherForecast.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.disposables.CompositeDisposable

@Suppress("UNCHECKED_CAST")
class ForecastViewModelFactory(var mApplication:Application, var disposable: CompositeDisposable):ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  ForecastViewModel(mApplication, disposable) as T
    }
}