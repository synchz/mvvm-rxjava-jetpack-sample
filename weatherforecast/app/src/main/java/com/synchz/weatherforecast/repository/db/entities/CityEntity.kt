package com.synchz.weatherforecast.repository.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "city")
data class CityEntity(@PrimaryKey var id:Long, var name:String, var country:String, var lat:Double, var lon:Double){
    override fun toString(): String {
        return name
    }
}