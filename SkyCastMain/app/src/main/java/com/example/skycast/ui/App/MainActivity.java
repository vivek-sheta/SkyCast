package com.example.skycast.ui.App;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.skycast.R;
import com.example.skycast.ui.App.WeatherResponse.WeatherResponse;
import com.example.skycast.ui.App.WeatherResponse.WeatherService;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SkyCastMain"; // Log tag
    private static final int SETTINGS_REQUEST_CODE = 1001;

    // UI Components
    private TextView textCity, textTemp, textFeelsLike, textDescription,
            textHumidity, textWind, textPressure, textVisibility, textSunrise, textSunset;
    private ImageView weatherIcon, feelsLikeIcon, humidityIcon, windIcon,
            pressureIcon, visibilityIcon, sunriseIcon, sunsetIcon;

    // API key for the OpenWather to pull data
    private final String API_KEY = "660d99e3d5bd8f79d912d95a780968ee";
    private String currentCity = "Calgary"; // Default city
    private WeatherService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Activity created");

        initializeViews();
        setupRetrofit();
        fetchWeatherData(currentCity);
    }

    /**
     * Initialize all UI components from the layout
     */
    private void initializeViews() {
        Log.d(TAG, "Initializing views");
        // Text Views
        textCity = findViewById(R.id.textCity);
        textTemp = findViewById(R.id.textTemp);
        textFeelsLike = findViewById(R.id.textFeelsLike);
        textDescription = findViewById(R.id.textDescription);
        textHumidity = findViewById(R.id.textHumidity);
        textWind = findViewById(R.id.textWind);
        textPressure = findViewById(R.id.textPressure);
        textVisibility = findViewById(R.id.textVisibility);
        textSunrise = findViewById(R.id.textSunrise);
        textSunset = findViewById(R.id.textSunset);

        // Image Views
        weatherIcon = findViewById(R.id.weatherIcon);
        feelsLikeIcon = findViewById(R.id.feelsLikeIcon);
        humidityIcon = findViewById(R.id.humidityIcon);
        windIcon = findViewById(R.id.windIcon);
        pressureIcon = findViewById(R.id.pressureIcon);
        visibilityIcon = findViewById(R.id.visibilityIcon);
        sunriseIcon = findViewById(R.id.sunriseIcon);
        sunsetIcon = findViewById(R.id.sunsetIcon);

        // Set static icons
        humidityIcon.setImageResource(R.drawable.ic_humidity);
        windIcon.setImageResource(R.drawable.ic_wind);
        pressureIcon.setImageResource(R.drawable.ic_pressure);
        visibilityIcon.setImageResource(R.drawable.ic_visibility);
        sunriseIcon.setImageResource(R.drawable.ic_sunrise);
        sunsetIcon.setImageResource(R.drawable.ic_sunset);
    }

    /**
     * Setup Retrofit for API calls
     */
    private void setupRetrofit() {
        Log.d(TAG, "Setting up Retrofit");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(WeatherService.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Log.d(TAG, "Settings menu item selected");
            openSettings();
            return true;
        } else if (item.getItemId() == R.id.btnLogout) {
            Log.d(TAG, "Logout menu item selected");
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Open SettingsActivity to change location
     */
    private void openSettings() {
        Log.d(TAG, "Opening SettingsActivity");
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Activity result received - RequestCode: " + requestCode);

        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("CITY_NAME")) {
                currentCity = data.getStringExtra("CITY_NAME");
                Log.i(TAG, "City changed to: " + currentCity);
                fetchWeatherData(currentCity);
            }
        }
    }

    /**
     * Fetch weather data from API for the given city
     * @param city The city to fetch weather for
     */
    private void fetchWeatherData(String city) {
        Log.i(TAG, "Fetching weather data for: " + city);
        showLoadingState();

        Call<WeatherResponse> call = service.getCurrentWeather(city, API_KEY, "metric");
        call.enqueue(new Callback<WeatherResponse>() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Weather data received successfully");
                    WeatherResponse weather = response.body();
                    updateWeatherUI(weather);
                } else {
                    String errorMsg = "Error: " + response.code() + " - " + response.message();
                    Log.e(TAG, errorMsg);
                    showError(errorMsg);
                    showErrorState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                String errorMsg = "API call failed: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                showError(errorMsg);
                showErrorState();
            }
        });
    }

    /**
     * Update UI with weather data
     * @param weather The weather data to display
     */
    private void updateWeatherUI(WeatherResponse weather) {
        Log.d(TAG, "Updating UI with weather data");

        // Set basic info
        textCity.setText(weather.name + ", " + weather.sys.country);
        textTemp.setText(String.format("%.1f°C", weather.main.temp));
        textFeelsLike.setText(String.format("Feels like: %.1f°C", weather.main.feels_like));

        // Set weather description and icons
        if (weather.weather.length > 0) {
            String description = weather.weather[0].description;
            // Capitalize first letter
            description = description.substring(0, 1).toUpperCase() + description.substring(1);
            textDescription.setText(description);

            // Set main weather icon
            setWeatherIcon(weatherIcon, weather.weather[0].id);

            // Set "Feels Like" icon based on temperature difference
            setFeelsLikeIcon(weather.main.temp, weather.main.feels_like);
        }

        // Set other details
        textHumidity.setText(String.format("%d%%", weather.main.humidity));
        textWind.setText(String.format("%.1f km/h", weather.wind.speed * 3.6)); // Convert m/s to km/h
        textPressure.setText(String.format("%d hPa", weather.main.pressure));
        textVisibility.setText(String.format("%.1f km", weather.visibility / 1000.0));

        // Convert timestamps to readable time
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        textSunrise.setText(sdf.format(new Date(weather.sys.sunrise * 1000L)));
        textSunset.setText(sdf.format(new Date(weather.sys.sunset * 1000L)));
    }

    /**
     * Set weather icon based on weather condition code
     * @param imageView The ImageView to set icon on
     * @param conditionCode OpenWeatherMap condition code
     */
    private void setWeatherIcon(ImageView imageView, int conditionCode) {
        Log.d(TAG, "Setting weather icon for condition: " + conditionCode);
        int iconResId;

        // Group weather conditions and assign appropriate icons
        if (conditionCode >= 200 && conditionCode < 300) {
            // Thunderstorm
            iconResId = R.drawable.ic_thunderstorm;
        } else if (conditionCode >= 300 && conditionCode < 500) {
            // Drizzle
            iconResId = R.drawable.ic_drizzle;
        } else if (conditionCode >= 500 && conditionCode < 600) {
            // Rain
            iconResId = R.drawable.ic_rain;
        } else if (conditionCode >= 600 && conditionCode < 700) {
            // Snow
            iconResId = R.drawable.ic_snow;
        } else if (conditionCode >= 700 && conditionCode < 800) {
            // Atmosphere (mist, fog, etc.)
            iconResId = R.drawable.ic_mist;
        } else if (conditionCode == 800) {
            // Clear
            iconResId = R.drawable.ic_clear;
        } else if (conditionCode > 800) {
            // Clouds
            iconResId = R.drawable.ic_cloudy;
        } else {
            // Default
            iconResId = R.drawable.ic_weather_default;
        }

        imageView.setImageResource(iconResId);
    }

    /**
     * Set "Feels Like" icon based on temperature difference
     * @param actualTemp The actual temperature
     * @param feelsLikeTemp The "feels like" temperature
     */
    private void setFeelsLikeIcon(double actualTemp, double feelsLikeTemp) {
        Log.d(TAG, String.format("Setting feels like icon - Actual: %.1f, Feels like: %.1f",
                actualTemp, feelsLikeTemp));

        int iconResId;
        double difference = feelsLikeTemp - actualTemp;

        if (difference > 2) {
            // Feels hotter
            iconResId = R.drawable.ic_hot;
        } else if (difference < -2) {
            // Feels colder
            iconResId = R.drawable.ic_cold;
        } else {
            // Feels similar
            iconResId = R.drawable.ic_normal;
        }

        feelsLikeIcon.setImageResource(iconResId);
    }

    /**
     * Show loading state while fetching data
     */
    private void showLoadingState() {
        textTemp.setText("--°C");
        textFeelsLike.setText("Feels like: --°C");
        textDescription.setText("Loading...");
        weatherIcon.setImageResource(R.drawable.ic_weather_default);
    }

    /**
     * Show error state when data fetch fails
     */
    private void showErrorState() {
        textDescription.setText("Error loading data");
        weatherIcon.setImageResource(R.drawable.ic_error);
    }

    /**
     * Show error message to user
     * @param message The error message to display
     */
    private void showError(String message) {
        Log.e(TAG, "Showing error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Log out the current user
     */
    private void logoutUser() {
        Log.d(TAG, "Logging out user");
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}