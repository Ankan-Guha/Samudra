package com.example.myapplication.Feathures;


import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherApiService {
    private static final String API_HOST = "weatherapi-com.p.rapidapi.com";
    private static final String API_KEY = "c412c85681msh9438d3305a1c3c7p1e092cjsnca25fde4bb15";

    private OkHttpClient client = new OkHttpClient();

    public void fetchWeatherData(double latitude, double longitude) {
        String url = "https://weatherapi-com.p.rapidapi.com/current.json?q=" + latitude + "," + longitude;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-rapidapi-host", API_HOST)
                .addHeader("x-rapidapi-key", API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle failure
                e.printStackTrace();
                Log.e("WeatherApiService", "Error fetching weather data", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle successful response
                    String responseData = response.body().string();
                    try {
                        // Parse the JSON response
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String weatherCondition = jsonResponse.getJSONObject("current")
                                .getJSONObject("condition")
                                .getString("text");

                        double temperature = jsonResponse.getJSONObject("current").getDouble("temp_c");
                        int humidity = jsonResponse.getJSONObject("current").getInt("humidity");

                        // Print or use the parsed data
                        Log.d("WeatherApiService", "Weather Condition: " + weatherCondition);
                        Log.d("WeatherApiService", "Temperature (C): " + temperature);
                        Log.d("WeatherApiService", "Humidity: " + humidity);

                        // Optionally, pass this data to another activity or UI element
                        // You may need to use a Handler or runOnUiThread to update UI elements
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("WeatherApiService", "Error parsing weather data", e);
                    }
                } else {
                    // Handle the case when the response is not successful
                    Log.e("WeatherApiService", "Response failed with status code: " + response.code());
                }
            }
        });
    }
}

