package com.example.myapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.Beach.BeachInfoActivity;
import com.example.myapplication.Feathures.WeatherApiService;
import com.example.myapplication.Profile.ProfileDatabase;
import com.example.myapplication.Profile.SharedViewModel;
import com.example.myapplication.Profile.UserProfile;
import com.example.myapplication.Profile.profile;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_REQUEST_CODE = 101;
    private OkHttpClient client = new OkHttpClient();
    private SearchView searchView;
    private List<Marker> defaultMarkers = new ArrayList<>();

    ImageView profile;
    ProfileDatabase db;
    private ImageView imageView;
    private SharedViewModel sharedViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //drop down menu
        db = new ProfileDatabase(this);

        // Drop down menu
        profile = findViewById(R.id.dropdown_menu);

        // Load user profile and set the image
        UserProfile userProfile = db.getUserProfile();
        if (userProfile != null && userProfile.getImage() != null) {
            // Convert byte array to Bitmap
            Bitmap profileImage = BitmapFactory.decodeByteArray(userProfile.getImage(), 0, userProfile.getImage().length);
            profile.setImageBitmap(profileImage);
        } else {
            // Set a default image if user profile is not found or image is null
            profile.setImageResource(R.drawable.baseline_account_circle_24); // Change this to your default image
        }
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,profile.class));
            }
        });



        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize SearchView
        searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("SearchView", "Query submitted: " + query);
                searchBeaches(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SearchView", "Query text changed: " + newText);
                return false;
            }
        });

        // Initialize Location Client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set Marker Click Listener
        mMap.setOnMarkerClickListener(marker -> {
            LatLng position = marker.getPosition();
            fetchBeachDataAndStartActivity(position.latitude, position.longitude);
            return false;
        });

        DataSet ds = new DataSet();
        int i = 0;
        for (LatLng beach : ds.beaches) {
            Bitmap beachIcon = getBitmapFromVectorDrawable(R.drawable.baseline_beach_access_24);
            if (beachIcon != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(beach)
                        .title(ds.beachNames.get(i++))  // Customize with actual beach names
                        .icon(BitmapDescriptorFactory.fromBitmap(beachIcon)));
            } else {
                mMap.addMarker(new MarkerOptions().position(beach).title(ds.beachNames.get(i++)));
            }
        }
    }

    private void fetchBeachDataAndStartActivity(double lat, double lon) {
        fetchWeatherData(lat, lon);
        fetchAirQualityData(lat, lon);
        fetchTsunamiAlerts(lat, lon);
    }

    private void fetchWeatherData(double lat, double lon) {
        WeatherApiService weatherApiService = new WeatherApiService();
        weatherApiService.fetchWeatherData(lat, lon);
    }

    private void fetchLocation() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }

        // Fetch the user's last known location
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
                // Optionally add a marker at user's location
                mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
            }
        });
    }

    private void searchBeaches(String query) {
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" +
                query + "&key=AIzaSyAbs-NgDYW1ZnAFUvEkijbCFv5lVFOqwTk";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MainActivity", "Failed to fetch beaches", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("SearchResponse", "Response Data: " + responseData);
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray results = jsonObject.getJSONArray("results");

                        runOnUiThread(() -> {
                            // Remove only search markers
                            mMap.clear();
                            addDefaultMarkers(); // Re-add default markers
                            for (int i = 0; i < results.length(); i++) {
                                try {
                                    JSONObject beach = results.getJSONObject(i);
                                    double lat = beach.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                    double lng = beach.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                                    String name = beach.getString("name");

                                    LatLng beachLocation = new LatLng(lat, lng);
                                    mMap.addMarker(new MarkerOptions().position(beachLocation).title(name));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(beachLocation, 12));
                                } catch (JSONException e) {
                                    Log.e("MainActivity", "JSON parsing error", e);
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON parsing error", e);
                    }
                }
            }
        });
    }

    private void addDefaultMarkers() {
        DataSet ds = new DataSet();
        int i = 0;
        for (LatLng beach : ds.beaches) {
            Bitmap beachIcon = getBitmapFromVectorDrawable(R.drawable.baseline_beach_access_24);
            Marker marker;
            if (beachIcon != null) {
                marker = mMap.addMarker(new MarkerOptions()
                        .position(beach)
                        .title(ds.beachNames.get(i++))  // You can customize this with actual beach names
                        .icon(BitmapDescriptorFactory.fromBitmap(beachIcon)));
            } else {
                marker = mMap.addMarker(new MarkerOptions().position(beach).title(ds.beachNames.get(i++)));
            }
            if (marker != null) {
                defaultMarkers.add(marker);
            }
        }
    }

    private void fetchBeachData(String beachName) {
        // Implement the logic to fetch detailed information about the beach
        String url = "https://example.com/api/beachinfo?name=" + beachName; // Replace with actual URL or logic

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MainActivity", "Failed to fetch beach data", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Process the data as needed
                }
            }
        });
    }

    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableId);
        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            }
        }
    }

    private void fetchAirQualityData(double lat, double lon) {
        String url = "https://air-quality.p.rapidapi.com/history/airquality?lat=" + lat + "&lon=" + lon;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-RapidAPI-Key", "38df8820b8msh31579e498bffef1p1052e8jsn8377e37389b5")
                .addHeader("X-RapidAPI-Host", "air-quality.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MainActivity", "Failed to fetch air quality data", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray data = jsonObject.getJSONArray("data");
                        JSONObject latestData = data.getJSONObject(0);
                        int aqi = latestData.getInt("aqi");

                        runOnUiThread(() -> {
                            String airQualityInfo = String.valueOf(aqi);
                            startBeachInfoActivity(null, airQualityInfo, null); // Update with air quality info
                        });
                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON parsing error", e);
                    }
                }
            }
        });
    }

    private void fetchTsunamiAlerts(double lat, double lon) {
        String url = "https://tsunami.incois.gov.in/itews/DSSProducts/OPR/past90days.json";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("MainActivity", "Failed to fetch tsunami alerts", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("MainActivity", "Tsunami Alert Data: " + responseData); // Debug Log

                    boolean isTsunamiAlert = checkForTsunamiAlert(responseData, lat, lon);

                    runOnUiThread(() -> {
                        String tsunamiAlertInfo = isTsunamiAlert ? "Yes" : "No";
                        startBeachInfoActivity(null, null, tsunamiAlertInfo); // Update with tsunami alert info
                    });
                } else {
                    Log.e("MainActivity", "Failed to fetch tsunami alerts: " + response.message());
                }
            }
        });
    }

    private boolean checkForTsunamiAlert(String responseData, double lat, double lon) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray datasetsArray = jsonObject.getJSONArray("datasets");

            for (int i = 0; i < datasetsArray.length(); i++) {
                JSONObject event = datasetsArray.getJSONObject(i);
                double eventLatitude = event.getDouble("LATITUDE");
                double eventLongitude = event.getDouble("LONGITUDE");
                double magnitude = event.getDouble("MAGNITUDE");
                String regionName = event.getString("REGIONNAME");

                // Optional: You can implement more sophisticated proximity check here
                if (isWithinProximity(lat, lon, eventLatitude, eventLongitude) && magnitude >= 5.0) {
                    Log.d("MainActivity", "Tsunami Alert for region: " + regionName);
                    return true; // There is an active tsunami alert in proximity
                }
            }
        } catch (JSONException e) {
            Log.e("MainActivity", "Error parsing tsunami alert data", e);
        }
        return false; // No active tsunami alert
    }

    // Helper method to determine if the location is within proximity
    private boolean isWithinProximity(double lat1, double lon1, double lat2, double lon2) {
        final double RADIUS_THRESHOLD = 100; // Define your proximity threshold in kilometers

        double earthRadius = 6371; // Earth's radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = earthRadius * c; // Distance in kilometers

        return distance <= RADIUS_THRESHOLD;
    }

    private void startBeachInfoActivity(String weather, String airQuality, String tsunamiAlert) {
        Intent intent = new Intent(MainActivity.this, BeachInfoActivity.class);
        intent.putExtra("weather", weather != null ? weather : "Fetching...");
        intent.putExtra("air_quality", airQuality != null ? airQuality : "Fetching...");
        intent.putExtra("tsunami_alert", tsunamiAlert != null ? tsunamiAlert : "Fetching...");
        startActivity(intent);
    }

}
