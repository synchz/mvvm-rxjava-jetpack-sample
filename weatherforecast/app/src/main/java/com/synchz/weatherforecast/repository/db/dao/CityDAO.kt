package com.synchz.weatherforecast.repository.db.dao

import androidx.room.*
import com.synchz.weatherforecast.repository.db.entities.CityEntity
import io.reactivex.Maybe
import androidx.lifecycle.LiveData
import io.reactivex.Flowable
import io.reactivex.Single


@Dao
interface CityDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCity(city: CityEntity):Long

    @Update
    fun updateCity(city: CityEntity)

    @Delete
    fun deleteCity(city: CityEntity)


    @Query("SELECT * FROM city")
    fun getAllCities(): Flowable<List<CityEntity>>

    @Query("SELECT * FROM city where id =:id")
    fun getCityById(id:Long): Maybe<CityEntity>

    @Query("SELECT * FROM city where name =:name COLLATE NOCASE")
    fun getCitiesByName(name:String): Single<CityEntity>

    @Query("SELECT * FROM city where name =:name COLLATE NOCASE")
    fun getByName(name:String): LiveData<CityEntity>

    @Query("SELECT COUNT(id) FROM city")
    fun getRowCount(): LiveData<Int>

    @Query("DELETE FROM city")
    fun clearCitiesTable()
}