package com.synchz.weatherforecast.ui.citySelection.viewmodel

import android.app.Application
import android.preference.PreferenceManager
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.synchz.weatherforecast.config.Constants
import com.synchz.weatherforecast.config.KeyConstants
import com.synchz.weatherforecast.repository.db.AppDatabase
import com.synchz.weatherforecast.repository.db.entities.CityEntity
import com.synchz.weatherforecast.repository.io.reader.CityJsonParser
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CityViewModel(application: Application, var compositeDisposable: CompositeDisposable) : AndroidViewModel(application) {

//    LIVE DATA OBJECT TO CHECK IF TO SHOW LOADER OR NOT
    var showProgress = MutableLiveData<Boolean>()
//    LIST OF ALL CITIES FETCHED FORM DB
    lateinit var cities :Flowable<List<CityEntity>>

    init {
        getCitiesFromDB()
    }

    /**
     *
     *@author Prikshit
     *GET INDIAN CITIES FROM JSON AND SAVE THEM IN SQLLITE
     *
     * */
    fun getCitiesFromJson(){
        if(!PreferenceManager.getDefaultSharedPreferences(getApplication()).getBoolean(KeyConstants.ARE_CITIES_LOADED, false)) {
            var parser = CityJsonParser()
            Observable.just(parser)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    showProgress.postValue(true)
                }
                .observeOn(Schedulers.io())
                .map {
                    parser.jsonFileToCitiesDb(getApplication(), Constants.COUNTRY_CODE)
                }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
//                    WON'T LOAD CITIES FROM JSON IF SAVED TO DATABASE
                    PreferenceManager.getDefaultSharedPreferences(getApplication()).edit {
                        putBoolean(KeyConstants.ARE_CITIES_LOADED, it)
                    }
                    showProgress.postValue(false)
                }.apply { compositeDisposable.add(this) }
        }else showProgress.postValue(false)
    }

    /**
     *
     *@author Prikshit
     * GET LIST OF ALL CITIES FROM SQLLITE
     *
     *
     * */
    private fun getCitiesFromDB(){
        var db = AppDatabase.getAppDataBase(context = getApplication())
        db?.let {
            cities = it.getCityDAO().getAllCities()
        }
    }

    /**
     *
     *@author Prikshit
     *GET CITY BY NASME TO CHECK IF GIVEN CITY EXISTS IN THE SYSTEM
     *
     * */
    fun getCityByNameFromDB(name:String): Single<CityEntity>? {
        var db = AppDatabase.getAppDataBase(context = getApplication())
        return db?.getCityDAO()?.getCitiesByName(name)
    }

    /**
     *
     *@author Prikshit
     *CLEAR PERSISTED DATA FOR CURRENT CITY
     *
     * */
    fun clearWeatherTable(){
        Completable.fromAction{
            AppDatabase.getAppDataBase(context = getApplication())?.let {
                it.getWeatherDAO().clearWeatherTable()
            }
        }
            .subscribeOn(Schedulers.io())
            .subscribe()

    }

}