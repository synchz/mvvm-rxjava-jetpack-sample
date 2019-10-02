package com.synchz.weatherforecast.repository.io.reader

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.synchz.weatherforecast.repository.db.AppDatabase
import com.synchz.weatherforecast.repository.db.entities.CityEntity
import com.synchz.weatherforecast.repository.io.model.City
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader

class CityJsonParser {

    /**
     *
     *@author Prikshit
     *PARSE JSON FILE AND SAVE DATA TO DB
     *
     * */
    fun  jsonFileToCitiesDb(context:Context, countryCode:String):Boolean{

        var isSuccess = false
        var stream:InputStream? =null
        var reader : JsonReader? = null
        var db = AppDatabase.getAppDataBase(context = context)
        try {
            stream= context.assets.open("city.list.json")
            reader = JsonReader(InputStreamReader(stream, "UTF-8"))
            var gson =  GsonBuilder().create()
            reader.beginArray()
            db?.getCityDAO()?.clearCitiesTable()
            while (reader.hasNext()) {
                var city = gson.fromJson<City>(reader, City::class.java)
                if(!city.country.equals(countryCode,true))
                    continue
                db?.getCityDAO()?.insertCity(CityEntity(city.id, city.name, city.country, city.coord.lat, city.coord.lon))
            }
            isSuccess=true
        } catch ( ex: FileNotFoundException) {
            ex.printStackTrace()
        } finally {
            stream?.close()
            reader?.close()
        }
        return isSuccess
    }
}