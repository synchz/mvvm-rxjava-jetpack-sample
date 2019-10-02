package com.synchz.weatherforecast.ui.weatherForecast.services

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters

class DataFetchWorker(var mContext: Context, params : WorkerParameters) : Worker(mContext, params){

    override fun doWork(): Result {
        DataFetchService.enqueueWork(mContext, Intent())
        return Result.success()
    }

}