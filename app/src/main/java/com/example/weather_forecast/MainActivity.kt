package com.example.weather_forecast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.anko.doAsync
import org.json.JSONObject

import java.io.FileInputStream

import java.io.InputStream

import java.io.File
import org.json.JSONArray

import java.io.FileReader
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var city_field: EditText?=null;
    private var get_btn: Button?=null;
    private var result_info: TextView?=null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputStream: InputStream = assets.open("citylistmin.json")
        var json: String?= null
        json = inputStream.bufferedReader().use { it.readText() }

        var jsonarr = JSONArray(json)




        city_field = findViewById(R.id.City)
        get_btn = findViewById(R.id.get_weather_btn)
        result_info = findViewById(R.id.result_info)

        get_btn?.setOnClickListener {
            if (city_field?.text?.toString()?.trim()?.equals("")!!) {
                Toast.makeText(this,"Set City", Toast.LENGTH_LONG).show()
            }
            else {
                var city: String = city_field?.text.toString()
                var lat_city: String =""
                var lon_city: String =""
                for (i in 0 until jsonarr.length()){
                    if (jsonarr.getJSONObject(i).getString("name")==city){
                        lat_city = jsonarr.getJSONObject(i).getJSONObject("coord").getString("lat")
                        lon_city = jsonarr.getJSONObject(i).getJSONObject("coord").getString("lon")
                        result_info?.setText(city+"\n"+lon_city+"\n"+lat_city)
                    }
                }
                var key: String = "b0f46f2f935e1c912c9e693692209b34"
                var url: String = "https://api.openweathermap.org/data/2.5/onecall?lat=$lat_city&lon=$lon_city&exclude=daily&appid=$key&units=metric"

                doAsync{
                    val apiResp = URL(url).readText()

                }
            }
        }
    }
}