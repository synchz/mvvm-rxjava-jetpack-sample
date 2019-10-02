package com.synchz.weatherforecast.repository.io.model

data class City(var id:Long, var name:String, var country:String, var coord:Coord){

    override fun toString(): String {
        return name
    }
}