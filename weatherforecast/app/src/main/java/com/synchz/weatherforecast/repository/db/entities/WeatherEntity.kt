package com.synchz.weatherforecast.repository.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "weather")
data class WeatherEntity(@PrimaryKey var id:Long, var dt:Long, var temp:Double, var temp_max:Double, var temp_min:Double,
                         var description:String, var main:String, var icon:String)