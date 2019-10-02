package com.synchz.weatherforecast.ui.weatherForecast

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProviders
import androidx.work.*
import com.bumptech.glide.Glide
import com.synchz.weatherforecast.R
import com.synchz.weatherforecast.config.Constants
import com.synchz.weatherforecast.config.KeyConstants
import com.synchz.weatherforecast.repository.db.entities.WeatherEntity
import com.synchz.weatherforecast.ui.citySelection.CitySelectionActivity
import com.synchz.weatherforecast.ui.weatherForecast.services.DataFetchService
import com.synchz.weatherforecast.ui.weatherForecast.services.DataFetchWorker
import com.synchz.weatherforecast.ui.weatherForecast.viewmodel.ForecastViewModel
import com.synchz.weatherforecast.ui.weatherForecast.viewmodel.ForecastViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_forecast.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

class ForecastActivity : AppCompatActivity() {

    lateinit var forecastViewmodel:ForecastViewModel
    lateinit var disposable: CompositeDisposable

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        init()
    }

    /**
     *
     *@author Prikshit
     *INITIALIZE ACTIVITY COMPONENTS
     *
     * */
    private fun init() {
        verifyIfInternetIsAvailable()
        disposable = CompositeDisposable()
        startWorker()
        intent.let {
            cityName.text = it.getStringExtra(KeyConstants.CITY_NAME)
            PreferenceManager.getDefaultSharedPreferences(application).edit {
                putString(KeyConstants.CITY_NAME, it.getStringExtra(KeyConstants.CITY_NAME))
                putString(KeyConstants.CITY_ID, it.getStringExtra(KeyConstants.CITY_ID))
            }
        }
        initViewModel()
        initListeners()
    }

    /**
     *
     *@author Prikshit
     *INITIALIZE ALL REQUIRED LISTENERS
     *
     * */
    private fun initListeners() {
        next.setOnClickListener {
            openCitySelection()
        }
        cityName.setOnClickListener {
            openCitySelection()
        }
        swipeRefresh.setOnRefreshListener {
            DataFetchService.enqueueWork(applicationContext, Intent())
        }
    }

    /**
     *
     *@author Prikshit
     *INITALIZE VIEW MODEL
     *
     * */
    private fun initViewModel() {
        forecastViewmodel = ViewModelProviders.of(this, ForecastViewModelFactory(application, disposable))
            .get(ForecastViewModel::class.java).apply {

                getWeatherData()
//                SUBSCRIBE TO WEATHER FORECAST AND RENDER IT ON SCREEN
                weatherData?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
                    Log.e("PK", it.size.toString())
                    it.forEachIndexed { index, we ->
                        when (we.id) {
//                            RENDER TODAY FORECAST
                            1L -> {
                                pb0.visibility = View.GONE
                                swipeRefresh.isRefreshing = false
//                                TO GET LAST FETCHED TIME
                                var millis =
                                    Date().time - PreferenceManager.getDefaultSharedPreferences(this@ForecastActivity).getLong(
                                        KeyConstants.LAST_FETCHED,
                                        0L
                                    )
                                var hms = String.format(
                                    "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                                        TimeUnit.MILLISECONDS.toHours(
                                            millis
                                        )
                                    ),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                            millis
                                        )
                                    )
                                )
                                time.text = getString(R.string.last_fetched) + " $hms"
                                Glide.with(this@ForecastActivity)
                                    .load(Constants.GET_IMAGE_URL + we.icon + Constants.GET_IMAGE_SUFFIX).into(currentIV)
                                currentTemp.text = String.format("%.1f", we.temp) + "°"
                                currentMinTemp.text = String.format("%.1f", we.temp_max) + "°"
                                currentMaxTemp.text = String.format("%.1f", we.temp_min) + "°"
                                textView.text = we.main
                            }
//                            RENDER FUTURE FORECAST
                            2L -> setWeatherFor(one, we)
                            3L -> setWeatherFor(two, we)
                            4L -> setWeatherFor(three, we)
                            5L -> setWeatherFor(four, we)
                            6L -> setWeatherFor(five, we)
                        }
                    }
                }?.let {
                    disposable.add(it)
                }
            }
    }

    /**
     *
     *@author Prikshit
     *SHOWS AN ALERT IN CASE INTERNET IS NOT AVAILABLE
     *
     * */
    private fun verifyIfInternetIsAvailable() {
        if (!isNetworkAvialable()) {
            val builder = AlertDialog.Builder(this)
            with(builder) {
                setTitle(getString(R.string.no_net))
                setMessage(getString(R.string.no_desc))
                setPositiveButton("OK") { di, _ ->
                    di.dismiss()
                }
                show()
            }
        }
    }

    /**
     *
     *@author Prikshit
     *START A SERVICE THAT WILL KEEP FETCHING DATA EACH 5 MINUTE
     *
     * */
    private fun startWorker() {
        DataFetchService.enqueueWork(applicationContext, Intent())
        try {
            WorkManager.initialize(this, Configuration.Builder().build())
        }catch (e: Exception){
            e.printStackTrace()
        }
        val myConstraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val dataRequest = PeriodicWorkRequest
            .Builder(DataFetchWorker::class.java, 5, TimeUnit.MINUTES)
            .addTag(DataFetchWorker::class.java.name)
            .setConstraints(myConstraints)
            .build()
        enqueTask(dataRequest, DataFetchWorker::class.java.name)
    }

    /**
     *
     *@author Prikshit
     *ADD TASK TO WORK MANAGER
     *
     * */
    private fun enqueTask(request1: PeriodicWorkRequest, tag:String) {
        var future1 = WorkManager.getInstance(this@ForecastActivity).getWorkInfosByTag(tag)
        var list1 = future1.get()
        // start only if no such tasks present
        if ((list1 == null) || (list1.size == 0)) {
            WorkManager.getInstance(this).enqueue(request1)
        }
    }

    /**
     *
     *@author Prikshit
     *SET FUTURE FORECAST IN RESPECTIVE VIEWS
     *
     * */
    private fun setWeatherFor(vg: View, cw:WeatherEntity){
        pb1.visibility=View.GONE
        var date = Date(cw.dt*1000)
        vg.findViewById<TextView>(R.id.dayTV).text = DateFormat.format("EEE", date).toString()

        vg.findViewById<TextView>(R.id.dateTV).text = DateFormat.format("dd/MM", date).toString()
        Glide.with(this@ForecastActivity)
            .load(Constants.GET_IMAGE_URL+cw.icon+Constants.GET_IMAGE_SUFFIX).into(vg.findViewById(R.id.iconIV))
    }

    /**
     *
     *@author Prikshit
     *OPEN CITY SELECTION SCREEN
     *
     * */
    private fun openCitySelection(){
        Intent(this@ForecastActivity, CitySelectionActivity::class.java).let { startActivity(it) }
    }

    override fun onDestroy() {
        if(!disposable.isDisposed) disposable.dispose()
        super.onDestroy()
    }

    /**
     *
     *@author Prikshit
     *@return Boolean (TRUE IF NET IS AVAILABLE ELSE FALSE)
     * CHECK IF NETWORK IS AVAILABLE
     *
     * */
    private fun isNetworkAvialable():Boolean{
        val connectivityManager=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo=connectivityManager.activeNetworkInfo
        return  networkInfo!=null && networkInfo.isConnected
    }


}