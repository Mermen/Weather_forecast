package com.example.weather_forecast

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject

import java.io.InputStream

import org.json.JSONArray

import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var country_field: EditText?=null
    private var city_field: EditText?=null
    private var get_btn: Button?=null
    private var set_btn: Button?=null
    private var result_info: TextView?=null
    private var img_weather: ImageView?=null
    private var img_tmp: ImageView?=null
    private var text_tmp: TextView?=null

    var check_loc = false
    var lat_check =0.0
    var lon_check =0.0
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        var inputStream: InputStream = assets.open("citylistmin.json")
        var json_city: String?= null
        json_city = inputStream.bufferedReader().use { it.readText() }

        var citiesArray = JSONArray(json_city)


        inputStream = assets.open("countriesmin.json")
        var json_countries: String?= null
        json_countries = inputStream.bufferedReader().use { it.readText() }

        var countriesArray = JSONArray(json_countries)

        val date_formater_hourly = java.text.SimpleDateFormat("dd.MM' 'HH:mm")

        val date_formater_daily = java.text.SimpleDateFormat("dd.MM")
        /*
        val date = java.util.Date(1532358895 * 1000)
        val asd: String = sdf.format(date)
        */
        country_field = findViewById(R.id.Country)
        city_field = findViewById(R.id.City)
        get_btn = findViewById(R.id.get_weather_btn)
        set_btn = findViewById(R.id.set_current_city_btn)
        result_info = findViewById(R.id.result_info)
        /*
        val viewId = resources.getIdentifier("image_weather", "id", packageName)
        img_weather = findViewById(viewId)
        */
        img_weather = findViewById(R.id.image_weather)
        fetchLocation(citiesArray)
        get_Weather(countriesArray, citiesArray, date_formater_hourly, date_formater_daily)
        set_btn?.setOnClickListener{

            fetchLocation(citiesArray)

        }
        get_btn?.setOnClickListener {
            get_Weather(countriesArray, citiesArray, date_formater_hourly, date_formater_daily)

        }

    }

    private fun fetchLocation(citiesArray: JSONArray) {

        val task = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
                ){
            ActivityCompat.requestPermissions( this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)

        }
        task.addOnSuccessListener {
            if (it!=null){
                val geoCoder = Geocoder(this,Locale.US)
                val currentLocation = geoCoder.getFromLocation(
                    it.latitude,
                    it.longitude,
                    1
                )
                country_field?.setText(currentLocation.get(0).countryName)
                var lat = 0.0
                var lon = 0.0
                //Toast.makeText(this, it.latitude.toString()+" "+it.longitude.toString(), Toast.LENGTH_LONG).show()
                var city_name =""
                val PI_R = 3.14159265359
                for (i in 0 until citiesArray.length()) {
                    if(citiesArray.getJSONObject(i).getString("country")==currentLocation.get(0).countryCode) {
                        if (distans_sin(it.latitude, it.longitude, citiesArray.getJSONObject(i).getJSONObject("coord").getString("lat")
                                .toDouble(), citiesArray.getJSONObject(i).getJSONObject("coord").getString("lon")
                                .toDouble(),PI_R)< distans_sin(it.latitude, it.longitude, lat, lon,PI_R)) {
                            lon =
                                citiesArray.getJSONObject(i).getJSONObject("coord").getString("lon")
                                    .toDouble()
                            lon_check = citiesArray.getJSONObject(i).getJSONObject("coord").getString("lon")
                                .toDouble()
                            lat =
                                citiesArray.getJSONObject(i).getJSONObject("coord").getString("lat")
                                    .toDouble()
                            lat_check =
                                citiesArray.getJSONObject(i).getJSONObject("coord").getString("lat")
                                    .toDouble()
                            city_name = citiesArray.getJSONObject(i).getString("name")
                        }
                   }
                }

                //Toast.makeText(this,  lat.toString()+" "+lon.toString(), Toast.LENGTH_LONG).show()
                city_field?.setText(city_name)
                check_loc = true
            }
        }
    }


    private fun distans_sin(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        PI_R: Double
    )=asin(
        sqrt(
            (sin((abs(lat1*PI_R/180.0- lat2*PI_R/180.0))/2.0))*(sin((abs(lat1*PI_R/180.0-lat2*PI_R/180.0))/2.0))
    + cos(lat2*PI_R/180.0)*cos(lat1*PI_R/180.0)
    *(sin(abs(lon1*PI_R/180.0-lon2*PI_R/180.0)/2.0))*(sin(abs(lon1*PI_R/180.0-lon2*PI_R/180.0)/2.0))
        )
    )
    private fun get_Weather(
        countriesArray: JSONArray,
        citiesArray: JSONArray,
        date_formater_hourly: SimpleDateFormat,
        date_formater_daily: SimpleDateFormat,
    ) {
        if (city_field?.text?.toString()?.trim()?.equals("")!!) {
            Toast.makeText(this, "Set City", Toast.LENGTH_LONG).show()
        } else if (country_field?.text?.toString()?.trim()?.equals("")!!) {
            Toast.makeText(this, "Set Country", Toast.LENGTH_LONG).show()
        } else {
            var check = 0
            var country: String = country_field?.text.toString()
            var country_ID: String = ""
            for (i in 0 until countriesArray.length()) {
                if (countriesArray.getJSONObject(i).getString("name") == country) {
                    country_ID = countriesArray.getJSONObject(i).getString("alpha-2")
                    check += 1
                }
            }
            var city: String = city_field?.text.toString()
            var lat_city: String = ""
            var lon_city: String = ""
            for (i in 0 until citiesArray.length()) {
                if (citiesArray.getJSONObject(i)
                        .getString("name") == city && citiesArray.getJSONObject(i)
                        .getString("country") == country_ID
                ) {
                    lat_city = citiesArray.getJSONObject(i).getJSONObject("coord").getString("lat")
                    lon_city = citiesArray.getJSONObject(i).getJSONObject("coord").getString("lon")
                    check += 2
                }
            }
            if(check_loc){
                check_loc = false
                lat_city = lat_check.toString()
                lon_city = lon_check.toString()
            }
            //Toast.makeText(this,  lat_city.toString()+" "+lon_city.toString(), Toast.LENGTH_LONG).show()
            var key: String = "b0f46f2f935e1c912c9e693692209b34"
            var url: String =
                "https://api.openweathermap.org/data/2.5/onecall?lat=$lat_city&lon=$lon_city&exclude=minutely,alerts&appid=$key&units=metric"

            when (check) {
                2 -> {
                    Toast.makeText(this, "Wrong Country", Toast.LENGTH_LONG).show()
                }
                1 -> {
                    Toast.makeText(this, "Wrong City", Toast.LENGTH_LONG).show()
                }
                0 -> {
                    Toast.makeText(this, "Wrong Country or City", Toast.LENGTH_LONG).show()
                }
                else -> {
                    doAsync {
                        val apiResp = URL(url).readText()
                        val weather =
                            JSONObject(apiResp).getJSONObject("current").getJSONArray("weather")
                        val desc = weather.getJSONObject(0).getString("description")
                        val temp = JSONObject(apiResp).getJSONObject("current").getString("temp")
                        var ico = weather.getJSONObject(0).getString("icon")
                        var date_string =
                            JSONObject(apiResp).getJSONObject("current").getString("dt")

                        val weather_hourly = JSONObject(apiResp).getJSONArray("hourly")
                        val icoArray_hourly = arrayOfNulls<String>(48)
                        val timeArray_hourly = arrayOfNulls<String>(48)
                        val tempArray_hourly = arrayOfNulls<String>(48)
                        val descArray_hourly = arrayOfNulls<String>(48)

                        val weather_daily = JSONObject(apiResp).getJSONArray("daily")
                        val icoArray_daily = arrayOfNulls<String>(8)
                        val timeArray_daily = arrayOfNulls<String>(8)
                        val tempArray_daily = arrayOfNulls<String>(8)
                        val descArray_daily = arrayOfNulls<String>(8)

                        for (i in 0 until weather_hourly.length()) {
                            icoArray_hourly[i] =
                                weather_hourly.getJSONObject(i).getJSONArray("weather")
                                    .getJSONObject(0).getString("icon")
                            timeArray_hourly[i] = weather_hourly.getJSONObject(i).getString("dt")
                            tempArray_hourly[i] = weather_hourly.getJSONObject(i).getString("temp")
                            descArray_hourly[i] =
                                weather_hourly.getJSONObject(i).getJSONArray("weather")
                                    .getJSONObject(0).getString("description")

                        }
                        for (i in 0 until weather_daily.length()) {
                            icoArray_daily[i] =
                                weather_daily.getJSONObject(i).getJSONArray("weather")
                                    .getJSONObject(0).getString("icon")
                            timeArray_daily[i] = weather_daily.getJSONObject(i).getString("dt")
                            tempArray_daily[i] =
                                weather_daily.getJSONObject(i).getJSONObject("temp")
                                    .getString("day")
                            descArray_daily[i] =
                                weather_daily.getJSONObject(i).getJSONArray("weather")
                                    .getJSONObject(0).getString("description")

                        }
                        uiThread {
                            var url_ico: String = "https://openweathermap.org/img/wn/$ico@4x.png"
                            Picasso.get().load(url_ico).into(img_weather)
                            for (i in icoArray_hourly.indices) {
                                ico = icoArray_hourly[i]
                                url_ico = "https://openweathermap.org/img/wn/$ico@4x.png"
                                val viewId =
                                    resources.getIdentifier("imageView0$i", "id", packageName)
                                img_tmp = findViewById(viewId)
                                Picasso.get().load(url_ico).into(img_tmp)
                            }
                            for (i in icoArray_daily.indices) {
                                ico = icoArray_daily[i]
                                url_ico = "https://openweathermap.org/img/wn/$ico@4x.png"
                                val viewId =
                                    resources.getIdentifier("imageView1$i", "id", packageName)
                                img_tmp = findViewById(viewId)
                                Picasso.get().load(url_ico).into(img_tmp)
                            }
                        }

                        result_info?.text = "Current Weather\nTemperature: $temp°C\n$desc"
                        for (i in timeArray_hourly.indices) {
                            date_string = timeArray_hourly[i]
                            val date_int = date_string.toLong()
                            val date = Date(date_int * 1000)
                            val string_time: String = date_formater_hourly.format(date)
                            val temp = tempArray_hourly[i]
                            val desc = descArray_hourly[i]
                            val viewId = resources.getIdentifier("textView0$i", "id", packageName)
                            text_tmp = findViewById(viewId)
                            text_tmp?.text = "Date: $string_time\nTemp: $temp°C\n$desc"

                        }
                        for (i in timeArray_daily.indices) {
                            date_string = timeArray_daily[i]
                            val date_int = date_string.toLong()
                            val date = Date(date_int * 1000)
                            val string_time: String = date_formater_daily.format(date)
                            val temp = tempArray_daily[i]
                            val desc = descArray_daily[i]
                            val viewId = resources.getIdentifier("textView1$i", "id", packageName)
                            text_tmp = findViewById(viewId)
                            text_tmp?.text = "Date: $string_time\nTemp: $temp°C\n$desc"

                        }
                    }


                }
            }

        }
    }

}