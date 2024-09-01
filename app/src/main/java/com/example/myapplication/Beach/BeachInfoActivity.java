package com.example.myapplication.Beach;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.myapplication.R;


import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BeachInfoActivity extends AppCompatActivity {

    private TextView weatherTextView, airQualityTextView, tsunamiAlertTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beach_info);

        // Initialize TextViews
        weatherTextView = findViewById(R.id.weather_text_view);
        airQualityTextView = findViewById(R.id.air_quality_text_view);
        tsunamiAlertTextView = findViewById(R.id.tsunami_alert_text_view);

        // Get data from Intent
        Intent intent = getIntent();
        String weather = intent.getStringExtra("weather");
        String airQuality = intent.getStringExtra("air_quality");
        String tsunamiAlert = intent.getStringExtra("tsunami_alert");

        // Set data to TextViews
        weatherTextView.setText("Weather: " + weather);
        airQualityTextView.setText("Air Quality Index: " + airQuality);
        tsunamiAlertTextView.setText("Tsunami Alert: " + tsunamiAlert);
    }
}
