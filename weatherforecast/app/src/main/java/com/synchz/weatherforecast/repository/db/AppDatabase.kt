package com.synchz.weatherforecast.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.synchz.weatherforecast.repository.db.dao.CityDAO
import com.synchz.weatherforecast.repository.db.dao.WeatherDAO
import com.synchz.weatherforecast.repository.db.entities.CityEntity
import com.synchz.weatherforecast.repository.db.entities.WeatherEntity

@Database(entities = [CityEntity::class, WeatherEntity::class], version =1)
abstract class AppDatabase: RoomDatabase() {

    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
            return INSTANCE
        }

        fun destroyDataBase(){
            INSTANCE = null
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "weatherSynchz.db")
                .fallbackToDestructiveMigration().build()
    }

    abstract fun getCityDAO(): CityDAO
    abstract fun getWeatherDAO(): WeatherDAO
}