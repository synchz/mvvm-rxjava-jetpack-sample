package com.synchz.weatherforecast.ui.citySelection

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.synchz.weatherforecast.R
import com.synchz.weatherforecast.config.KeyConstants
import com.synchz.weatherforecast.repository.db.AppDatabase
import com.synchz.weatherforecast.repository.db.entities.CityEntity
import com.synchz.weatherforecast.ui.weatherForecast.ForecastActivity
import com.synchz.weatherforecast.ui.citySelection.viewmodel.CityViewModel
import com.synchz.weatherforecast.ui.citySelection.viewmodel.CityViewModelFactory
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_city_selection.*

class CitySelectionActivity : AppCompatActivity() {

    lateinit var cityViewModel: CityViewModel
    lateinit var disposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_selection)
        checkIfToShowForecastScreen()

    }

    /**
     *
     *@author Prikshit
     *INITIALIZE ACTIVITY
     *
     * */
    private fun init() {
        disposable = CompositeDisposable()
        initViewModel()
        initListeners()
    }

    /**
     *
     *@author Prikshit
     *INITIALIZE REQUIRED LISTENERS
     *
     * */
    private fun initListeners() {
//        ON NEXT BUTTON CLICK CHECK WHETHER CITY NAME IS IN DB AND ACCORDINGLY TAKE USER TO
//        FORECAST SCREEN OR SHOW ERROR TOAST
        next.setOnClickListener {
            cityViewModel.getCityByNameFromDB(editText.text.toString())?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(object : SingleObserver<CityEntity> {
                    override fun onSuccess(t: CityEntity) {
                        startWeatherActivity(t.id.toString(), t.name)
                    }

                    override fun onSubscribe(d: Disposable) {
                        disposable.add(d)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(this@CitySelectionActivity, "City does not exist", Toast.LENGTH_LONG).show()
                    }

                })

        }
//        TAKE USER TO FORECAST SCREEN AFTER SELECTING CITY
        editText.setOnItemClickListener { parent, _, position, _ ->
            var cityEntity = parent.getItemAtPosition(position) as CityEntity
            startWeatherActivity(cityEntity.id.toString(), cityEntity.name)
        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    /**
     *
     *@author Prikshit
     *INITALAIZE VIEWMODEL
     *
     * */
    private fun initViewModel() {
        cityViewModel =
            ViewModelProviders.of(this, CityViewModelFactory(application, disposable)).get(CityViewModel::class.java)
                .apply {
                    getCitiesFromJson()
                }

//      DECIDES WHETHER TO SHOW PROGRESS
        cityViewModel.showProgress.observe(this, Observer {
            if (it) {
                progress.visibility = View.VISIBLE
                loadTV.visibility = View.VISIBLE
                editText.visibility = View.GONE
                next.visibility = View.GONE
            } else {
                progress.visibility = View.GONE
                loadTV.visibility = View.GONE
                editText.visibility = View.VISIBLE
                next.visibility = View.VISIBLE
            }
        })

//      INITIALIZE ARRAY ADAPTER
        cityViewModel.cities.subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                var sortedList = it.sortedWith(compareBy { it.name })
                var adapter = ArrayAdapter<CityEntity>(this, R.layout.list_view, sortedList)
                editText.threshold = 1
                editText.setAdapter(adapter)
            }.apply { disposable.add(this) }
    }


    /**
     *
     *@author Prikshit
     * Verify which screen to display
     * if city was previously slected display forecast activity
     *
     * */
    private fun checkIfToShowForecastScreen() {
        PreferenceManager.getDefaultSharedPreferences(application).getString(KeyConstants.CITY_ID, "")?.let {
            if (it.isEmpty()) backBtn.visibility = View.INVISIBLE
            if (it.isNotEmpty() && isTaskRoot) {
                startWeatherActivity(it,
                    PreferenceManager.getDefaultSharedPreferences(application).getString(KeyConstants.CITY_NAME, "")
                        ?: ""
                )
            }else init()
        }
    }

    /**
     *
     *@author Prikshit
     *START WEATHER FORECAST SCREEN
     *
     * */
    private fun startWeatherActivity(cityId:String, cityName:String) {
        if(!isTaskRoot) cityViewModel.clearWeatherTable()
        Intent(this@CitySelectionActivity, ForecastActivity::class.java).apply {
            putExtra(KeyConstants.CITY_NAME, cityName)
            putExtra(KeyConstants.CITY_ID, cityId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }.let { startActivity(it) }
    }

    override fun onDestroy() {
        if(!disposable.isDisposed) disposable.dispose()
        super.onDestroy()
    }
}
