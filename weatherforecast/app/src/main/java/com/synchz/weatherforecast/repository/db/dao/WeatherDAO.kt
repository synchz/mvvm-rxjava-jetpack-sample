package com.synchz.weatherforecast.repository.db.dao

import androidx.room.*
import com.synchz.weatherforecast.repository.db.entities.CityEntity
import io.reactivex.Maybe
import androidx.lifecycle.LiveData
import com.synchz.weatherforecast.repository.db.entities.WeatherEntity
import io.reactivex.Flowable
import io.reactivex.Single


@Dao
interface WeatherDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeather(weather: WeatherEntity):Long

    @Update
    fun updateWeather(weather: WeatherEntity)

    @Delete
    fun deleteWeather(weather: WeatherEntity)


    @Query("SELECT * FROM weather")
    fun getAllWeather(): Flowable<List<WeatherEntity>>

    @Query("SELECT * FROM weather where id =:id")
    fun getWeatherById(id:Long): Maybe<WeatherEntity>

    @Query("SELECT COUNT(id) FROM weather")
    fun getRowCount(): LiveData<Int>

    @Query("DELETE FROM weather")
    fun clearWeatherTable()
}