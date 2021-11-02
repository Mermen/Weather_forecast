package com.example.weather_forecast

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject

import java.io.InputStream

import org.json.JSONArray

import java.net.URL


class MainActivity : AppCompatActivity() {

    private var country_field: EditText?=null;
    private var city_field: EditText?=null;
    private var get_btn: Button?=null;
    private var result_info: TextView?=null;
    private var img_weather: ImageView?=null;
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var inputStream: InputStream = assets.open("citylistmin.json")
        var json_city: String?= null
        json_city = inputStream.bufferedReader().use { it.readText() }

        var citiesArray = JSONArray(json_city)


        inputStream = assets.open("countriesmin.json")
        var json_countries: String?= null
        json_countries = inputStream.bufferedReader().use { it.readText() }

        var countriesArray = JSONArray(json_countries)



        country_field = findViewById(R.id.Country)
        city_field = findViewById(R.id.City)
        get_btn = findViewById(R.id.get_weather_btn)
        result_info = findViewById(R.id.result_info)
        img_weather = findViewById(R.id.image_weather)
        get_btn?.setOnClickListener {
            if (city_field?.text?.toString()?.trim()?.equals("")!!) {
                Toast.makeText(this,"Set City", Toast.LENGTH_LONG).show()
            }
            else if (country_field?.text?.toString()?.trim()?.equals("")!!) {
                Toast.makeText(this,"Set Country", Toast.LENGTH_LONG).show()
            }
            else {
                var check = 0;
                var country: String = country_field?.text.toString()
                var country_ID: String ="";
                for (i in 0 until countriesArray.length()){
                    if (countriesArray.getJSONObject(i).getString("name")==country){
                        country_ID =countriesArray.getJSONObject(i).getString("alpha-2")
                        check+=1
                    }
                }
                var city: String = city_field?.text.toString()
                var lat_city: String =""
                var lon_city: String =""
                for (i in 0 until citiesArray.length()){
                    if (citiesArray.getJSONObject(i).getString("name")==city&&citiesArray.getJSONObject(i).getString("country")==country_ID){
                        lat_city = citiesArray.getJSONObject(i).getJSONObject("coord").getString("lat")
                        lon_city = citiesArray.getJSONObject(i).getJSONObject("coord").getString("lon")
                        check+=2
                    }
                }
                var key: String = "b0f46f2f935e1c912c9e693692209b34"
                var url: String = "https://api.openweathermap.org/data/2.5/onecall?lat=$lat_city&lon=$lon_city&exclude=daily&appid=$key&units=metric"

                when(check){
                    2->{
                        Toast.makeText(this,"Wrong Country", Toast.LENGTH_LONG).show()
                    }
                    1->{
                    Toast.makeText(this,"Wrong City", Toast.LENGTH_LONG).show()
                    }
                    0->{
                        Toast.makeText(this,"Wrong Country and City", Toast.LENGTH_LONG).show()
                    }
                    else->{
                        doAsync{
                            val apiResp = URL(url).readText()
                            val weather = JSONObject(apiResp).getJSONObject("current").getJSONArray("weather")
                            val desc = weather.getJSONObject(0).getString("description")
                            val temp = JSONObject(apiResp).getJSONObject("current").getString("temp")
                            val ico =weather.getJSONObject(0).getString("icon")
                            uiThread{
                            var url_ico: String = "https://openweathermap.org/img/wn/$ico@4x.png"
                            Picasso.get().load(url_ico).into(img_weather);
                            }
                            result_info?.text = "Temperature: $temp°C \n $desc"
                        }


                    }
                }

            }
        }
    }
}