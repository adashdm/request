package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var contextTextTemp: TextView
    private lateinit var contextTextWind: TextView
    private lateinit var imageWeather: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        contextTextTemp = findViewById(R.id.temperature)
        contextTextWind = findViewById(R.id.wind)
        imageWeather = findViewById(R.id.imageView2)

        val button1: Button = findViewById(R.id.button)
        button1.setOnClickListener {
            if(hasConnection(this)) {
                getWeather()
            }
            else Toast.makeText(this, "Немає підключення до інтернету", Toast.LENGTH_LONG).show()
        }
    }

    private fun getWeather() {
        thread {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val weatherApi = retrofit.create(WeatherApi::class.java)
            val call = weatherApi.getWeather(49.0625, 33.4048, "temperature_2m,precipitation,rain,showers,snowfall,wind_speed_10m", "unixtime", 1)
            val response = call.execute()

            if (response.isSuccessful) {
                val forecastResponse = response.body()
                val getWind = "Wind Speed: ${forecastResponse?.current?.wind_speed_10m} m"
                val getTemp = "Temperature: ${forecastResponse?.current?.temperature_2m} °C"
                val checkTemp = forecastResponse?.current?.temperature_2m
                //println(call.request().toString())

                runOnUiThread {
                    contextTextTemp.text = getTemp
                    contextTextWind.text = getWind

                }
            if(checkTemp!! <= 1.0)
            {
                runOnUiThread {
                    imageWeather.setImageResource(R.drawable.lowtemp)
                }
            }
                else if (checkTemp!! > 1)
            {
                runOnUiThread {
                    imageWeather.setImageResource(R.drawable.normaltemp)
                }
            }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${response.code()}",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    @Suppress("DEPRECATION")
    fun hasConnection(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.activeNetworkInfo
        return wifiInfo != null && wifiInfo.isConnected
    }
    data class ForecastResponse(
        val current: CurrentWeather
    )

    data class CurrentWeather(
        var wind_speed_10m: Double,
        val temperature_2m: Double
    )

    interface WeatherApi {
        @GET("/v1/forecast")
        fun getWeather(
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("current") current: String,
            @Query("timeformat") timeformat: String,
            @Query("forecast_days") forecastDays: Int
        ): Call<ForecastResponse>
    }

}
